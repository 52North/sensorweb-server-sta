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

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.uri.UriParameter;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.sta.data.query.HistoricalLocationQuerySpecifications;
import org.n52.sta.data.repositories.HistoricalLocationRepository;
import org.n52.sta.mapping.HistoricalLocationMapper;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class HistoricalLocationService implements AbstractSensorThingsEntityService {

    private HistoricalLocationRepository repository;

    private HistoricalLocationMapper mapper;
    
    private HistoricalLocationQuerySpecifications hlQS = new HistoricalLocationQuerySpecifications();
    
    //TODO: remove deprecated Methods
    @Override
    public Entity getRelatedEntity(Entity sourceEntity) {throw new UnsupportedOperationException("Not supported anymore.");}
    //TODO: remove deprecated Methods
    @Override
    public Entity getRelatedEntity(Entity sourceEntity, List<UriParameter> keyPredicates) {throw new UnsupportedOperationException("Not supported anymore.");}
    //TODO: remove deprecated Methods
    @Override
    public EntityCollection getRelatedEntityCollection(Entity sourceEntity) {throw new UnsupportedOperationException("Not supported anymore.");}

    
    public HistoricalLocationService(HistoricalLocationRepository repository, HistoricalLocationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    @Override
    public EntityCollection getEntityCollection() {
        EntityCollection retEntitySet = new EntityCollection();
        repository.findAll().forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }
    
    @Override
    public Entity getEntity(Long id) {
        Optional<HistoricalLocationEntity> entity = repository.findById(Long.valueOf(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }
 
    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType) {
        BooleanExpression filter;
        switch(sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case "iot.Location": {
                filter = hlQS.getLocationEntityById(sourceId);
                break;
            }
            case "iot.Thing": {
                filter = hlQS.getThingEntityById(sourceId);
                break;
            }
            default: return null;
        }
        
        Iterable<HistoricalLocationEntity> locations = repository.findAll(filter);
        EntityCollection retEntitySet = new EntityCollection();
        locations.forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public boolean existsEntity(Long id) {
        return repository.existsById(id);
    }
    
    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.existsRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        BooleanExpression filter;
        switch(sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case "iot.Location": {
                filter = hlQS.getLocationEntityById(sourceId);
                break;
            }
            case "iot.Thing": {
                filter = hlQS.getThingEntityById(sourceId);
                break;
            }

        default: return false;
        }
        if (targetId != null) {
            filter = filter.and(hlQS.matchesId(targetId));
        }
        return repository.exists(filter);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }
    
    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<HistoricalLocationEntity> thing = this.getRelatedEntityRaw(targetId, sourceEntityType, targetId);
        if (thing.isPresent()) {
            return OptionalLong.of(thing.get().getId());
        } else {
            return OptionalLong.empty();
        }
    }
    
    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return null;
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        return null;
    }
    
    /**
     * Retrieves Thing Entity with Relation to sourceEntity from Database.
     * Returns empty if Thing is not found or Entities are not related.
     * 
     * @param sourceId Id of the Source Entity
     * @param sourceEntityType Type of the Source Entity
     * @param targetId Id of the Thing to be retrieved
     * @return Optional<ThingEntity> Requested Entity
     */
    private Optional<HistoricalLocationEntity> getRelatedEntityRaw(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        // In Theory there are only related Collections
        return Optional.empty();
    }
}
