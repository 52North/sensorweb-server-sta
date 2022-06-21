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

import org.hibernate.Hibernate;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.TextParameterEntity;
import org.n52.series.db.beans.parameter.location.LocationParameterEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.LocationQuerySpecifications;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.LocationEncodingRepository;
import org.n52.sta.data.repositories.LocationParameterRepository;
import org.n52.sta.data.repositories.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
public class LocationService
    extends AbstractSensorThingsEntityServiceImpl<LocationRepository, LocationEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

    private static final LocationQuerySpecifications lQS = new LocationQuerySpecifications();

    private static final String UNABLE_TO_UPDATE_ENTITY_NOT_FOUND = "Unable to update. Entity not found";

    private final LocationEncodingRepository locationEncodingRepository;

    private final boolean updateFOIFeatureEnabled;
    private final LocationParameterRepository parameterRepository;

    public LocationService(@Value("${server.feature.updateFOI:false}") boolean updateFOI,
                           LocationRepository repository,
                           LocationEncodingRepository locationEncodingRepository,
                           LocationParameterRepository parameterRepository,
                           EntityManager em) {
        super(repository, em, LocationEntity.class);
        this.locationEncodingRepository = locationEncodingRepository;
        this.updateFOIFeatureEnabled = updateFOI;
        this.parameterRepository = parameterRepository;
    }

    @Override protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
        throws STAInvalidQueryException {
        Set<EntityGraphRepository.FetchGraph> fetchGraphs = new HashSet<>(6);
        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_PARAMETERS);
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.HISTORICAL_LOCATIONS:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_HIST_LOCATIONS);
                        break;
                    case STAEntityDefinition.THINGS:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_PLATFORMS);
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED, expandProperty,
                                                                         StaConstants.LOCATION));
                }
            }
        }
        return fetchGraphs.toArray(new EntityGraphRepository.FetchGraph[0]);
    }

    @Override
    protected LocationEntity fetchExpandEntitiesWithFilter(LocationEntity entity, ExpandFilter expandOption)
        throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }
            String expandProperty = expandItem.getPath();
            switch (expandProperty) {
                case STAEntityDefinition.HISTORICAL_LOCATIONS:
                    Page<HistoricalLocationEntity> hLocs = getHistoricalLocationService()
                        .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                               STAEntityDefinition.LOCATIONS,
                                                               expandItem.getQueryOptions());
                    entity.setHistoricalLocations(hLocs.get().collect(Collectors.toSet()));
                    break;
                case STAEntityDefinition.THINGS:
                    Page<PlatformEntity> things =
                        getThingService().getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                                                STAEntityDefinition.LOCATIONS,
                                                                                expandItem.getQueryOptions());
                    entity.setThings(things.get().collect(Collectors.toSet()));
                    break;
                default:
                    throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED, expandProperty,
                                                                     StaConstants.LOCATION));
            }
        }
        return entity;
    }

    @Override
    public Specification<LocationEntity> byRelatedEntityFilter(String relatedId, String relatedType, String ownId) {
        Specification<LocationEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.HISTORICAL_LOCATIONS: {
                filter = lQS.withHistoricalLocationStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.THINGS: {
                filter = lQS.withThingStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(lQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public LocationEntity createOrfetch(LocationEntity newLocation) throws STACRUDException {
        LocationEntity location = newLocation;
        if (!location.isProcessed()) {
            if (location.getStaIdentifier() != null && !location.isSetName()) {
                Optional<LocationEntity> optionalEntity =
                    getRepository().findByStaIdentifier(location.getStaIdentifier());
                if (optionalEntity.isPresent()) {
                    return optionalEntity.get();
                } else {
                    throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                             StaConstants.LOCATION,
                                                             location.getStaIdentifier()));
                }
            }
            if (location.getStaIdentifier() == null) {
                if (getRepository().existsByName(location.getName())) {
                    Optional<LocationEntity> optional = getRepository().findByName(location.getName());
                    return optional.orElse(null);
                } else {
                    // Autogenerate Identifier
                    String uuid = UUID.randomUUID().toString();
                    location.setIdentifier(uuid);
                    location.setStaIdentifier(uuid);
                }
            }
            synchronized (getLock(location.getStaIdentifier())) {
                if (getRepository().existsByStaIdentifier(location.getStaIdentifier())) {
                    throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
                }
                location.setProcessed(true);
                checkLocationEncoding(location);
                processThings(location);
                location = getRepository().save(location);
            }
        }
        return location;
    }

    @Override
    public LocationEntity updateEntity(String id, LocationEntity entity, HttpMethod method) throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<LocationEntity> existing = getRepository()
                    .findByStaIdentifier(id,
                                         EntityGraphRepository.FetchGraph.FETCHGRAPH_HIST_LOCATIONS,
                                         EntityGraphRepository.FetchGraph.FETCHGRAPH_PLATFORMS);
                if (existing.isPresent()) {
                    LocationEntity merged = merge(existing.get(), entity);
                    LocationEntity result = getRepository().save(merged);
                    Hibernate.initialize(result.getParameters());
                    return result;
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override
    public LocationEntity createOrUpdate(LocationEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override
    public String checkPropertyName(String property) {
        return lQS.checkPropertyName(property);
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

    @Override
    public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                LocationEntity location = getRepository()
                    .findByStaIdentifier(id,
                                         EntityGraphRepository.FetchGraph.FETCHGRAPH_HIST_LOCATIONS,
                                         EntityGraphRepository.FetchGraph.FETCHGRAPH_PLATFORMSHISTLOCATION)
                    .get();
                for (PlatformEntity thing : location.getThings()) {
                    thing.setLocations(null);
                    if (location.getHistoricalLocations() != null) {
                        thing.getHistoricalLocations().removeAll(location.getHistoricalLocations());
                    }
                    getThingService().save(thing);
                }
                // delete all historical locations
                for (HistoricalLocationEntity historicalLocation : location.getHistoricalLocations()) {
                    getHistoricalLocationService().delete(historicalLocation.getStaIdentifier());
                }

                if (location.hasParameters()) {
                    location.getParameters()
                        .forEach(entity -> parameterRepository.delete((LocationParameterEntity) entity));
                }
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    private void checkLocationEncoding(LocationEntity location) throws STACRUDException {
        if (location.getLocationEncoding() != null) {
            FormatEntity optionalLocationEncoding = createLocationEncoding(location.getLocationEncoding());
            location.setLocationEncoding(optionalLocationEncoding);
        }
    }

    private FormatEntity createLocationEncoding(FormatEntity locationEncoding) throws STACRUDException {
        ExampleMatcher createEncodingTypeMatcher = createEncodingTypeMatcher();
        synchronized (getLock(locationEncoding.getFormat())) {
            if (!locationEncodingRepository
                .exists(createEncodingTypeExample(locationEncoding, createEncodingTypeMatcher))) {
                return locationEncodingRepository.save(locationEncoding);
            }
            return locationEncodingRepository
                .findOne(createEncodingTypeExample(locationEncoding, createEncodingTypeMatcher)).get();
        }
    }

    private Example<FormatEntity> createEncodingTypeExample(FormatEntity locationEncoding, ExampleMatcher matcher) {
        return Example.of(locationEncoding, matcher);
    }

    private ExampleMatcher createEncodingTypeMatcher() {
        return ExampleMatcher.matching().withMatcher(ENCODINGTYPE, GenericPropertyMatchers.ignoreCase());
    }

    private void processThings(LocationEntity location) throws STACRUDException {
        if (location.hasThings()) {
            Set<PlatformEntity> things = new LinkedHashSet<>();
            for (PlatformEntity newThing : location.getThings()) {
                // The only way for a Thing to be processed is if we are currently persisting said Thing
                // IF this is the case the Thing takes care of Locations itself and we must not mess with it here
                if (!newThing.isProcessed()) {
                    HashSet<LocationEntity> set = new HashSet<>();
                    set.add(createReferencedLocation(location));
                    newThing.setLocations(set);
                    PlatformEntity updated = getThingService().createOrUpdate(newThing);
                    things.add(updated);

                    // non-standard feature 'updateFOI'
                    if (updateFOIFeatureEnabled && updated.getParameters() != null) {
                        // Try to be more performant and not deserialize whole
                        // properties but only grep relevant parts
                        // via simple regex
                        for (ParameterEntity<?> parameter : updated.getParameters()) {
                            if (parameter instanceof TextParameterEntity &&
                                parameter.getName().equals("updateFOI")) {
                                try {
                                    LOGGER.debug("Updating FOI with id: " + parameter.getValueAsString());
                                    FeatureOfInterestService foiService = getFeatureOfInterestService();
                                    foiService.updateFeatureOfInterestGeometry(parameter.getValueAsString(),
                                                                               location.getGeometry());
                                } catch (Exception e) {
                                    LOGGER.error("Updating FOI failed as ID could not be extracted from properties!");
                                    throw new STACRUDException("Could not extract FeatureOfInterest ID from " +
                                                                   "Thing->properties!");
                                }
                            }
                        }
                    }
                    location.setThings(things);
                }
            }
        }
    }

    private LocationEntity createReferencedLocation(LocationEntity location) {
        LocationEntity referenced = new LocationEntity();
        referenced.setStaIdentifier(location.getStaIdentifier());
        referenced.setId(location.getId());
        return referenced;
    }
}
