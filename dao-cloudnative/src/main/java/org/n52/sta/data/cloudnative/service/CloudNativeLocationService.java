/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.cloudnative.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jooq.Condition;
import org.jooq.Field;
import org.locationtech.jts.io.WKBWriter;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.CollectionWrapper;
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.LocationQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.LocationDao;
import org.n52.sta.data.cloudnative.dao.impl.LocationDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.LocationHistoricalLocationDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.ThingLocationDaoImpl;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Format;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Location;
import org.n52.sta.data.cloudnative.schema.tables.pojos.LocationHistoricalLocation;
import org.n52.sta.data.cloudnative.schema.tables.pojos.PlatformLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.n52.sta.api.RequestUtils.QUERY_OPTIONS_FACTORY;
import static org.n52.sta.data.cloudnative.dao.StaEntityDao.INVALID_EXPAND_OPTION_SUPPLIED;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
public class CloudNativeLocationService
        extends CloudNativeAbstractSensorThingsEntityServiceImpl<
        LocationDao,
        LocationDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudNativeLocationService.class);
    private static final String UNABLE_TO_UPDATE_ENTITY_NOT_FOUND = "Unable to update. Entity not found";

    private final LocationQueryConditions lQC;

    private final boolean updateFOIFeatureEnabled;
    private final AtomicLong TS = new AtomicLong();
    private final CloudNativeFormatService formatService;

    private final LocationDaoImpl locationDao;
    private final LocationHistoricalLocationDaoImpl locationHistoricalLocationDao;
    private final ThingLocationDaoImpl thingLocationDao;


    public CloudNativeLocationService(LocationDaoImpl locationDao,
                                      CloudNativeFormatService formatDao,
                                      LocationHistoricalLocationDaoImpl locationHistoricalLocationDao,
                                      ThingLocationDaoImpl thingLocationDao,
                                      @Value("false") boolean updateFOIFeatureEnabled,
                                      MutexFactory lock, LocationQueryConditions lQC) {
        super(locationDao, LocationDTO.class, lock);
        this.formatService = formatDao;
        this.updateFOIFeatureEnabled = updateFOIFeatureEnabled;
        this.locationDao = locationDao;
        this.locationHistoricalLocationDao = locationHistoricalLocationDao;
        this.thingLocationDao = thingLocationDao;
        this.lQC = lQC;
    }

    @Override
    protected LocationDTO fetchExpandEntitiesWithFilter(LocationDTO entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }
            String expandProperty = expandItem.getPath();
            switch (expandProperty) {
                case STAEntityDefinition.HISTORICAL_LOCATIONS:
                    CollectionWrapper hLocs = getHistoricalLocationService()
                            .getEntityCollectionByRelatedEntity(entity.getId(),
                                    STAEntityDefinition.LOCATIONS,
                                    expandItem.getQueryOptions());
                    entity.setHistoricalLocations((Set<HistoricalLocationDTO>) hLocs
                            .getEntities()
                            .stream()
                            .collect(Collectors.toSet()));
                    break;
                case STAEntityDefinition.THINGS:
                    CollectionWrapper things =
                            getThingService().getEntityCollectionByRelatedEntity(entity.getId(),
                                    STAEntityDefinition.LOCATIONS,
                                    expandItem.getQueryOptions());
                    entity.setThings((Set<ThingDTO>) things
                            .getEntities()
                            .stream()
                            .collect(Collectors.toSet()));
                    break;
                default:
                    throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED, expandProperty,
                            StaConstants.LOCATION));
            }
        }
        return entity;
    }

    @Override
    protected Condition byRelatedEntityFilter(String relatedId, String relatedType, String ownId) {
        Condition filter;
        switch (relatedType) {
            case STAEntityDefinition.HISTORICAL_LOCATIONS: {
                filter = lQC.withHistoricalLocationStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.THINGS: {
                filter = lQC.withThingStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(lQC.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    protected LocationDTO createOrfetch(LocationDTO entity)
            throws STACRUDException, STAInvalidQueryException {
        if (entity.getId() != null && entity.getName() == null) {
            Optional<LocationDTO> optionalEntity =
                    locationDao.findByStaIdentifier(entity.getId(), null, entityClass);
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                        StaConstants.LOCATION,
                        entity.getId()));
            }
        }
        if (entity.getId() == null) {
            if (locationDao.existsByName(entity.getName(), entityClass)) {
                Optional<LocationDTO> optional = locationDao.findByName(entity.getName(), entityClass);
                return optional.orElse(null);
            } else {
                entity.setId(NULL_ID_MASK);
            }
        }
        synchronized (getLock(entity.getId())) {
            if (!Objects.equals(entity.getId(), NULL_ID_MASK) &&
                    locationDao.existsByStaIdentifier(entity.getId(), entityClass)) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
            }
            // overwrite user provided Id
            entity.setId(getUniqueTimestamp().toString());
            if (entity.getProperties() != null) {
                locationDao.saveLocationParameters(entity.getId(), entity.getProperties());
            }
            locationDao.save(POJOWrapper(entity));
            processThings(entity);
        }
        return entity;
    }

    private Location POJOWrapper(LocationDTO location) throws STACRUDException {
        Location locationPOJO = new Location();
        locationPOJO.setStaIdentifier(location.getId());
        locationPOJO.setLocationId(Long.valueOf(location.getId()));
        locationPOJO.setIdentifier(location.getId());
        locationPOJO.setName(location.getName());
        locationPOJO.setDescription(location.getDescription());
        if (location.getGeometry() != null) {
            locationPOJO.setGeom(new WKBWriter().write(location.getGeometry()));
        }
        if (location.getEncodingType() != null) {
            Format formatPOJO = formatService.createOrFetchFormat(location.getEncodingType());
            locationPOJO.setFkFormatId(formatPOJO.getFormatId());
        }
        return locationPOJO;
    }

    private void processThings(LocationDTO location)
            throws STAInvalidQueryException, STACRUDException {
        if (location.getThings() != null) {
            Set<PlatformLocation> thingLocationSet = new HashSet<>();
            for (ThingDTO newThing : location.getThings()) {

                // because Thing table has no links to Location table,
                // we do not have to update Thing entity here and
                // we do not set the locations link in the DTO

                // update PlatformLocation table
                String originalId = newThing.getId();
                ThingDTO savedThing = getThingService().createOrfetch(newThing);
                if(!originalId.equals(savedThing.getId())) {
                    PlatformLocation thingLocation = new PlatformLocation();
                    thingLocation.setFkLocationId(Long.valueOf(location.getId()));
                    thingLocation.setFkPlatformId(Long.valueOf(savedThing.getId()));
                    thingLocationSet.add(thingLocation);
                }

                // non-standard feature 'updateFOI'
                if (updateFOIFeatureEnabled && savedThing.getProperties() != null) {
                    ObjectNode properties = savedThing.getProperties();
                    Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        String fieldName = field.getKey();
                        JsonNode fieldValue = field.getValue();

                        if (fieldName.equals("updateFOI")) {
                            try {
                                LOGGER.debug("Updating FOI with id: " + fieldValue.asText());
                                CloudNativeFeatureOfInterestService foiService = getFeatureOfInterestService();
                                foiService.updateFeatureOfInterestGeometry(fieldValue.asText(),
                                        location.getGeometry());
                            } catch (Exception e) {
                                LOGGER.error("Updating FOI failed as ID could not be extracted from properties!");
                                throw new STACRUDException("Could not extract FeatureOfInterest ID from Thing->properties!");
                            }
                        }
                    }
                }

            }

            thingLocationDao.saveAll(thingLocationSet);
        }

    }

    @Override
    protected LocationDTO updateEntity(String id, LocationDTO entity, HttpMethod method)
            throws STACRUDException, STAInvalidQueryException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<LocationDTO> existing = locationDao
                        .findByStaIdentifier(id, null, entityClass);
                if (existing.isPresent()) {
                    LocationDTO merged = merge(existing.get(), entity);
                    locationDao.update(POJOWrapper(merged));
                    return merged;
                }
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override
    protected LocationDTO createOrUpdate(LocationDTO entity)
            throws STACRUDException, STAInvalidQueryException {
        if (entity.getId() != null && locationDao.existsByStaIdentifier(entity.getId(), entityClass)) {
            return updateEntity(entity.getId(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);

    }

    @Override
    protected void deleteEntity(String id) throws STACRUDException, STAInvalidQueryException {
        synchronized (getLock(id)) {
            if (locationDao.existsByStaIdentifier(id, entityClass)) {
                QueryOptions options = QUERY_OPTIONS_FACTORY
                        .createQueryOptions("$expand=HistoricalLocations");
                LocationDTO location = locationDao
                        .findByStaIdentifier(id, options, entityClass)
                        .get();

                // delete all historical locations
                if (!location.getHistoricalLocations().isEmpty()) {
                    for (HistoricalLocationDTO historicalLocation : location.getHistoricalLocations()) {
                        getHistoricalLocationService().delete(historicalLocation.getId());
                    }
                    // update LocationHistoricalLocation table
                    locationHistoricalLocationDao.deleteByLocationId(Long.parseLong(id));
                }
                // delete parameters
                if (location.getProperties() != null) {
                    locationDao.deleteLocationParameters(Long.valueOf(location.getId()));
                }
                // update PlatformLocation table
                thingLocationDao.deleteByLocationId(Long.parseLong(id));
                // finally delete the Location entity
                locationDao.deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }

    }

    @Override
    protected LocationDTO merge(LocationDTO existing, LocationDTO toMerge) throws STACRUDException {
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        if (toMerge.getGeometry() != null) {
            existing.setGeometry(toMerge.getGeometry());
        }
        // update LocationHistoricalLocation link table
        Set<LocationHistoricalLocation> locationHistoricalLocationSet = new LinkedHashSet<>();
        if (toMerge.getHistoricalLocations() != null) {
            for (HistoricalLocationDTO histLocation : toMerge.getHistoricalLocations()) {
                LocationHistoricalLocation lHloc = new LocationHistoricalLocation();
                lHloc.setFkHistoricalLocationId(Long.valueOf(histLocation.getId()));
                lHloc.setFkLocationId(Long.valueOf(existing.getId()));
                locationHistoricalLocationSet.add(lHloc);
            }
            locationHistoricalLocationDao.saveAll(locationHistoricalLocationSet);
        }
        return existing;

    }

    @Override
    protected String checkPropertyName(String property) {
        Field<?> field = locationDao.checkAliasedPropertyName(property);
        return field == StaEntity.LOCATION.GEOM ? "locationGeom" : field.getName();
    }

    @Override
    protected Field<String> getStaEntityId() {
        return StaEntity.LOCATION.STA_IDENTIFIER;
    }

    @Override
    protected AtomicLong getStaEntityTS() {
        return TS;
    }
}