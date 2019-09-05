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

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
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
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.edm.provider.entities.ObservedPropertyEntityProvider;
import org.n52.sta.edm.provider.entities.SensorEntityProvider;
import org.n52.sta.edm.provider.entities.ThingEntityProvider;
import org.n52.sta.mapping.DatastreamMapper;
import org.n52.sta.service.query.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
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
        super(repository);
        this.mapper = mapper;
        this.unitRepository = unitRepository;
        this.formatRepository = formatRepository;
        this.dataRepository = dataRepository;
        this.datasetRepository = datasetRepository;
    }

    @Override
    public EntityTypes getType() {
        return EntityTypes.Datastream;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) throws ODataApplicationException {
        EntityCollection retEntitySet = new EntityCollection();
        Specification<DatastreamEntity> filter = getFilterPredicate(DatastreamEntity.class, queryOptions);

        getRepository().findAll(filter, createPageableRequest(queryOptions))
                       .forEach(t -> retEntitySet.getEntities()
                       .add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(String id) {
        Optional<DatastreamEntity> entity = getRepository().findByIdentifier(id);
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(String sourceId,
                                                       EdmEntityType sourceEntityType,
                                                       QueryOptions queryOptions) {
        Specification<DatastreamEntity> filter = getFilter(sourceId, sourceEntityType)
                .and(getFilterPredicate(DatastreamEntity.class, queryOptions));
        Iterable<DatastreamEntity> datastreams = getRepository().findAll(filter, createPageableRequest(queryOptions));
        EntityCollection retEntitySet = new EntityCollection();
        datastreams.forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public long getRelatedEntityCollectionCount(String sourceId, EdmEntityType sourceEntityType) {
        return getRepository().count(getFilter(sourceId, sourceEntityType));
    }

    @Override
    public boolean existsEntity(String id) {
        return getRepository().existsByIdentifier(id);
    }

    @Override
    public boolean existsRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.existsRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public boolean existsRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Specification<DatastreamEntity> filter = getFilter(sourceId, sourceEntityType);
        if (filter == null) {
            return false;
        }
        if (targetId != null) {
            filter = filter.and(dQS.withIdentifier(targetId));
        }
        return getRepository().count(filter) > 0;
    }

    @Override
    public Optional<String> getIdForRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Optional<String> getIdForRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Optional<DatastreamEntity> thing = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        return thing.map(thingEntity -> Optional.of(thingEntity.getIdentifier())).orElseGet(Optional::empty);
    }

    @Override
    public Entity getRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.getRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Entity getRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Optional<DatastreamEntity> thing = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        return thing.map(datastreamEntity -> mapper.createEntity(datastreamEntity)).orElse(null);
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
    public long getCount(QueryOptions queryOptions) throws ODataApplicationException {
        return getRepository().count(getFilterPredicate(DatastreamEntity.class, queryOptions));
    }

    /**
     * Retrieves Datastream Entity with Relation to sourceEntity from Database.
     * Returns empty if Entity is not found or Entities are not related.
     *
     * @param sourceId         Id of the Source Entity
     * @param sourceEntityType Type of the Source Entity
     * @param targetId         Id of the Entity to be retrieved
     * @return Optional&lt;DatastreamEntity&gt; Requested Entity
     */
    private Optional<DatastreamEntity> getRelatedEntityRaw(String sourceId,
                                                           EdmEntityType sourceEntityType,
                                                           String targetId) {
        Specification<DatastreamEntity> filter = getFilter(sourceId, sourceEntityType);
        if (filter == null) {
            return Optional.empty();
        }

        if (targetId != null) {
            filter = filter.and(dQS.withIdentifier(targetId));
        }
        return getRepository().findOne(filter);
    }

    /**
     * Creates BooleanExpression to Filter Queries depending on source Entity Type
     *
     * @param sourceId         ID of Source Entity
     * @param sourceEntityType Type of Source Entity
     * @return BooleanExpression Filter
     */
    private Specification<DatastreamEntity> getFilter(String sourceId, EdmEntityType sourceEntityType) {
        Specification<DatastreamEntity> filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case "iot.Thing": {
                filter = dQS.withThingIdentifier(sourceId);
                break;
            }
            case "iot.Sensor": {
                filter = dQS.withSensorIdentifier(sourceId);
                break;
            }
            case "iot.ObservedProperty": {
                filter = dQS.withObservedPropertyIdentifier(sourceId);
                break;
            }
            case "iot.Observation": {
                filter = dQS.withObservationIdentifier(sourceId);
                break;
            }
            default:
                return null;
        }
        return Specification.where(filter);
    }

    ///**
    // * Constructs SQL Expression to request Entity by ID.
    // *
    // * @param id id of the requested entity
    // * @return BooleanExpression evaluating to true if Entity is found and valid
    // */
    //private BooleanExpression byId(Long id) {
    //    return dQS.withId(id);
    //}

    @Override
    public DatastreamEntity create(DatastreamEntity datastream) throws ODataApplicationException {
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
            datastream.setObservableProperty(getObservedPropertyService().create(datastream.getObservableProperty()));
            datastream.setProcedure(getSensorService().create(datastream.getProcedure()));
            datastream.setThing(getThingService().create(datastream.getThing()));
            if (datastream.getIdentifier() != null) {
                if (getRepository().existsByIdentifier(datastream.getIdentifier())) {
                    throw new ODataApplicationException("Identifier already exists!",
                            HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
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

    private void check(DatastreamEntity datastream) throws ODataApplicationException {
        if (datastream.getThing() == null || datastream.getObservableProperty() == null
                || datastream.getProcedure() == null) {
            throw new ODataApplicationException("The datastream to create is invalid",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
    }

    @Override
    protected DatastreamEntity update(DatastreamEntity entity) {
        return getRepository().save(entity);
    }

    @Override
    public DatastreamEntity update(DatastreamEntity entity, HttpMethod method) throws ODataApplicationException {
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
            throw new ODataApplicationException(
                    "Unable to update. Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new ODataApplicationException("Http PUT is not yet supported!",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.getDefault());
        }
        throw new ODataApplicationException("Invalid http method for updating entity!",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
    }

    private void checkUpdate(DatastreamEntity entity) throws ODataApplicationException {
        String ERROR_MSG = "Inlined entities are not allowed for updates!";
        if (entity.getObservableProperty() != null && (entity.getObservableProperty().getIdentifier() == null
                || entity.getObservableProperty().isSetName() || entity.getObservableProperty().isSetDescription())) {
            throw new ODataApplicationException(ERROR_MSG,
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }

        if (entity.getProcedure() != null
                && (entity.getProcedure().getIdentifier() == null
                    || entity.getProcedure().isSetName()
                    || entity.getProcedure().isSetDescription())) {
            throw new ODataApplicationException(ERROR_MSG,
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }

        if (entity.getThing() != null && (entity.getThing().getIdentifier() == null || entity.getThing().isSetName()
                || entity.getThing().isSetDescription())) {
            throw new ODataApplicationException(ERROR_MSG,
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
        if (entity.getObservations() != null) {
            throw new ODataApplicationException(ERROR_MSG,
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
    }

    @Override
    public void delete(String id) throws ODataApplicationException {
        if (getRepository().existsByIdentifier(id)) {
            DatastreamEntity datastream = getRepository().getOneByIdentifier(id);
            // check datasets
            deleteRelatedDatasetsAndObservations(datastream);
            // check observations
            getRepository().deleteByIdentifier(id);
        } else {
            throw new ODataApplicationException(
                    "Unable to delete. Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        }
    }

    @Override
    protected void delete(DatastreamEntity entity) {
        getRepository().deleteByIdentifier(entity.getIdentifier());
    }

    @Override
    protected DatastreamEntity createOrUpdate(DatastreamEntity entity) throws ODataApplicationException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return update(entity, HttpMethod.PATCH);
        }
        return create(entity);
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
                                                Set<StaDataEntity> observations) throws ODataApplicationException {
        if (observations != null && !observations.isEmpty()) {
            Set<DatasetEntity> datasets = new LinkedHashSet<>();
            if (datastream.getDatasets() != null) {
                datasets.addAll(datastream.getDatasets());
            }
            for (StaDataEntity observation : observations) {
                DataEntity<?> data = getObservationService().create(observation);
                if (data != null) {
                    datasets.add(data.getDataset());
                }
            }
            datastream.setDatasets(datasets);
        }
        return datastream;
    }

    private AbstractSensorThingsEntityService<?, PlatformEntity> getThingService() {
        return (AbstractSensorThingsEntityService<?, PlatformEntity>) getEntityService(EntityTypes.Thing);
    }

    private AbstractSensorThingsEntityService<?, ProcedureEntity> getSensorService() {
        return (AbstractSensorThingsEntityService<?, ProcedureEntity>) getEntityService(EntityTypes.Sensor);
    }

    private AbstractSensorThingsEntityService<?, PhenomenonEntity> getObservedPropertyService() {
        return (AbstractSensorThingsEntityService<?, PhenomenonEntity>) getEntityService(EntityTypes.ObservedProperty);
    }

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
        try {
            collections.put(ThingEntityProvider.ET_THING_NAME,
                    Collections.singleton(entity.getThing().getIdentifier()));
        } catch (NullPointerException e) {
            logger.debug("No Thing associated with this Entity {}", entity.getIdentifier());
        }
        try {
            collections.put(SensorEntityProvider.ET_SENSOR_NAME,
                    Collections.singleton(entity.getProcedure().getIdentifier()));
        } catch (NullPointerException e) {
            logger.debug("No Sensor associated with this Entity {}", entity.getIdentifier());
        }
        try {
            collections.put(ObservedPropertyEntityProvider.ET_OBSERVED_PROPERTY_NAME,
                    Collections.singleton(entity.getObservableProperty().getIdentifier()));
        } catch (NullPointerException e) {
            logger.debug("No ObservedProperty associated with this Entity {}", entity.getIdentifier());
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
