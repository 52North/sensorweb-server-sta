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

import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.query.LocationQuerySpecifications;
import org.n52.sta.data.repositories.LocationEncodingRepository;
import org.n52.sta.data.repositories.LocationRepository;
import org.n52.sta.serdes.model.ElementWithQueryOptions;
import org.n52.sta.serdes.model.ElementWithQueryOptions.LocationWithQueryOptions;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.edm.provider.entities.HistoricalLocationEntityProvider;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.edm.provider.entities.ThingEntityProvider;
import org.n52.sta.exception.STACRUDException;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

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
public class LocationService extends AbstractSensorThingsEntityService<LocationRepository, LocationEntity> {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private static final LocationQuerySpecifications lQS = new LocationQuerySpecifications();
    private static final String UNABLE_TO_UPDATE_ENTITY_NOT_FOUND = "Unable to update. Entity not found";

    private final LocationEncodingRepository locationEncodingRepository;

    private LocationMapper mapper;

    @Autowired
    public LocationService(LocationRepository repository,
                           LocationMapper mapper,
                           LocationEncodingRepository locationEncodingRepository) {
        super(repository, LocationEntity.class);
        this.mapper = mapper;
        this.locationEncodingRepository = locationEncodingRepository;
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.Location, EntityTypes.Locations};
    }

    @Override
    protected ElementWithQueryOptions createWrapper(Object entity, QueryOptions queryOptions) {
        return new LocationWithQueryOptions((LocationEntity) entity, queryOptions);
    }

    @Override
    public Specification<LocationEntity> byRelatedEntityFilter(String relatedId,
                                                               String relatedType,
                                                               String ownId) {
        Specification<LocationEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.HISTORICAL_LOCATIONS: {
                filter = lQS.withRelatedHistoricalLocationIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.THINGS: {
                filter = lQS.withRelatedThingIdentifier(relatedId);
                break;
            }
            default:
                return null;
        }

        if (ownId != null) {
            filter = filter.and(lQS.withIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public LocationEntity createEntity(LocationEntity newLocation) throws STACRUDException {
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
                throw new STACRUDException("Identifier already exists!", HttpStatus.BAD_REQUEST);
            }
            location.setProcesssed(true);
            checkLocationEncoding(location);
            location = getRepository().save(location);
            processThings(location);
        }
        return location;
    }

    @Override
    public LocationEntity updateEntity(String id, LocationEntity entity, HttpMethod method) throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            Optional<LocationEntity> existing = getRepository().findByIdentifier(id);
            if (existing.isPresent()) {
                LocationEntity merged = mapper.merge(existing.get(), entity);
                return getRepository().save(merged);
            }
            throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HttpStatus.NOT_FOUND);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HttpStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HttpStatus.BAD_REQUEST);
    }

    @Override
    public LocationEntity updateEntity(LocationEntity entity) {
        return getRepository().save(entity);
    }

    @Override
    public void delete(String id) throws STACRUDException {
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
                getThingService().updateEntity(thing);
            }
            getRepository().deleteByIdentifier(id);
        } else {
            throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void delete(LocationEntity entity) {
        getRepository().deleteByIdentifier(entity.getIdentifier());
    }

    @Override
    protected LocationEntity createOrUpdate(LocationEntity entity) throws STACRUDException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return updateEntity(entity.getIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
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
        return Example.of(locationEncoding, matcher);
    }

    private ExampleMatcher createEncodingTypeMatcher() {
        return ExampleMatcher.matching().withMatcher(ENCODINGTYPE, GenericPropertyMatchers.ignoreCase());
    }

    private void processThings(LocationEntity location) throws STACRUDException {
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

    @Override
    protected LocationEntity merge(LocationEntity existing, LocationEntity toMerge) {
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        if (toMerge.hasLocation()) {
            existing.setLocation(toMerge.getLocation());
        }
        if (toMerge.isSetGeometry()) {
            existing.setGeometryEntity(toMerge.getGeometryEntity());
        }
        return existing;
    }
}
