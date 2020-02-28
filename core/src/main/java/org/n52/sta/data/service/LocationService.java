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
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.LocationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.LocationQuerySpecifications;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.LocationEncodingRepository;
import org.n52.sta.data.repositories.LocationRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class LocationService
        extends AbstractSensorThingsEntityService<LocationRepository, LocationEntity, LocationEntity> {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private static final LocationQuerySpecifications lQS = new LocationQuerySpecifications();
    private static final String UNABLE_TO_UPDATE_ENTITY_NOT_FOUND = "Unable to update. Entity not found";

    private final LocationEncodingRepository locationEncodingRepository;

    private final boolean updateFOIFeatureEnabled;
    private Pattern updateFOIPattern = Pattern.compile("(?:.*updateFOI\":\")([0-9A-z'+%-]+)(?:\".*)");

    public LocationService(@Value("${server.feature.updateFOI}") boolean updateFOI,
                           LocationRepository repository,
                           LocationEncodingRepository locationEncodingRepository) {
        super(repository, LocationEntity.class);
        this.locationEncodingRepository = locationEncodingRepository;
        this.updateFOIFeatureEnabled = updateFOI;
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.Location, EntityTypes.Locations};
    }

    @Override protected LocationEntity fetchExpandEntities(LocationEntity entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (LocationEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                CollectionWrapper expandedEntities;
                switch (expandProperty) {
                case STAEntityDefinition.HISTORICAL_LOCATIONS:
                    expandedEntities =
                            getHistoricalLocationService()
                                    .getEntityCollectionByRelatedEntity(entity.getIdentifier(),
                                                                        LocationEntity.class.getSimpleName(),
                                                                        expandItem.getQueryOptions());
                    entity.setHistoricalLocations(
                            expandedEntities.getEntities()
                                            .stream()
                                            .map(s -> (HistoricalLocationEntity) s.getEntity())
                                            .collect(Collectors.toSet()));
                    break;
                case STAEntityDefinition.THINGS:
                    expandedEntities = getThingService()
                            .getEntityCollectionByRelatedEntity(entity.getIdentifier(),
                                                                LocationEntity.class.getSimpleName(),
                                                                expandItem.getQueryOptions());
                    entity.setThings(
                            expandedEntities.getEntities()
                                            .stream()
                                            .map(s -> (PlatformEntity) s.getEntity())
                                            .collect(Collectors.toSet()));
                    break;
                }
            } else {
                throw new STAInvalidQueryException("Invalid expandOption supplied. Cannot find " + expandProperty +
                                                           "on Entity of type 'Thing'");
            }
        }
        return entity;
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
                Optional<LocationEntity> optionalEntity =
                        getRepository().findByIdentifier(location.getIdentifier());
                if (optionalEntity.isPresent()) {
                    return optionalEntity.get();
                } else {
                    throw new STACRUDException("No Location with id '" + location.getIdentifier() + "' found");
                }
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
                throw new STACRUDException("Identifier already exists!", HTTPStatus.BAD_REQUEST);
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
                LocationEntity merged = merge(existing.get(), entity);
                return getRepository().save(merged);
            }
            throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HTTPStatus.BAD_REQUEST);
    }

    @Override
    public LocationEntity updateEntity(LocationEntity entity) {
        return getRepository().save(entity);
    }

    @Override
    public void delete(String id) throws STACRUDException {
        if (getRepository().existsByIdentifier(id)) {
            LocationEntity location =
                    getRepository().findByIdentifier(id,
                                                     EntityGraphRepository.FetchGraph.FETCHGRAPH_HIST_LOCATION,
                                                     EntityGraphRepository.FetchGraph.FETCHGRAPH_THINGSHISTLOCATION)
                                   .get();
            for (PlatformEntity thing : location.getThings()) {
                thing.setLocations(null);
                if (location.getHistoricalLocations() != null) {
                    thing.getHistoricalLocations().removeAll(location.getHistoricalLocations());
                }
                getThingService().updateEntity(thing);
            }
            // delete all historical locations
            for (HistoricalLocationEntity historicalLocation : location.getHistoricalLocations()) {
                getHistoricalLocationService().delete(historicalLocation);
            }
            getRepository().deleteByIdentifier(id);
        } else {
            throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
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
            for (PlatformEntity newThing : location.getThings()) {
                HashSet<LocationEntity> set = new HashSet<>();
                set.add(createReferencedLocation(location));
                newThing.setLocations(set);
                PlatformEntity updated = (PlatformEntity) getThingService().createOrUpdate(newThing);
                things.add(updated);

                // non-standard feature 'updateFOI'
                if (updateFOIFeatureEnabled
                        && updated.getProperties() != null
                        && updated.getProperties().contains("updateFOI")) {
                    // Try to be more performant and not deserialize whole properties but only grep relevant parts
                    // via simple regex
                    Matcher matcher = updateFOIPattern.matcher(updated.getProperties());
                    if (matcher.matches()) {
                        FeatureOfInterestService foiService = (FeatureOfInterestService) getFeatureOfInterestService();
                        foiService.updateFeatureOfInterestGeometry(matcher.group(1), location.getGeometry());
                    } else {
                        throw new STACRUDException("Could not extract FeatureOfInterest ID from Thing->properties!");
                    }
                }
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

    /* (non-Javadoc)
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    @Override
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        LocationEntity entity = (LocationEntity) rawObject;

        if (entity.hasHistoricalLocations()) {
            collections.put(STAEntityDefinition.HISTORICAL_LOCATION,
                            entity.getHistoricalLocations()
                                  .stream()
                                  .map(HistoricalLocationEntity::getIdentifier)
                                  .collect(Collectors.toSet()));
        }

        if (entity.hasThings()) {
            collections.put(STAEntityDefinition.THING,
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
        if (toMerge.hasHistoricalLocations()) {
            existing.setHistoricalLocations(toMerge.getHistoricalLocations());
        }
        if (toMerge.isSetGeometry()) {
            existing.setGeometryEntity(toMerge.getGeometryEntity());
        }
        return existing;
    }
}
