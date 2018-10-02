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

import java.util.Optional;
import java.util.OptionalLong;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.mapping.DatastreamMapper;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class DatastreamService extends AbstractSensorThingsEntityService<DatastreamRepository> {

    private DatastreamMapper mapper;

    private final static DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();

    public DatastreamService(DatastreamRepository repository, DatastreamMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) {
        EntityCollection retEntitySet = new EntityCollection();
        getRepository().findAll(createPageableRequest(queryOptions)).forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(Long id) {
        Optional<DatastreamEntity> entity = getRepository().findOne(byId(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType, QueryOptions queryOptions) {
        Iterable<DatastreamEntity> datastreams = getRepository().findAll(getFilter(sourceId, sourceEntityType), createPageableRequest(queryOptions));

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
            filter = filter.and(dQS.matchesId(targetId));
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
    protected String checkPropertyForSorting(String property) {
        switch (property) {
        case "phenomenonTime":
            return DatastreamEntity.PROPERTY_SAMPLING_TIME_START;
        case "resultTime":
            return DatastreamEntity.PROPERTY_RESULT_TIME_START;
        default:
            return super.checkPropertyForSorting(property);
        }
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
            filter = filter.and(dQS.matchesId(targetId));
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
        return dQS.matchesId(id);
    }
}
