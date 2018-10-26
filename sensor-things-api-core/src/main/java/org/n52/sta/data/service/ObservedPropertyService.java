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

import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.PhenomenonRepository;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.sta.data.query.DatastreamQuerySpecifications;
import org.n52.sta.data.query.ObservedPropertyQuerySpecifications;
import org.n52.sta.data.query.SensorQuerySpecifications;
import org.n52.sta.data.repositories.DatastreamRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.ObservedPropertyMapper;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class ObservedPropertyService extends AbstractSensorThingsEntityService<PhenomenonRepository, PhenomenonEntity> {

    private ObservedPropertyMapper mapper;
    
    @Autowired
    private DatastreamRepository datastreamRepository;

    private final static ObservedPropertyQuerySpecifications oQS = new ObservedPropertyQuerySpecifications();

    private final static DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    
    public ObservedPropertyService(PhenomenonRepository repository, ObservedPropertyMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }
    
    @Override
    public EntityTypes getType() {
        return EntityTypes.ObservedProperty;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) {
        EntityCollection retEntitySet = new EntityCollection();
        getRepository().findAll(oQS.isValidEntity(), createPageableRequest(queryOptions)).forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(Long id) {
        Optional<PhenomenonEntity> entity = getRepository().findOne(byId(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType, QueryOptions queryOptions) {
        return null;
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
        switch(sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.Datastream": {
            BooleanExpression filter = oQS.withDatastream(sourceId);
            if (targetId != null) {
                filter = filter.and(oQS.withId(targetId));
            }
            return getRepository().exists(filter);
        }
        default: return false;
        }
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<PhenomenonEntity> sensor = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (sensor.isPresent()) {
            return OptionalLong.of(sensor.get().getId());
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
        Optional<PhenomenonEntity> sensor = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (sensor.isPresent()) {
            return mapper.createEntity(sensor.get());
        } else {
            return null;
        }
    }
    
    @Override
    protected String checkPropertyForSorting(String property) {
        switch (property) {
        case "definition":
            return DataEntity.PROPERTY_IDENTIFIER;
        default:
            return super.checkPropertyForSorting(property);
        }
    }

    /**
     * Retrieves ObservedPropertyEntity (aka PhenomenonEntity) with Relation to sourceEntity from Database.
     * Returns empty if Entity is not found or Entities are not related.
     * 
     * @param sourceId Id of the Source Entity
     * @param sourceEntityType Type of the Source Entity
     * @param targetId Id of the Entity to be retrieved
     * @return Optional<PhenomenonEntity> Requested Entity
     */
    private Optional<PhenomenonEntity> getRelatedEntityRaw(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        BooleanExpression filter;
        switch(sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.Datastream": {
            filter = oQS.withDatastream(sourceId);
            break;
        }
        default: return Optional.empty();
        }
        
        if (targetId != null) {
            filter = filter.and(oQS.withId(targetId));
        }
        return getRepository().findOne(filter);
    }

    /**
     * Constructs SQL Expression to request Entity by ID.
     * 
     * @param id id of the requested entity
     * @return BooleanExpression evaluating to true if Entity is found and valid
     */
    private BooleanExpression byId(Long id) {
        return oQS.isValidEntity().and(oQS.withId(id));
    }

    @Override
    public PhenomenonEntity create(PhenomenonEntity observableProperty) {
        if (observableProperty.getId() != null && !observableProperty.isSetName()) {
            return getRepository().findOne(oQS.withId(observableProperty.getId())).get();
        }
        if (getRepository().exists(oQS.withIdentifier(observableProperty.getIdentifier()))) {
            Optional<PhenomenonEntity> optional =
                    getRepository().findOne(oQS.withIdentifier(observableProperty.getIdentifier()));
            return optional.isPresent() ? optional.get() : null;
        }
        return getRepository().save(observableProperty);
    }

    @Override
    public PhenomenonEntity update(PhenomenonEntity entity, HttpMethod method) throws ODataApplicationException {
        if (HttpMethod.PATCH.equals(method)) {
            Optional<PhenomenonEntity> existing = getRepository().findOne(oQS.withId(entity.getId()));
            if (existing.isPresent()) {
                PhenomenonEntity merged = mapper.merge(existing.get(), entity);
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

    @Override
    public void delete(Long id) throws ODataApplicationException {
        if (getRepository().existsById(id)) {
         // delete datastreams
            datastreamRepository.findAll(dQS.withSensor(id)).forEach(d -> {
                try {
                    getDatastreamService().delete(d.getId());
                } catch (ODataApplicationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            getRepository().deleteById(id);
        }
        throw new ODataApplicationException("Entity not found.",
                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
    }
    
    private AbstractSensorThingsEntityService<?, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity>) getEntityService(
                EntityTypes.Datastream);
    }
}
