/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */

package org.n52.sta.data.editor;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.hibernate.Hibernate;
import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetAggregationEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.dataset.DatasetType;
import org.n52.series.db.beans.dataset.ObservationType;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.domain.aggregate.ThingAggregate;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.License;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Party;
import org.n52.sta.api.entity.Project;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.exception.editor.EditorException;
import org.n52.sta.config.EntityPropertyMapping;
import org.n52.sta.data.entity.DatastreamData;
import org.n52.sta.data.entity.LicenseData;
import org.n52.sta.data.entity.ObservationData;
import org.n52.sta.data.entity.ObservedPropertyData;
import org.n52.sta.data.entity.PartyData;
import org.n52.sta.data.entity.ProjectData;
import org.n52.sta.data.entity.SensorData;
import org.n52.sta.data.entity.StaData;
import org.n52.sta.data.entity.ThingData;
import org.n52.sta.data.repositories.entity.DatastreamRepository;
import org.n52.sta.data.repositories.value.CategoryRepository;
import org.n52.sta.data.repositories.value.OfferingRepository;
import org.n52.sta.data.support.DatastreamGraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

public class DatastreamEntityEditor extends DatabaseEntityAdapter<AbstractDatasetEntity> implements
        DatastreamEditorDelegate<Datastream, DatastreamData> {

    private static final Logger logger = LoggerFactory.getLogger(DatastreamEntityEditor.class);

    @Autowired
    private DatastreamRepository datastreamRepository;

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ValueHelper valueHelper;

    @Autowired
    private EntityPropertyMapping propertyMapping;

    private EntityEditorDelegate<Thing, ThingData> thingEditor;
    private EntityEditorDelegate<Sensor, SensorData> sensorEditor;
    private EntityEditorDelegate<ObservedProperty, ObservedPropertyData> observedPropertyEditor;
    private ObservationEditorDelegate<Observation, ObservationData> observationEditor;
    private EntityEditorDelegate<License, LicenseData> licenseEditor;
    private EntityEditorDelegate<Party, PartyData> partyEditor;
    private EntityEditorDelegate<Project, ProjectData> projectEditor;

    private CategoryEntity defaultCategory;

    public DatastreamEntityEditor(EntityServiceLookup serviceLookup) {
        super(serviceLookup);

        // TODO feature toggles, e.g. mobile, FOI-update, ...
    }

    @EventListener
    @SuppressWarnings("unchecked")
    private void postConstruct(ContextRefreshedEvent event) {
        //@formatter:off
        // As we are the package providing the EE Implementations, this cast should never fail.
        this.thingEditor = (EntityEditorDelegate<Thing, ThingData>)
                getService(Thing.class).unwrapEditor();
        this.sensorEditor = (EntityEditorDelegate<Sensor, SensorData>)
                getService(Sensor.class).unwrapEditor();
        this.observedPropertyEditor = (EntityEditorDelegate<ObservedProperty, ObservedPropertyData>)
                getService(ObservedProperty.class).unwrapEditor();
        this.observationEditor = (ObservationEditorDelegate<Observation, ObservationData>)
                getService(Observation.class).unwrapEditor();
        this.licenseEditor = (EntityEditorDelegate<License, LicenseData>)
                getService(License.class).unwrapEditor();
        this.partyEditor = (EntityEditorDelegate<Party, PartyData>)
                getService(Party.class).unwrapEditor();
        this.projectEditor = (EntityEditorDelegate<Project, ProjectData>)
                getService(Project.class).unwrapEditor();
        //@formatter:on

        // Persist or get default category on startup
        final String DEFAULT_CATEGORY = "DEFAULT_STA_CATEGORY";
        defaultCategory = categoryRepository.findByIdentifier(DEFAULT_CATEGORY)
                                            .orElseGet(() -> {
                                                CategoryEntity category = new CategoryEntity();
                                                category.setIdentifier(DEFAULT_CATEGORY);
                                                category.setName(DEFAULT_CATEGORY);
                                                category.setDescription("Default STA category");
                                                logger.debug("Persisting default CategoryEntity: "
                                                        + category.getName());
                                                return categoryRepository.save(category);
                                            });
    }

    @Override
    public DatastreamData getOrSave(Datastream entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must be present");
        Optional<AbstractDatasetEntity> stored = getEntity(entity.getId());
        return stored.map(e -> new DatastreamData(e, Optional.of(propertyMapping)))
                     .orElseGet(() -> save(entity));
    }

    @Override
    public DatastreamData save(Datastream entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null");

        // DTOTransformerImpl#createDatasetEntity
        // CommonDatastreamService
        // DatastreamService (old package)

        // TODO check handling race conditions and rollback handling

        String id = entity.getId() == null
                ? generateId()
                : entity.getId();

        // metadata
        DatasetEntity dataset = new DatasetEntity();
        dataset.setIdentifier(id);
        dataset.setStaIdentifier(id);
        dataset.setName(entity.getName());
        dataset.setDescription(entity.getDescription());

        // values
        Time resultTime = entity.getResultTime();
        valueHelper.setStartTime(dataset::setResultTimeStart, resultTime);
        valueHelper.setEndTime(dataset::setResultTimeEnd, resultTime);
        valueHelper.setFormat(dataset::setOMObservationType, entity.getObservationType());
        valueHelper.setUnit(dataset::setUnit, entity.getUnitOfMeasurement());

        // references
        ThingData thing = thingEditor.getOrSave(entity.getThing());
        ThingAggregate thingAggregate = new ThingAggregate(thing);
        if (thingAggregate.isMobile()) {
            dataset.setDatasetType(DatasetType.trajectory);
            dataset.setMobile(true);
        } else {
            dataset.setDatasetType(DatasetType.timeseries);
            dataset.setMobile(false);
        }
        dataset.setPlatform(thing.getData());

        SensorData sensor = sensorEditor.getOrSave(entity.getSensor());
        ProcedureEntity sensorEntity = sensor.getData();
        dataset.setProcedure(sensorEntity);

        // 52N-DB-model specific entries
        OfferingEntity offering = getOrSaveOfferingValue(sensor);
        dataset.setOffering(offering);
        dataset.setCategory(defaultCategory);

        ObservedPropertyData observedProperty = observedPropertyEditor.getOrSave(entity.getObservedProperty());
        dataset.setObservableProperty(observedProperty.getData());

        Set<Observation> observations = entity.getObservations();
        Streams.stream(observations)
               .findFirst()
               .ifPresentOrElse(o -> dataset.setObservationType(getObservationType(o)),
                                () -> dataset.setObservationType(ObservationType.not_initialized));

        // TODO create aggregate on multiple FOIs <- observation

        // parameters are saved as cascade
        Map<String, Object> properties = entity.getProperties();
        Streams.stream(properties.entrySet())
               .map(e -> convertParameter(dataset, e))
               .forEach(dataset::addParameter);

        DatasetEntity savedEntity = datastreamRepository.save(dataset);

        // Set Observations
        dataset.setObservations(Streams.stream(observations)
                                       .map(o -> observationEditor.getOrSave(o))
                                       .map(StaData::getData)
                                       .collect(Collectors.toSet()));

        if (entity.getLicense() != null) {
            LicenseData licence = licenseEditor.getOrSave(entity.getLicense());
            savedEntity.setLicense(licence.getData());
        }

        if (entity.getParty() != null) {
            PartyData party = partyEditor.getOrSave(entity.getParty());
            savedEntity.setParty(party.getData());
        }

        if (entity.getProject() != null) {
            ProjectData project = projectEditor.getOrSave(entity.getProject());
            savedEntity.setProject(project.getData());
        }
        // TODO explicitly save all references, too? if so, what about CASCADE.PERSIST?
        // sensorEntity.addDatastream(savedEntity);
        // sensorEditor.update(new SensorData(sensorEntity, propertyMapping));

        datastreamRepository.flush();

        return new DatastreamData(savedEntity, Optional.ofNullable(propertyMapping));
    }

    private ObservationType getObservationType(Observation observation) {
        throw new EditorException();
    }

    @Override
    public DatastreamData update(Datastream oldEntity, Datastream updateEntity) throws EditorException {
        Objects.requireNonNull(oldEntity, "no entity to patch found");
        Objects.requireNonNull(updateEntity, "no patches found");

        AbstractDatasetEntity data = ((DatastreamData) oldEntity).getData();

        setIfNotNull(updateEntity::getName, data::setName);
        setIfNotNull(updateEntity::getDescription, data::setDescription);

        if (updateEntity.getUnitOfMeasurement() != null) {
            valueHelper.setUnit(data::setUnit, updateEntity.getUnitOfMeasurement());
        }
        if (updateEntity.getPhenomenonTime() != null) {
            Time phenomenonTime = updateEntity.getPhenomenonTime();
            valueHelper.setStartTime(data::setSamplingTimeStart, phenomenonTime);
            valueHelper.setEndTime(data::setSamplingTimeEnd, phenomenonTime);
        }
        setIfNotNull(updateEntity::getObservedArea, data::setGeometry);
        errorIfNotEmptyMap(updateEntity::getProperties, "properties");

        return new DatastreamData(datastreamRepository.save(data), Optional.empty());
    }

    @Override
    public void delete(String id) throws EditorException {
        AbstractDatasetEntity dataset = getEntity(id)
                .orElseThrow(() -> new EditorException("could not find entity with id: " + id));

        // Delete first/last to be able to delete observations
        dataset.setFirstObservation(null);
        dataset.setLastObservation(null);

        // Delete subdatasets and their observations if we are aggregation
        if (dataset instanceof DatasetAggregationEntity) {
            Set<AbstractDatasetEntity> allByAggregationId =
                    datastreamRepository.findAllByAggregationId(dataset.getId());
            Set<Long> datasetIds = allByAggregationId.stream()
                                                     .map(IdEntity::getId)
                                                     .collect(Collectors.toSet());
            allByAggregationId.forEach(ds -> {
                ds.setFirstObservation(null);
                ds.setLastObservation(null);
            });

            // Flush to disk
            datastreamRepository.saveAll(allByAggregationId);
            datastreamRepository.flush();
            // delete observations
            observationEditor.deleteObservationsByDatasetId(datasetIds);
            // delete subdatastreams
            datasetIds.forEach(datasetId -> datastreamRepository.deleteById(datasetId));
        } else {
            // delete observations
            observationEditor.deleteObservationsByDatasetId(Collections.singleton(dataset.getId()));
        }

        datastreamRepository.delete(dataset);
    }

    @Override
    protected Optional<AbstractDatasetEntity> getEntity(String id) {
        return datastreamRepository.findByStaIdentifier(id, DatastreamGraphBuilder.createEmpty());
    }

    @Transactional(value = TxType.REQUIRES_NEW)
    private OfferingEntity getOrSaveOfferingValue(Sensor sensor) {
        Optional<OfferingEntity> optionalOffering = offeringRepository.findByIdentifier(sensor.getId());
        if (optionalOffering.isPresent()) {
            return optionalOffering.get();
        }

        OfferingEntity offering = new OfferingEntity();
        offering.setIdentifier(sensor.getId());
        offering.setName(sensor.getName());
        offering.setDescription(sensor.getDescription());
        return offeringRepository.save(offering);
    }

    private void assertNew(Datastream datastream) throws EditorException {
        String staIdentifier = datastream.getId();
        if (getEntity(staIdentifier).isPresent()) {
            throw new EditorException("Datastream already exists with ID '" + staIdentifier + "'");
        }
    }

    public boolean removeAsFirstObservation(DataEntity< ? > observation) {
        DatasetEntity dataset = observation.getDataset();
        if (dataset.getFirstObservation() != null
                && dataset.getFirstObservation()
                          .getStaIdentifier()
                          .equals(observation.getStaIdentifier())) {
            deleteFirstObservation(dataset);
            observation.setDataset(datastreamRepository.saveAndFlush(dataset));
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAsLastObservation(DataEntity< ? > observation) {
        DatasetEntity dataset = observation.getDataset();
        if (dataset.getLastObservation() != null
                && dataset.getLastObservation()
                          .getStaIdentifier()
                          .equals(observation.getStaIdentifier())) {
            deleteLastObservation(dataset);
            observation.setDataset(datastreamRepository.saveAndFlush(dataset));
            return true;
        }
        return false;
    }

    public DatasetEntity updateFeature(DatasetEntity datastreamEntity, AbstractFeatureEntity feature) {
        if (datastreamEntity.getFeature() == null) {
            datastreamEntity.setFeature(feature);
            return datastreamRepository.save(datastreamEntity);
        }
        return datastreamEntity;
    }

    public void updateFirstLastObservation(AbstractDatasetEntity datastreamEntity,
            DataEntity< ? > first,
            DataEntity< ? > last) {
        if (first != null
                && (!datastreamEntity.isSetFirstValueAt()
                        || first.getSamplingTimeStart()
                                .before(datastreamEntity.getSamplingTimeStart()))) {
            datastreamEntity.setFirstObservation(first);
            datastreamEntity.setFirstValueAt(first.getSamplingTimeStart());
            DataEntity unwrapped = (DataEntity) Hibernate.unproxy(first);
            if (unwrapped instanceof QuantityDataEntity) {
                datastreamEntity.setFirstQuantityValue(((QuantityDataEntity) unwrapped).getValue());
            }
        }

        if (last != null
                && (!datastreamEntity.isSetLastValueAt()
                        || last.getSamplingTimeEnd()
                               .after(datastreamEntity.getSamplingTimeEnd()))) {
            datastreamEntity.setLastObservation(last);
            datastreamEntity.setFirstValueAt(last.getSamplingTimeEnd());

            DataEntity unwrapped = (DataEntity) Hibernate.unproxy(last);
            if (unwrapped instanceof QuantityDataEntity) {
                datastreamEntity.setLastQuantityValue(((QuantityDataEntity) unwrapped).getValue());
            }
        }

        datastreamRepository.save(datastreamEntity);

        // update parent if datastream is part of aggregation
        if (datastreamEntity.isSetAggregation()) {
            updateFirstLastObservation(
                                       datastreamRepository.findById(datastreamEntity.getAggregation()
                                                                                     .getId())
                                                           .get(),
                                       first,
                                       last);
        }
    }

    public void clearFirstObservationLastObservationFeature(DatasetEntity dataset) {
        deleteFirstObservation(dataset);
        deleteLastObservation(dataset);
        dataset.setFeature(null);

        datastreamRepository.saveAndFlush(dataset);
    }

    private void deleteFirstObservation(DatasetEntity dataset) {
        dataset.setFirstObservation(null);
        dataset.setFirstQuantityValue(null);
        dataset.setFirstValueAt(null);
    }

    private void deleteLastObservation(DatasetEntity dataset) {
        dataset.setLastObservation(null);
        dataset.setLastQuantityValue(null);
        dataset.setLastValueAt(null);
    }
}
