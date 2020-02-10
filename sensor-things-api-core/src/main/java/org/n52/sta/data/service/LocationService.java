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

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.query.LocationQuerySpecifications;
import org.n52.sta.data.repositories.LocationEncodingRepository;
import org.n52.sta.data.repositories.LocationRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider;
import org.n52.sta.edm.provider.entities.ThingEntityProvider;
import org.n52.sta.mapping.LocationMapper;
import org.n52.sta.service.query.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
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
public class LocationService extends AbstractSensorThingsEntityService<LocationRepository, LocationEntity> {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private static final LocationQuerySpecifications lQS = new LocationQuerySpecifications();

    private final LocationEncodingRepository locationEncodingRepository;
    private final String IOT_THING = "iot.Thing";
    private final String IOT_HISTORICAL_LOCATION = "iot.HistoricalLocation";
    private final String ENCODINGTYPE = "encodingType";

    private LocationMapper mapper;

    @Autowired
    public LocationService(LocationRepository repository,
                           LocationMapper mapper,
                           LocationEncodingRepository locationEncodingRepository) {
        super(repository);
        this.mapper = mapper;
        this.locationEncodingRepository = locationEncodingRepository;
    }

    @Override
    public EntityTypes getType() {
        return EntityTypes.Location;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) throws ODataApplicationException {
        EntityCollection retEntitySet = new EntityCollection();
        Specification<LocationEntity> filter = getFilterPredicate(LocationEntity.class, queryOptions);
        getRepository().findAll(filter, createPageableRequest(queryOptions))
                       .forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(String id) {
        Optional<LocationEntity> entity = getRepository().findByIdentifier(id);
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(String sourceId,
                                                       EdmEntityType sourceEntityType,
                                                       QueryOptions queryOptions) throws ODataApplicationException {
        Specification<LocationEntity> filter = getFilter(sourceId, sourceEntityType);
        filter = filter.and(getFilterPredicate(LocationEntity.class, queryOptions));

        EntityCollection retEntitySet = new EntityCollection();
        Iterable<LocationEntity> locations = getRepository().findAll(filter, createPageableRequest(queryOptions));
        locations.forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public long getRelatedEntityCollectionCount(String sourceId, EdmEntityType sourceEntityType) {
        Specification<LocationEntity> filter = getFilter(sourceId, sourceEntityType);
        return getRepository().count(filter);
    }

    private Specification<LocationEntity> getFilter(String sourceId, EdmEntityType sourceEntityType) {
        Specification<LocationEntity> filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case IOT_HISTORICAL_LOCATION: {
                filter = lQS.withRelatedHistoricalLocationIdentifier(sourceId);
                break;
            }
            case IOT_THING: {
                filter = lQS.withRelatedThingIdentifier(sourceId);
                break;
            }
            default:
                return null;
        }
        return filter;
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
        Specification<LocationEntity> filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case IOT_THING: {
                filter = lQS.withRelatedThingIdentifier(sourceId);
                break;
            }
            case IOT_HISTORICAL_LOCATION: {
                filter = lQS.withRelatedHistoricalLocationIdentifier(sourceId);
                break;
            }
            default:
                return false;
        }
        if (targetId != null) {
            filter = filter.and(lQS.withIdentifier(targetId));
        }
        return getRepository().count(filter) > 0;
    }

    @Override
    public Optional<String> getIdForRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Optional<String> getIdForRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Optional<LocationEntity> location = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        return location.map(locationEntity -> Optional.of(locationEntity.getIdentifier())).orElseGet(Optional::empty);
    }

