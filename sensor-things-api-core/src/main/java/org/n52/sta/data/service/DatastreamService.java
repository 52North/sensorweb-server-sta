/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
package org.n52.sta.data.service;

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.ObservationQuerySpecifications;
import org.n52.sta.data.repositories.DataRepository;
import org.n52.sta.data.repositories.DatasetRepository;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.FormatRepository;
import org.n52.sta.data.repositories.UnitRepository;
import org.n52.sta.data.serialization.ElementWithQueryOptions.DatastreamWithQueryOptions;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider;
import org.n52.sta.edm.provider.entities.SensorEntityProvider;
import org.n52.sta.edm.provider.entities.ThingEntityProvider;
import org.n52.sta.exception.STACRUDException;
import org.n52.sta.mapping.DatastreamMapper;
import org.n52.sta.service.query.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
public class DatastreamService extends AbstractSensorThingsEntityService<DatastreamRepository, DatastreamEntity> {

    private static final Logger logger = LoggerFactory.getLogger(DatastreamService.class);
    private static final DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    private static final ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();

    private final UnitRepository unitRepository;
    private final FormatRepository formatRepository;
    private final DataRepository dataRepository;
    private final DatasetRepository datasetRepository;

    private DatastreamMapper mapper;

    @Autowired
    public DatastreamService(DatastreamRepository repository,
                             DatastreamMapper mapper,
                             UnitRepository unitRepository,
                             FormatRepository formatRepository,
                             DataRepository dataRepository,
                             DatasetRepository datasetRepository) {
        super(repository, DatastreamEntity.class);
        this.mapper = mapper;
        this.unitRepository = unitRepository;
        this.formatRepository = formatRepository;
        this.dataRepository = dataRepository;
        this.datasetRepository = datasetRepository;
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.Datastream, EntityTypes.Datastreams};
    }

    @Override
    protected Object createWrapper(Object entity, QueryOptions queryOptions) {
        return new DatastreamWithQueryOptions((DatastreamEntity) entity, queryOptions);
    }

    @Override
    protected Specification<DatastreamEntity> byRelatedEntityFilter(String relatedId,
                                                                    String relatedType,
                                                                    String ownId) {
        Specification<DatastreamEntity> filter;
        switch (relatedType) {
            case IOT_THING: {
                filter = dQS.withThingIdentifier(relatedId);
                break;
            }
            case IOT_SENSOR: {
                filter = dQS.withSensorIdentifier(relatedId);
                break;
            }
            case IOT_OBSERVED_PROPERTY: {
                filter = dQS.withObservedPropertyIdentifier(relatedId);
                break;
            }
            case IOT_OBSERVATION: {
                filter = dQS.withObservationIdentifier(relatedId);
                break;
            }
            default:
                return null;
        }

        if (ownId != null) {
            filter = filter.and(dQS.withIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
            case "phenomenonTime":
                return DatastreamEntity.PROPERTY_SAMPLING_TIME_START;
            case "resultTime":
                return DatastreamEntity.PROPERTY_RESULT_TIME_START;
            default:
                return super.checkPropertyName(property);
        }
    }

    @Override
    public DatastreamEntity createEntity(DatastreamEntity datastream) throws STACRUDException {
        DatastreamEntity entity = datastream;
        if (!datastream.isProcesssed()) {
            if (datastream.getIdentifier() != null && !datastream.isSetName()) {
                return getRepository().findOne(dQS.withIdentifier(datastream.getIdentifier())).get();
            }
            check(datastream);
            //Specification<DatastreamEntity> predicate = createQuery(datastream);
            //if (getRepository().count() > 0) {
            //    DatastreamEntity optional = getRepository().findOne(predicate).get();
            //    return processObservation((DatastreamEntity) optional.setProcesssed(true),
            //    datastream.getObservations());
            //}
            datastream.setProcesssed(true);
            checkObservationType(datastream);
            checkUnit(datastream);
            datastream.setObservableProperty(getObservedPropertyService().createEntity(datastream.getObservableProperty()));
            datastream.setProcedure(getSensorService().createEntity(datastream.getProcedure()));
            datastream.setThing(getThingService().createEntity(datastream.getThing()));
            if (datastream.getIdentifier() != null) {
                if (getRepository().existsByIdentifier(datastream.getIdentifier())) {
                    throw new STACRUDException("Identifier already exists!", HttpStatus.BAD_REQUEST);
                } else {
                    datastream.setIdentifier(datastream.getIdentifier());
                }
            } else {
                datastream.setIdentifier(UUID.randomUUID().toString());
            }
            entity = getRepository().intermediateSave(datastream);
            processObservation(entity, entity.getObservations());
            entity = getRepository().save(entity);
        }
        return entity;
    }

    private Specification<DatastreamEntity> createQuery(DatastreamEntity datastream) {
        Specification<DatastreamEntity> expression;
        if (datastream.getThing().getIdentifier() != null && !datastream.getThing().isSetName()) {
            expression = dQS.withThingIdentifier(datastream.getThing().getIdentifier());
        } else {
            expression = dQS.withThingName(datastream.getThing().getName());
        }
        if (datastream.getProcedure().getIdentifier() != null && !datastream.getProcedure().isSetName()) {
            expression = expression.and(dQS.withSensorIdentifier(datastream.getProcedure().getIdentifier()));
        } else {
            expression = expression.and(dQS.withSensorName(datastream.getProcedure().getName()));
        }
        if (datastream.getObservableProperty().getIdentifier() != null && !datastream.getObservableProperty()
                .isSetName()) {
            expression = expression.and(dQS.withObservedPropertyIdentifier(datastream.getObservableProperty()
                    .getIdentifier()));
        } else {
            expression = expression.and(dQS.withObservedPropertyName(datastream.getObservableProperty().getName()));
        }
        return expression;
    }

    private void check(DatastreamEntity datastream) throws STACRUDException {
        if (datastream.getThing() == null || datastream.getObservableProperty() == null
                || datastream.getProcedure() == null) {
            throw new STACRUDException("The datastream to create is invalid", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    protected DatastreamEntity updateEntity(DatastreamEntity entity) {
        return getRepository().save(entity);
    }

    @Override
    public DatastreamEntity updateEntity(DatastreamEntity entity, HttpMethod method) throws STACRUDException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            Optional<DatastreamEntity> existing = getRepository().findOne(dQS.withIdentifier(entity.getIdentifier()));
            if (existing.isPresent()) {
                DatastreamEntity merged = mapper.merge(existing.get(), entity);
                checkUnit(merged, entity);
                if (merged.getDatasets() != null) {
                    merged.getDatasets().forEach(d -> {
                        d.setUnit(merged.getUnit());
                        datasetRepository.saveAndFlush(d);
                    });
                }
                return getRepository().save(merged);
            }
            throw new STACRUDException("Unable to update. Entity not found.", HttpStatus.NOT_FOUND);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HttpStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HttpStatus.BAD_REQUEST);
    }

    private void checkUpdate(DatastreamEntity entity) throws STACRUDException {
        String ERROR_MSG = "Inlined entities are not allowed for updates!";
        if (entity.getObservableProperty() != null && (entity.getObservableProperty().getIdentifier() == null
                || entity.getObservableProperty().isSetName() || entity.getObservableProperty().isSetDescription())) {
            throw new STACRUDException(ERROR_MSG, HttpStatus.BAD_REQUEST);
        }

        if (entity.getProcedure() != null
                && (entity.getProcedure().getIdentifier() == null
                || entity.getProcedure().isSetName()
                || entity.getProcedure().isSetDescription())) {
            throw new STACRUDException(ERROR_MSG, HttpStatus.BAD_REQUEST);
        }

        if (entity.getThing() != null && (entity.getThing().getIdentifier() == null || entity.getThing().isSetName()
                || entity.getThing().isSetDescription())) {
            throw new STACRUDException(ERROR_MSG, HttpStatus.BAD_REQUEST);
        }
        if (entity.getObservations() != null) {
            throw new STACRUDException(ERROR_MSG, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void delete(String id) throws STACRUDException {
        if (getRepository().existsByIdentifier(id)) {
            DatastreamEntity datastream = getRepository().getOneByIdentifier(id);
            // check datasets
            deleteRelatedDatasetsAndObservations(datastream);
            // check observations
            getRepository().deleteByIdentifier(id);
        } else {
            throw new STACRUDException("Unable to delete. Entity not found.", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    protected void delete(DatastreamEntity entity) {
        getRepository().deleteByIdentifier(entity.getIdentifier());
    }

    @Override
    protected DatastreamEntity createOrUpdate(DatastreamEntity entity) throws STACRUDException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return updateEntity(entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    private void deleteRelatedDatasetsAndObservations(DatastreamEntity datastream) {
        // update datasets
        datastream.getDatasets().forEach(d -> {
            d.setFirstObservation(null);
            d.setFirstQuantityValue(null);
            d.setFirstValueAt(null);
            d.setLastObservation(null);
            d.setLastQuantityValue(null);
            d.setLastValueAt(null);
            datasetRepository.saveAndFlush(d);
        });
        // delete observations
        dataRepository.deleteAll(dataRepository.findAll(oQS.withDatastreamIdentifier(datastream.getIdentifier())));
        getRepository().flush();
        // delete datasets
        Set<DatasetEntity> datasets = new HashSet<>(datastream.getDatasets());
        datastream.setDatasets(null);
        getRepository().saveAndFlush(datastream);
        datasets.forEach(d -> {
            d.setFirstObservation(null);
            d.setFirstQuantityValue(null);
            d.setFirstValueAt(null);
            d.setLastObservation(null);
            d.setLastQuantityValue(null);
            d.setLastValueAt(null);
            datasetRepository.delete(d);
        });
        getRepository().flush();
    }

    private void checkUnit(DatastreamEntity datastream) {
        UnitEntity unit;
        if (datastream.isSetUnit()) {
            if (!unitRepository.existsBySymbol(datastream.getUnit().getSymbol())) {
                unit = unitRepository.save(datastream.getUnit());
            } else {
                unit = unitRepository.findBySymbol(datastream.getUnit().getSymbol());
            }
            datastream.setUnit(unit);
        }
    }

    private void checkUnit(DatastreamEntity merged, DatastreamEntity toMerge) {
        if (toMerge.isSetUnit()) {
            checkUnit(toMerge);
            merged.setUnit(toMerge.getUnit());
        }
    }

    private void checkObservationType(DatastreamEntity datastream) {
        FormatEntity format;
        if (!formatRepository.existsByFormat(datastream.getObservationType().getFormat())) {
            format = formatRepository.save(datastream.getObservationType());
        } else {
            format = formatRepository.findByFormat(datastream.getObservationType().getFormat());
        }
        datastream.setObservationType(format);
    }

    private DatastreamEntity processObservation(DatastreamEntity datastream,
                                                Set<StaDataEntity> observations) throws STACRUDException {
        if (observations != null && !observations.isEmpty()) {
            Set<DatasetEntity> datasets = new LinkedHashSet<>();
            if (datastream.getDatasets() != null) {
                datasets.addAll(datastream.getDatasets());
            }
            for (StaDataEntity observation : observations) {
                DataEntity<?> data = getObservationService().createEntity(observation);
                if (data != null) {
                    datasets.add(data.getDataset());
                }
            }
            datastream.setDatasets(datasets);
        }
        return datastream;
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, PlatformEntity> getThingService() {
        return (AbstractSensorThingsEntityService<?, PlatformEntity>) getEntityService(EntityTypes.Thing);
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, ProcedureEntity> getSensorService() {
        return (AbstractSensorThingsEntityService<?, ProcedureEntity>) getEntityService(EntityTypes.Sensor);
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, PhenomenonEntity> getObservedPropertyService() {
        return (AbstractSensorThingsEntityService<?, PhenomenonEntity>) getEntityService(EntityTypes.ObservedProperty);
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, DataEntity<?>> getObservationService() {
        return (AbstractSensorThingsEntityService<?, DataEntity<?>>) getEntityService(EntityTypes.Observation);
    }

    @Override
    /* (non-Javadoc)
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        DatastreamEntity entity = (DatastreamEntity) rawObject;

        if (entity.hasThing()) {
            collections.put(ThingEntityProvider.ET_THING_NAME,
                    Collections.singleton(entity.getThing().getIdentifier()));
        }

        if (entity.hasProcedure()) {
            collections.put(SensorEntityProvider.ET_SENSOR_NAME,
                    Collections.singleton(entity.getProcedure().getIdentifier()));
        }

        if (entity.hasObservableProperty()) {
            collections.put(ObservedPropertyEntityProvider.ET_OBSERVED_PROPERTY_NAME,
                    Collections.singleton(entity.getObservableProperty().getIdentifier()));
        }

        //Iterable<DataEntity<?>> observations = dataRepository.findAll(dQS.withId(entity.getId()));
        //Set<Long> observationIds = new HashSet<>();
        //observations.forEach((o) -> {
        //    observationIds.add(o.getId());
        //});
        //collections.put(ET_OBSERVATION_NAME, observationIds);

        return collections;
    }

}
