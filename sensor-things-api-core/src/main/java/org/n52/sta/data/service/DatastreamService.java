/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.DataRepository;
import org.n52.series.db.DatasetRepository;
import org.n52.series.db.FormatRepository;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.StaDataEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.series.db.query.DatasetQuerySpecifications;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.ObservationQuerySpecifications;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.repositories.UnitRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.DatastreamMapper;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class DatastreamService extends AbstractSensorThingsEntityService<DatastreamRepository, DatastreamEntity> {

    private DatastreamMapper mapper;
    
    @Autowired
    private UnitRepository unitRepository;
    
    @Autowired
    private FormatRepository formatRepository;
    
    @Autowired
    private DataRepository<DataEntity<?>> dataRepository;
    
    @Autowired
    private DatasetRepository<DatasetEntity> datasetRepository;
    
    private final static DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    
    private ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    
    private DatasetQuerySpecifications dsQS = DatasetQuerySpecifications.of(null);

    public DatastreamService(DatastreamRepository repository, DatastreamMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }

    @Override
    public EntityTypes getType() {
        return EntityTypes.Datastream;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) throws ODataApplicationException {
        EntityCollection retEntitySet = new EntityCollection();
        Predicate filter = getFilterPredicate(DatastreamEntity.class, queryOptions);
        getRepository().findAll(filter, createPageableRequest(queryOptions)).forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(Long id) {
        Optional<DatastreamEntity> entity = getRepository().findOne(byId(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType, QueryOptions queryOptions) throws ODataApplicationException {
        Predicate filter = getFilter(sourceId, sourceEntityType).and(getFilterPredicate(DatastreamEntity.class, queryOptions));
        Iterable<DatastreamEntity> datastreams = getRepository().findAll(filter, createPageableRequest(queryOptions));
        EntityCollection retEntitySet = new EntityCollection();
        datastreams.forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }
    
    @Override
    public long getRelatedEntityCollectionCount(Long sourceId, EdmEntityType sourceEntityType) {
        return getRepository().count(getFilter(sourceId, sourceEntityType));
    }
    
    @Override
    public boolean existsEntity(Long id) {
        return getRepository().exists(byId(id));
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.existsRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        BooleanExpression filter = getFilter(sourceId, sourceEntityType);
        if (filter == null) {
            return false;
        }
        if (targetId != null) {
            filter = filter.and(dQS.withId(targetId));
        }
        return getRepository().exists(filter);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<DatastreamEntity> thing = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (thing.isPresent()) {
            return OptionalLong.of(thing.get().getId());
        } else {
            return OptionalLong.empty();
        }
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<DatastreamEntity> thing = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (thing.isPresent()) {
            return mapper.createEntity(thing.get());
        } else {
            return null;
        }
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
     * @param sourceId Id of the Source Entity
     * @param sourceEntityType Type of the Source Entity
     * @param targetId Id of the Entity to be retrieved
     * @return Optional<DatastreamEntity> Requested Entity
     */
    private Optional<DatastreamEntity> getRelatedEntityRaw(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        BooleanExpression filter = getFilter(sourceId, sourceEntityType);
        if (filter == null) {
            return Optional.empty();
        }

        if (targetId != null) {
            filter = filter.and(dQS.withId(targetId));
        }
        return getRepository().findOne(filter);
    }

    /**
     * Creates BooleanExpression to Filter Queries depending on source Entity Type
     * 
     * @param sourceId ID of Source Entity
     * @param sourceEntityType Type of Source Entity
     * @return BooleanExpression Filter
     */
    private BooleanExpression getFilter(Long sourceId, EdmEntityType sourceEntityType) {
        BooleanExpression filter;
        switch(sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.Thing": {
            filter = dQS.withThing(sourceId);
            break;
        }
        case "iot.Sensor": {
            filter = dQS.withSensor(sourceId);
            break;
        }
        case "iot.ObservedProperty": {
            filter = dQS.withObservedProperty(sourceId);
            break;
        }
        case "iot.Observation": {
            filter = dQS.withObservation(sourceId);
            break;
        }
        default: return null;
        }
        return filter;
    }

    /**
     * Constructs SQL Expression to request Entity by ID.
     * 
     * @param id id of the requested entity
     * @return BooleanExpression evaluating to true if Entity is found and valid
     */
    private BooleanExpression byId(Long id) {
        return dQS.withId(id);
    }

    @Override
    public DatastreamEntity create(DatastreamEntity datastream) throws ODataApplicationException {
        if (!datastream.isProcesssed()) {
            if (datastream.getId() != null && !datastream.isSetName()) {
                return getRepository().findOne(dQS.withId(datastream.getId())).get();
            }
            check(datastream);
            Predicate predicate = createQuery(datastream);
            if (getRepository().exists(predicate)) {
                DatastreamEntity optional = getRepository().findOne(predicate).get();
                return processObservation((DatastreamEntity) optional.setProcesssed(true), datastream.getObservations());
            }
            datastream.setProcesssed(true);
            checkObservationType(datastream);
            checkUnit(datastream);
            datastream.setObservableProperty(getObservedPropertyService().create(datastream.getObservableProperty()));
            datastream.setProcedure(getSensorService().create(datastream.getProcedure()));
            datastream.setThing(getThingService().create(datastream.getThing()));
            datastream = getRepository().save(datastream);
            processObservation(datastream, datastream.getObservations());
            datastream = getRepository().save(datastream);
        }
        return datastream;
    }

    private Predicate createQuery(DatastreamEntity datastream) {
        BooleanExpression expression;
        if (datastream.getThing().getId() != null && !datastream.getThing().isSetName()) {
            expression = dQS.withThing(datastream.getThing().getId());
        } else {
            expression = dQS.withThing(datastream.getThing().getName());
        }
        if (datastream.getProcedure().getId() != null && !datastream.getProcedure().isSetName()) {
            expression = expression.and(dQS.withSensor(datastream.getProcedure().getId()));
        } else {
            expression = expression.and(dQS.withSensor(datastream.getProcedure().getName()));
        }
        if (datastream.getObservableProperty().getId() != null && !datastream.getObservableProperty().isSetName()) {
            expression =  expression.and(dQS.withObservedProperty(datastream.getObservableProperty().getId()));
        } else {
            expression = expression.and(dQS.withObservedProperty(datastream.getObservableProperty().getName()));
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
    public DatastreamEntity update(DatastreamEntity entity, HttpMethod method) throws ODataApplicationException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            Optional<DatastreamEntity> existing = getRepository().findOne(dQS.withId(entity.getId()));
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
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new ODataApplicationException("Http PUT is not yet supported!",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.getDefault());
        }
        throw new ODataApplicationException("Invalid http method for updating entity!",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
    }
    
    private void checkUnit(DatastreamEntity merged, DatastreamEntity toMerge) {
        if (toMerge.isSetUnit()) {
            checkUnit(toMerge);
            merged.setUnit(toMerge.getUnit());
        }
    }

    private void checkUpdate(DatastreamEntity entity) throws ODataApplicationException {
        if (entity.getObservableProperty() != null && (entity.getObservableProperty().getId() == null
                || entity.getObservableProperty().isSetName() || entity.getObservableProperty().isSetDescription())) {
            throw new ODataApplicationException("Inlined entities are not allowed for updates!",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }

        if (entity.getProcedure() != null  && (entity.getProcedure().getId() == null || entity.getProcedure().isSetName()
                || entity.getProcedure().isSetDescription())) {
            throw new ODataApplicationException("Inlined entities are not allowed for updates!",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }

        if (entity.getThing() != null  && (entity.getThing().getId() == null || entity.getThing().isSetName()
                || entity.getThing().isSetDescription())) {
            throw new ODataApplicationException("Inlined entities are not allowed for updates!",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
        if (entity.getObservations() != null) {
            throw new ODataApplicationException("Inlined entities are not allowed for updates!",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
    }
    
    @Override
    protected DatastreamEntity update(DatastreamEntity entity) throws ODataApplicationException {
        return getRepository().save(entity);
    }

    @Override
    public void delete(Long id) throws ODataApplicationException {
        if (getRepository().existsById(id)) {
            DatastreamEntity datastream = getRepository().getOne(id);
            // check datasets
            deleteRelatedDatasetsAndObservations(datastream);
            // check observations
            getRepository().deleteById(id);
        } else {
            throw new ODataApplicationException("Entity not found.", HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        }
    }
    
    @Override
    protected void delete(DatastreamEntity entity) throws ODataApplicationException {
        getRepository().deleteById(entity.getId());
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
        dataRepository.deleteAll(dataRepository.findAll(oQS.withDatastream(datastream.getId())));
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

    private void checkObservationType(DatastreamEntity datastream) {
        FormatEntity format;
        if (!formatRepository.existsByFormat(datastream.getObservationType().getFormat())) {
            format = formatRepository.save(datastream.getObservationType());
        } else {
            format = formatRepository.findByFormat(datastream.getObservationType().getFormat());
        }
        datastream.setObservationType(format);
    }
    
    private DatastreamEntity processObservation(DatastreamEntity datastream, Set<StaDataEntity> observations) throws ODataApplicationException {
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

    private AbstractSensorThingsEntityService<?, ThingEntity> getThingService() {
        return (AbstractSensorThingsEntityService<?, ThingEntity>) getEntityService(EntityTypes.Thing);
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

}
