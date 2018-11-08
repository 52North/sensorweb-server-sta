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

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.ProcedureEntity;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEncodingEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.sta.data.query.HistoricalLocationQuerySpecifications;
import org.n52.sta.data.query.LocationQuerySpecifications;
import org.n52.sta.data.query.ThingQuerySpecifications;
import org.n52.sta.data.repositories.HistoricalLocationRepository;
import org.n52.sta.data.repositories.LocationEncodingRepository;
import org.n52.sta.data.repositories.LocationRepository;
import org.n52.sta.data.repositories.ThingRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.LocationMapper;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.Predicate;
import com.google.common.collect.Sets;
import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class LocationService extends AbstractSensorThingsEntityService<LocationRepository, LocationEntity> {

    private LocationMapper mapper;
    
    @Autowired
    private LocationEncodingRepository locationEncodingRepository;
    
    @Autowired
    private HistoricalLocationRepository historicalLocationRepository;
    
    @Autowired
    private ThingRepository thingRepository;

    private final static LocationQuerySpecifications lQS= new LocationQuerySpecifications();
    
    private HistoricalLocationQuerySpecifications hlQS = new HistoricalLocationQuerySpecifications();
    
    private final static ThingQuerySpecifications tQS = new ThingQuerySpecifications();

    public LocationService(LocationRepository repository, LocationMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }
    
    @Override
    public EntityTypes getType() {
        return EntityTypes.Location;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) throws ODataApplicationException {
        EntityCollection retEntitySet = new EntityCollection();
        Predicate filter = getFilterPredicate(LocationEntity.class, queryOptions);
        getRepository().findAll(filter, createPageableRequest(queryOptions)).forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(Long id) {
        Optional<LocationEntity> entity = getRepository().findOne(byId(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType, QueryOptions queryOptions) throws ODataApplicationException {
        BooleanExpression filter = getFilter(sourceId, sourceEntityType);
        filter = filter.and(getFilterPredicate(LocationEntity.class, queryOptions));
        
        EntityCollection retEntitySet = new EntityCollection();
        Iterable<LocationEntity> locations = getRepository().findAll(filter, createPageableRequest(queryOptions));
        locations.forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public long getRelatedEntityCollectionCount(Long sourceId, EdmEntityType sourceEntityType) {
        BooleanExpression filter = getFilter(sourceId, sourceEntityType);
        return getRepository().count(filter);
    }

    private BooleanExpression getFilter(Long sourceId, EdmEntityType sourceEntityType) {
        BooleanExpression filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.HistoricalLocation": {
            filter = lQS.withRelatedHistoricalLocation(sourceId);
            break;
        }
        case "iot.Thing": {
            filter = lQS.withRelatedThing(sourceId);
            break;
        }
        default:
            return null;
        }
        return filter;
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
        BooleanExpression filter;
        switch(sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.Thing": {
            filter = lQS.withRelatedThing(sourceId);
            break;
        }
        case "iot.HistoricalLocation": {
            filter = lQS.withRelatedHistoricalLocation(sourceId);
            break;
        }
        default: return false;
        }
        if (targetId != null) {
            filter = filter.and(lQS.withId(targetId));
        }
        return getRepository().exists(filter);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        Optional<LocationEntity> location = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (location.isPresent()) {
            return OptionalLong.of(location.get().getId());
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
        Optional<LocationEntity> location = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        if (location.isPresent()) {
            return mapper.createEntity(location.get());
        } else {
            return null;
        }
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
    private Optional<LocationEntity> getRelatedEntityRaw(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        BooleanExpression filter;
        switch(sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
        case "iot.HistoricalLocation": {
            filter = lQS.withRelatedHistoricalLocation(sourceId);
            break;
        }
        case "iot.Thing": {
            filter = lQS.withRelatedThing(sourceId);
            break;
        }
        default: return Optional.empty();
        }

        if (targetId != null) {
            filter = filter.and(lQS.withId(targetId));
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
        return lQS.withId(id);
    }

    @Override
    public LocationEntity create(LocationEntity location) throws ODataApplicationException {
        if (!location.isProcesssed()) {
            if (location.getId() != null && !location.isSetName()) {
                return getRepository().findOne(lQS.withId(location.getId())).get();
            }
            if (getRepository().exists(lQS.withName(location.getName()))) {
                Optional<LocationEntity> optional = getRepository().findOne(lQS.withName(location.getName()));
                return optional.isPresent() ? optional.get() : null;
            }
            location.setProcesssed(true);
            checkLocationEncoding(location);
            location = getRepository().save(location);
            processThings(location);
        }
        return location;
    }

    @Override
    public LocationEntity update(LocationEntity entity, HttpMethod method) throws ODataApplicationException {
        if (HttpMethod.PATCH.equals(method)) {
            Optional<LocationEntity> existing = getRepository().findOne(lQS.withId(entity.getId()));
            if (existing.isPresent()) {
                LocationEntity merged = mapper.merge(existing.get(), entity);
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
    public LocationEntity update(LocationEntity entity) throws ODataApplicationException {
        return getRepository().save(entity);
    }

    @Override
    public void delete(Long id) throws ODataApplicationException {
        if (getRepository().existsById(id)) {
            LocationEntity location = getRepository().getOne(id);
            // delete all historical locations
            for (HistoricalLocationEntity historicalLocation : location.getHistoricalLocationEntities()) {
                getHistoricalLocationService().delete(historicalLocation);
            }
            location.setHistoricalLocationEntities(null);
            getRepository().save(location);
            for (ThingEntity thing : location.getThingEntities()) {
                thing.setLocationEntities(null);
                if (location.getHistoricalLocationEntities() != null) {
                    thing.getHistoricalLocationEntities().removeAll(location.getHistoricalLocationEntities());
                }
                getThingService().update(thing);
            }
            getRepository().deleteById(id);
        } else {
            throw new ODataApplicationException("Entity not found.", HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        }
    }
    
    
    @Override
    public void delete(LocationEntity entity) throws ODataApplicationException {
        getRepository().deleteById(entity.getId());
    }

    private void checkLocationEncoding(LocationEntity location) {
        if (location.getLocationEncoding() != null) {
            LocationEncodingEntity optionalLocationEncoding = createLocationEncoding(location.getLocationEncoding());
            location.setLocationEncoding(optionalLocationEncoding);
        }
    }
    
    
    private LocationEncodingEntity createLocationEncoding(LocationEncodingEntity locationEncoding) {
        ExampleMatcher createEncodingTypeMatcher = createEncodingTypeMatcher();
        if (!locationEncodingRepository.exists(createEncodingTypeExample(locationEncoding, createEncodingTypeMatcher))) {
            return locationEncodingRepository.save(locationEncoding);
        }
        return locationEncodingRepository.findOne(createEncodingTypeExample(locationEncoding, createEncodingTypeMatcher)).get();
    }

    private Example<LocationEncodingEntity> createEncodingTypeExample(LocationEncodingEntity locationEncoding, ExampleMatcher matcher) {
        return Example.<LocationEncodingEntity>of(locationEncoding, matcher);
    }

    private ExampleMatcher createEncodingTypeMatcher() {
        return ExampleMatcher.matching().withMatcher("encodingType", GenericPropertyMatchers.ignoreCase());
    }
    
    private void processThings(LocationEntity location) throws ODataApplicationException {
        if (location.hasThings()) {
            Set<ThingEntity> things = new LinkedHashSet<>();
            for (ThingEntity thing : location.getThingEntities()) {
                thing.setLocationEntities(Sets.newHashSet(location));
                ThingEntity optionalThing = getThingService().createOrUpdate(thing);
                things.add(optionalThing != null ? optionalThing : thing);
            }
            location.setThingEntities(things);
        }
    }

    private AbstractSensorThingsEntityService<?, ThingEntity> getThingService() {
        return (AbstractSensorThingsEntityService<?, ThingEntity>) getEntityService(EntityTypes.Thing);
    }
    
    private AbstractSensorThingsEntityService<?, HistoricalLocationEntity> getHistoricalLocationService() {
        return (AbstractSensorThingsEntityService<?, HistoricalLocationEntity>) getEntityService(EntityTypes.HistoricalLocation);
    }
    
}
