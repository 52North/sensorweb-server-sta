/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.DatastreamEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.ObservationQuerySpecifications;
import org.n52.sta.data.repositories.DataRepository;
import org.n52.sta.data.repositories.DatasetRepository;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.FormatRepository;
import org.n52.sta.data.repositories.UnitRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.data.service.util.CollectionWrapper;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class DatastreamService
        extends AbstractSensorThingsEntityService<DatastreamRepository, DatastreamEntity, DatastreamEntity> {

    //private static final Logger logger = LoggerFactory.getLogger(DatastreamService.class);
    private static final DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    private static final ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    private static final String UNKNOWN = "unknown";

    private final UnitRepository unitRepository;
    private final FormatRepository formatRepository;
    private final DataRepository dataRepository;
    private final DatasetRepository datasetRepository;

    @Autowired
    public DatastreamService(DatastreamRepository repository,
                             UnitRepository unitRepository,
                             FormatRepository formatRepository,
                             DataRepository dataRepository,
                             DatasetRepository datasetRepository) {
        super(repository,
              DatastreamEntity.class,
              EntityGraphRepository.FetchGraph.FETCHGRAPH_OBS_TYPE,
              EntityGraphRepository.FetchGraph.FETCHGRAPH_UOM);
        this.unitRepository = unitRepository;
        this.formatRepository = formatRepository;
        this.dataRepository = dataRepository;
        this.datasetRepository = datasetRepository;
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.Datastream, EntityTypes.Datastreams};
    }

    @Override protected DatastreamEntity fetchExpandEntities(DatastreamEntity entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (DatastreamEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                CollectionWrapper expandedEntities;
                ElementWithQueryOptions<?> expandedEntity;
                switch (expandProperty) {
                case STAEntityDefinition.SENSOR:
                    expandedEntity = getSensorService()
                            .getEntityByRelatedEntity(entity.getIdentifier(),
                                                      DatastreamEntity.class.getSimpleName(),
                                                      null,
                                                      expandItem.getQueryOptions());
                    entity.setProcedure((ProcedureEntity) expandedEntity.getEntity());
                    break;
                case STAEntityDefinition.THING:
                    expandedEntity = getThingService()
                            .getEntityByRelatedEntity(entity.getIdentifier(),
                                                      DatastreamEntity.class.getSimpleName(),
                                                      null,
                                                      expandItem.getQueryOptions());
                    entity.setThing((PlatformEntity) expandedEntity.getEntity());
                    break;
                case STAEntityDefinition.OBSERVED_PROPERTY:
                    expandedEntity = getObservedPropertyService()
                            .getEntityByRelatedEntity(entity.getIdentifier(),
                                                      DatastreamEntity.class.getSimpleName(),
                                                      null,
                                                      expandItem.getQueryOptions());
                    entity.setObservableProperty((PhenomenonEntity) expandedEntity.getEntity());
                    break;
                case STAEntityDefinition.OBSERVATIONS:
                    expandedEntities = getObservationService()
                            .getEntityCollectionByRelatedEntity(entity.getIdentifier(),
                                                                DatastreamEntity.class.getSimpleName(),
                                                                expandItem.getQueryOptions());
                    entity.setObservations(
                            expandedEntities.getEntities()
                                            .stream()
                                            .map(s -> (StaDataEntity<?>) s.getEntity())
                                            .collect(Collectors.toSet()));
                    break;
                default:
                    throw new RuntimeException("This can never happen!");
                }
            } else {
                throw new STAInvalidQueryException("Invalid expandOption supplied. Cannot find " + expandProperty +
                                                           " on Entity of type 'Datastream'");
            }
        }
        return entity;
    }

    @Override
    protected Specification<DatastreamEntity> byRelatedEntityFilter(String relatedId,
                                                                    String relatedType,
                                                                    String ownId) {
        Specification<DatastreamEntity> filter;
        switch (relatedType) {
        case STAEntityDefinition.THINGS: {
            filter = dQS.withThingIdentifier(relatedId);
            break;
        }
        case STAEntityDefinition.SENSORS: {
            filter = dQS.withSensorIdentifier(relatedId);
            break;
        }
        case STAEntityDefinition.OBSERVED_PROPERTIES: {
            filter = dQS.withObservedPropertyIdentifier(relatedId);
            break;
        }
        case STAEntityDefinition.OBSERVATIONS: {
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
            // Getting by reference
            if (datastream.getIdentifier() != null && !datastream.isSetName()) {
                Optional<DatastreamEntity> optionalEntity =
                        getRepository().findOne(dQS.withIdentifier(datastream.getIdentifier()));
                if (optionalEntity.isPresent()) {
                    return optionalEntity.get();
                } else {
                    throw new STACRUDException("No Datastream with id '" + datastream.getIdentifier() + "' found");
                }
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
            datastream.setObservableProperty(getObservedPropertyService()
                                                     .createEntity(datastream.getObservableProperty()));
            datastream.setProcedure(getSensorService().createEntity(datastream.getProcedure()));
            datastream.setThing(getThingService().createEntity(datastream.getThing()));
            if (datastream.getIdentifier() != null) {
                if (getRepository().existsByIdentifier(datastream.getIdentifier())) {
                    throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
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
        return getRepository().findByIdentifier(entity.getIdentifier(),
                                                EntityGraphRepository.FetchGraph.FETCHGRAPH_UOM,
                                                EntityGraphRepository.FetchGraph.FETCHGRAPH_OBS_TYPE)
                              .orElseThrow(() -> new STACRUDException("Datastream requested but still processing!"));
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
            throw new STACRUDException("The datastream to create is invalid", HTTPStatus.BAD_REQUEST);
        }
    }

    @Override
    protected DatastreamEntity updateEntity(DatastreamEntity entity) {
        return getRepository().save(entity);
    }

    @Override
    public DatastreamEntity updateEntity(String id, DatastreamEntity entity, HttpMethod method)
            throws STACRUDException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            Optional<DatastreamEntity> existing =
                    getRepository().findOne(dQS.withIdentifier(id),
                                            EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS,
                                            EntityGraphRepository.FetchGraph.FETCHGRAPH_UOM,
                                            EntityGraphRepository.FetchGraph.FETCHGRAPH_OBS_TYPE);
            if (existing.isPresent()) {
                DatastreamEntity merged = merge(existing.get(), entity);
                checkUnit(merged, entity);
                if (merged.getDatasets() != null) {
                    merged.getDatasets().forEach(d -> {
                        d.setUnit(merged.getUnit());
                        datasetRepository.saveAndFlush(d);
                    });
                }
                getRepository().save(merged);
                return merged;
            }
            throw new STACRUDException("Unable to update. Entity not found.", HTTPStatus.NOT_FOUND);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HTTPStatus.BAD_REQUEST);
    }

    private void checkUpdate(DatastreamEntity entity) throws STACRUDException {
        String ERROR_MSG = "Inlined entities are not allowed for updates!";
        if (entity.getObservableProperty() != null && (entity.getObservableProperty().getIdentifier() == null
                || entity.getObservableProperty().isSetName() || entity.getObservableProperty().isSetDescription())) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }

        if (entity.getProcedure() != null
                && (entity.getProcedure().getIdentifier() == null
                || entity.getProcedure().isSetName()
                || entity.getProcedure().isSetDescription())) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }

        if (entity.getThing() != null && (entity.getThing().getIdentifier() == null || entity.getThing().isSetName()
                || entity.getThing().isSetDescription())) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }
        if (entity.getObservations() != null) {
            throw new STACRUDException(ERROR_MSG, HTTPStatus.BAD_REQUEST);
        }
    }

    @Override
    public void delete(String id) throws STACRUDException {
        if (getRepository().existsByIdentifier(id)) {
            Optional<DatastreamEntity> datastream =
                    getRepository().findByIdentifier(id,
                                                     EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASETS);
            // check datasets
            deleteRelatedDatasetsAndObservations(datastream.get());
            // check observations
            getRepository().deleteByIdentifier(id);
        } else {
            throw new STACRUDException("Unable to delete. Entity not found.", HTTPStatus.NOT_FOUND);
        }
    }

    @Override
    protected void delete(DatastreamEntity entity) {
        getRepository().deleteByIdentifier(entity.getIdentifier());
    }

    @Override
    protected DatastreamEntity createOrUpdate(DatastreamEntity entity) throws STACRUDException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return updateEntity(entity.getIdentifier(), entity, HttpMethod.PATCH);
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

    private void checkObservationType(DatastreamEntity existing, DatastreamEntity toMerge)
            throws STACRUDException {
        if (toMerge.isSetObservationType() && !toMerge.getObservationType()
                                                      .getFormat()
                                                      .equalsIgnoreCase(UNKNOWN)
                && !existing.getObservationType().getFormat().equals(toMerge.getObservationType().getFormat())) {
            throw new STACRUDException(
                    String.format(
                            "The updated observationType (%s) does not comply with the existing observationType (%s)",
                            toMerge.getObservationType().getFormat(),
                            existing.getObservationType().getFormat()),
                    HTTPStatus.CONFLICT);
        }
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

    @Override
    /* (non-Javadoc)
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        DatastreamEntity entity = (DatastreamEntity) rawObject;

        if (entity.hasThing()) {
            collections.put(STAEntityDefinition.THINGS,
                            Collections.singleton(entity.getThing().getIdentifier()));
        }

        if (entity.hasProcedure()) {
            collections.put(STAEntityDefinition.SENSORS,
                            Collections.singleton(entity.getProcedure().getIdentifier()));
        }

        if (entity.hasObservableProperty()) {
            collections.put(STAEntityDefinition.OBSERVED_PROPERTIES,
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

    @Override
    public DatastreamEntity merge(DatastreamEntity existing, DatastreamEntity toMerge) throws STACRUDException {
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        checkObservationType(existing, toMerge);
        // observedArea
        if (toMerge.isSetGeometry()) {
            existing.setGeometryEntity(toMerge.getGeometryEntity());
        }
        // unit
        if (toMerge.isSetUnit() && existing.getUnit().getSymbol().equals(toMerge.getUnit().getSymbol())) {
            existing.setUnit(toMerge.getUnit());
        }

        // resultTime
        if (toMerge.hasResultTimeStart() && toMerge.hasResultTimeEnd()) {
            existing.setResultTimeStart(toMerge.getResultTimeStart());
            existing.setResultTimeEnd(toMerge.getResultTimeEnd());
        }
        // observationType
        if (existing.getDatasets() == null || existing.getDatasets().isEmpty() && toMerge.isSetObservationType()) {
            if (!existing.getObservationType().getFormat().equals(toMerge.getObservationType().getFormat())
                    && !toMerge.getObservationType().getFormat().equalsIgnoreCase(UNKNOWN)) {
                existing.setObservationType(toMerge.getObservationType());
            }
        }
        return existing;
    }

}