    @Override
    public Entity getRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.getRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Entity getRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Optional<LocationEntity> location = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        return location.map(locationEntity -> mapper.createEntity(locationEntity)).orElse(null);
    }

    /**
     * Retrieves Thing Entity with Relation to sourceEntity from Database.
     * Returns empty if Thing is not found or Entities are not related.
     *
     * @param sourceId         Id of the Source Entity
     * @param sourceEntityType Type of the Source Entity
     * @param targetId         Id of the Thing to be retrieved
     * @return Optional&lt;PlatformEntity&gt; Requested Entity
     */
    private Optional<LocationEntity> getRelatedEntityRaw(String sourceId,
                                                         EdmEntityType sourceEntityType,
                                                         String targetId) {
        Specification<LocationEntity> filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case IOT_HISTORICAL_LOCATION: {
                filter = lQS.withRelatedHistoricalLocationIdentifier(sourceId);
                break;
            }
            case IOT_THING: {
                filter = lQS.withRelatedThingIdentifier(sourceId);
                break;
            }
            default:
                return Optional.empty();
        }

        if (targetId != null) {
            filter = filter.and(lQS.withIdentifier(targetId));
        }
        return getRepository().findOne(filter);
    }

    @Override
    public long getCount(QueryOptions queryOptions) throws ODataApplicationException {
        return getRepository().count(getFilterPredicate(LocationEntity.class, queryOptions));
    }

    @Override
    public LocationEntity create(LocationEntity newLocation) throws ODataApplicationException {
        LocationEntity location = newLocation;
        if (!location.isProcesssed()) {
            if (location.getIdentifier() != null && !location.isSetName()) {
                return getRepository().findByIdentifier(location.getIdentifier()).get();
            }
            if (location.getIdentifier() == null) {
                if (getRepository().existsByName(location.getName())) {
                    Optional<LocationEntity> optional = getRepository().findByName(location.getName());
                    return optional.orElse(null);
                } else {
                    // Autogenerate Identifier
                    location.setIdentifier(UUID.randomUUID().toString());
                }
            } else if (getRepository().existsByIdentifier(location.getIdentifier())) {
                throw new ODataApplicationException("Identifier already exists!",
                        HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
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
            Optional<LocationEntity> existing = getRepository().findByIdentifier(entity.getIdentifier());
            if (existing.isPresent()) {
                LocationEntity merged = mapper.merge(existing.get(), entity);
                return getRepository().save(merged);
            }
            throw new ODataApplicationException(
                    "Unable to update. Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new ODataApplicationException("Http PUT is not yet supported!",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.getDefault());
        }
        throw new ODataApplicationException("Invalid http method for updating entity!",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
    }

    @Override
    public LocationEntity update(LocationEntity entity) {
        return getRepository().save(entity);
    }

    @Override
    public void delete(String id) throws ODataApplicationException {
        if (getRepository().existsByIdentifier(id)) {
            LocationEntity location = getRepository().getOneByIdentifier(id);
            // delete all historical locations
            for (HistoricalLocationEntity historicalLocation : location.getHistoricalLocations()) {
                getHistoricalLocationService().delete(historicalLocation);
            }
            location.setHistoricalLocations(null);
            getRepository().save(location);
            for (PlatformEntity thing : location.getThings()) {
                thing.setLocations(null);
                if (location.getHistoricalLocations() != null) {
                    thing.getHistoricalLocations().removeAll(location.getHistoricalLocations());
                }
                getThingService().update(thing);
            }
            getRepository().deleteByIdentifier(id);
        } else {
            throw new ODataApplicationException(
                    "Unable to delete. Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        }
    }

    @Override
    public void delete(LocationEntity entity) {
        getRepository().deleteByIdentifier(entity.getIdentifier());
    }

    @Override
    protected LocationEntity createOrUpdate(LocationEntity entity) throws ODataApplicationException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return update(entity, HttpMethod.PATCH);
        }
        return create(entity);
    }

    @Override
    public String checkPropertyName(String property) {
        if (property.equals(ENCODINGTYPE)) {
            return LocationEntity.PROPERTY_NAME;
        } else if (property.equals("location")) {
            return "name desc";
        } else {
            return property;
        }
    }

    private void checkLocationEncoding(LocationEntity location) {
        if (location.getLocationEncoding() != null) {
            FormatEntity optionalLocationEncoding = createLocationEncoding(location.getLocationEncoding());
            location.setLocationEncoding(optionalLocationEncoding);
        }
    }

    private FormatEntity createLocationEncoding(FormatEntity locationEncoding) {
        ExampleMatcher createEncodingTypeMatcher = createEncodingTypeMatcher();
        if (!locationEncodingRepository.exists(
                createEncodingTypeExample(locationEncoding, createEncodingTypeMatcher))) {
            return locationEncodingRepository.save(locationEncoding);
        }
        return locationEncodingRepository.findOne(
                createEncodingTypeExample(locationEncoding, createEncodingTypeMatcher)).get();
    }

    private Example<FormatEntity> createEncodingTypeExample(FormatEntity locationEncoding,
                                                                      ExampleMatcher matcher) {
        return Example.<FormatEntity>of(locationEncoding, matcher);
    }

    private ExampleMatcher createEncodingTypeMatcher() {
        return ExampleMatcher.matching().withMatcher(ENCODINGTYPE, GenericPropertyMatchers.ignoreCase());
    }

    private void processThings(LocationEntity location) throws ODataApplicationException {
        if (location.hasThings()) {
            Set<PlatformEntity> things = new LinkedHashSet<>();
            for (PlatformEntity thing : location.getThings()) {
                HashSet<LocationEntity> set = new HashSet<>();
                set.add(createReferencedLocation(location));
                thing.setLocations(set);
                PlatformEntity optionalThing = getThingService().createOrUpdate(thing);
                things.add(optionalThing != null ? optionalThing : thing);
            }
            location.setThings(things);
        }
    }

    private LocationEntity createReferencedLocation(LocationEntity location) {
        LocationEntity referenced = new LocationEntity();
        referenced.setIdentifier(location.getIdentifier());
        referenced.setId(location.getId());
        return referenced;
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, PlatformEntity> getThingService() {
        return (AbstractSensorThingsEntityService<?, PlatformEntity>) getEntityService(EntityTypes.Thing);
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, HistoricalLocationEntity> getHistoricalLocationService() {
        return (AbstractSensorThingsEntityService<?, HistoricalLocationEntity>)
                getEntityService(EntityTypes.HistoricalLocation);
    }

    /* (non-Javadoc)
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    @Override
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        LocationEntity entity = (LocationEntity) rawObject;

        if (entity.hasHistoricalLocations()) {
            collections.put(HistoricalLocationEntityProvider.ET_HISTORICAL_LOCATION_NAME,
                    entity.getHistoricalLocations()
                            .stream()
                            .map(HistoricalLocationEntity::getIdentifier)
                            .collect(Collectors.toSet()));
        }

        if (entity.hasThings()) {
            collections.put(ThingEntityProvider.ET_THING_NAME,
                    entity.getThings()
                            .stream()
                            .map(PlatformEntity::getIdentifier)
                            .collect(Collectors.toSet()));
        }
        return collections;
    }

}
