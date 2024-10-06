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

import org.joda.time.DateTime;
import org.jooq.Condition;
import org.jooq.Field;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.dto.DatastreamDTO;
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.api.dto.impl.HistoricalLocation;
import org.n52.sta.api.dto.impl.Thing;
import org.n52.sta.data.MutexFactory;
import org.n52.sta.data.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.condition.ThingQueryConditions;
import org.n52.sta.data.cloudnative.dao.ThingDao;
import org.n52.sta.data.cloudnative.dao.impl.DatastreamDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.LocationHistoricalLocationDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.ThingDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.ThingLocationDaoImpl;
import org.n52.sta.data.cloudnative.schema.tables.pojos.LocationHistoricalLocation;
import org.n52.sta.data.cloudnative.schema.tables.pojos.Platform;
import org.n52.sta.data.cloudnative.schema.tables.pojos.PlatformLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
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
public class CloudNativeThingService
        extends CloudNativeAbstractSensorThingsEntityServiceImpl<
        ThingDao,
        ThingDTO> {

    private static ThingQueryConditions tQC = new ThingQueryConditions();
    private static DatastreamQueryConditions dsQC = new DatastreamQueryConditions();
    private static final Logger logger = LoggerFactory.getLogger(CloudNativeThingService.class);
    private final ThingDaoImpl thingDao;
    private final ThingLocationDaoImpl thingLocationDao;
    private final LocationHistoricalLocationDaoImpl locationHistoricalLocationDao;
    private final DatastreamDaoImpl datastreamDao;
    private final AtomicLong TS = new AtomicLong();

    public CloudNativeThingService(ThingDaoImpl thingDao,
                                   ThingLocationDaoImpl thingLocationDao,
                                   LocationHistoricalLocationDaoImpl locationHistoricalLocationDao,
                                   DatastreamDaoImpl datastreamDao,
                                   MutexFactory lock) {
        super(thingDao, ThingDTO.class, lock);
        this.thingDao = thingDao;
        this.thingLocationDao = thingLocationDao;
        this.locationHistoricalLocationDao = locationHistoricalLocationDao;
        this.datastreamDao = datastreamDao;
    }

    public static void setDatastreamQueryConditions(DatastreamQueryConditions dsQC) {
        CloudNativeThingService.dsQC = dsQC;
    }
    public static void setThingQueryConditions (ThingQueryConditions tQC) {
        CloudNativeThingService.tQC = tQC;
    }
    @Override
    protected Condition byRelatedEntityFilter(String relatedId, String relatedType, String ownId) {
        Condition filter;
        switch (relatedType) {
            case STAEntityDefinition.HISTORICAL_LOCATIONS: {
                filter = tQC.withHistoricalLocationStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.DATASTREAMS: {
                filter = tQC.withDatastreamStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.LOCATIONS: {
                filter = tQC.withLocationStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(tQC.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    protected ThingDTO fetchExpandEntitiesWithFilter(ThingDTO entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() ||
                    expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }
            String expandProperty = expandItem.getPath();
            switch (expandProperty) {
                case STAEntityDefinition.HISTORICAL_LOCATIONS:
                    Page<HistoricalLocationDTO> hLocs = getHistoricalLocationService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getId(),
                                    STAEntityDefinition.THINGS,
                                    expandItem.getQueryOptions());
                    entity.setHistoricalLocations(hLocs.get().collect(Collectors.toSet()));
                    break;
                case STAEntityDefinition.DATASTREAMS:
                    Page<DatastreamDTO> datastreams = getDatastreamService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getId(),
                                    STAEntityDefinition.THINGS,
                                    expandItem.getQueryOptions());
                    entity.setDatastreams(datastreams.get().collect(Collectors.toSet()));
                    break;
                case STAEntityDefinition.LOCATIONS:
                    Page<LocationDTO> locations = getLocationService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getId(),
                                    STAEntityDefinition.THINGS,
                                    expandItem.getQueryOptions());
                    entity.setLocations(locations.get().collect(Collectors.toSet()));
                    break;
                default:
                    throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                            expandProperty,
                            StaConstants.THING));
            }
        }
        return entity;
    }

    @Override
    protected ThingDTO createOrfetch(ThingDTO entity)
            throws STACRUDException, STAInvalidQueryException {
        if (entity.getId() != null && entity.getName() == null) {
            Optional<ThingDTO> optionalEntity =
                    thingDao.findByStaIdentifier(entity.getId(), null, entityClass);
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                        StaConstants.THING,
                        entity.getId()));
            }
        }
        if (entity.getId() == null) {
            if (thingDao.existsByName(entity.getName(), entityClass)) {
                return thingDao.findByName(entity.getName(), entityClass).orElse(null);
            }
            else {
                entity.setId(NULL_ID_MASK);
            }
        }
        synchronized (getLock(entity.getId())) {
            if (!Objects.equals(entity.getId(), NULL_ID_MASK) &&
                    thingDao.existsByStaIdentifier(entity.getId(), entityClass)) {
                throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
            } else {
                entity.setId(getUniqueTimestamp().toString());
                if (entity.getProperties() != null) {
                    thingDao.saveThingParameters(entity.getId(), entity.getProperties());
                }
                processDatastreams(entity);
                boolean locationChanged = processLocations(entity, entity.getLocations());
                boolean hasUnpersistedHLocs = entity.getHistoricalLocations() != null &&
                        entity.getHistoricalLocations().stream().anyMatch(p -> p.getId() == null);
                if (locationChanged || hasUnpersistedHLocs) {
                    generateHistoricalLocation(entity);
                }
                thingDao.save(POJOWrapper(entity));
            }
        }

        return entity;
    }

    private Platform POJOWrapper(ThingDTO thing) {
        Platform thingPOJO = new Platform();
        thingPOJO.setPlatformId(Long.valueOf(thing.getId()));
        thingPOJO.setStaIdentifier(thing.getId());
        thingPOJO.setIdentifier(thing.getId());
        thingPOJO.setName(thing.getName());
        thingPOJO.setDescription(thing.getDescription());
        return thingPOJO;
    }

    private void generateHistoricalLocation(ThingDTO thing)
            throws STACRUDException, STAInvalidQueryException {
        if (thing == null) {
            throw new STACRUDException("Error processing HistoricalLocations. Thing does not exist!");
        }
        Set<HistoricalLocationDTO> persistedHistoricalLocations =  new HashSet<>();
        // Persist nested HistoricalLocations
        if (thing.getHistoricalLocations() != null) {
            //thing.setHistoricalLocations(null);
            for (HistoricalLocationDTO historicalLocation : thing.getHistoricalLocations()) {
                // Check if historicalLocation is not already persisted
                if (historicalLocation.getId() == null) {
                    // avoid persisting Thing again
                    ThingDTO relatedThing = new Thing();
                    relatedThing.setDescription("AUTOGENERATED");
                    relatedThing.setId(thing.getId());
                    historicalLocation.setThing(relatedThing);
                    // avoid persisting Location again
                    historicalLocation.setLocations(new HashSet<>());
                    persistedHistoricalLocations.add(getHistoricalLocationService().createOrUpdate(historicalLocation));
                }
            }
        }

        // Create new HistoricalLocation based on current location
        if (thing.getLocations() != null) {
            Set<LocationHistoricalLocation> locationHistLocationTable = new HashSet<>();

            HistoricalLocationDTO historicalLocation = new HistoricalLocation();
            historicalLocation.setId(getUniqueTimestamp().toString());
            historicalLocation.setTime(new TimeInstant(DateTime.now()));
            // avoid persisting Thing again
            ThingDTO relatedThing = new Thing();
            relatedThing.setId(thing.getId());
            relatedThing.setDescription("AUTOGENERATED");
            historicalLocation.setThing(relatedThing);
            // avoid persisting Location again
            historicalLocation.setLocations(new HashSet<>());
            HistoricalLocationDTO createdHistoricalLocation =
                    getHistoricalLocationService().createOrUpdate(historicalLocation);
            if (createdHistoricalLocation != null) {
                persistedHistoricalLocations.add(createdHistoricalLocation);
            }
            for (LocationDTO location : thing.getLocations()) {
                // update LocationHistoricalLocation link table here
                LocationHistoricalLocation locationHistLocationRecord = new LocationHistoricalLocation();
                locationHistLocationRecord.setFkHistoricalLocationId(Long.valueOf(createdHistoricalLocation.getId()));
                locationHistLocationRecord.setFkLocationId(Long.valueOf(location.getId()));
                locationHistLocationTable.add(locationHistLocationRecord);
            }
            locationHistoricalLocationDao.saveAll(locationHistLocationTable);
        }
        thing.setHistoricalLocations(persistedHistoricalLocations);
    }

    private void processDatastreams(ThingDTO thing)
            throws STAInvalidQueryException, STACRUDException {
        if (thing.getDatastreams() != null) {
            Set<DatastreamDTO> datastreams = new HashSet<>();
            for (DatastreamDTO datastream : thing.getDatastreams()) {
                datastream.setThing(thing);
                datastreams.add(getDatastreamService().createOrfetch(datastream));
            }
            thing.setDatastreams(datastreams);
        }
    }

    private boolean processLocations(ThingDTO thing, Set<LocationDTO> nestedLocations)
            throws STAInvalidQueryException, STACRUDException {
        boolean didPersist = false;
        if (nestedLocations != null) {
            Set<LocationDTO> savedLocations = new HashSet<>();
            Set<PlatformLocation> thingLocationTable = new HashSet<>();
            thing.setLocations(new HashSet<>());
            for (LocationDTO location : nestedLocations) {
                String originalId = location.getId();
                location.setThings(null);
                LocationDTO savedLocation = getLocationService().createOrfetch(location);

                PlatformLocation thingLocationRecord = new PlatformLocation();
                thingLocationRecord.setFkLocationId(Long.valueOf(savedLocation.getId()));
                thingLocationRecord.setFkPlatformId(Long.valueOf(thing.getId()));
                thingLocationTable.add(thingLocationRecord);

                savedLocations.add(savedLocation);
                if (!originalId.equals(savedLocation.getId())) {
                    didPersist = true;
                }
            }
            thing.setLocations(savedLocations);
            thingLocationDao.saveAll(thingLocationTable);
        }
        return didPersist;
    }

    @Override
    protected ThingDTO createOrUpdate(ThingDTO entity) throws STACRUDException, STAInvalidQueryException {
        if (entity.getId() != null && thingDao.existsByStaIdentifier(entity.getId(), entityClass)) {
            return updateEntity(entity.getId(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override
    protected ThingDTO updateEntity(String id, ThingDTO newEntity, HttpMethod method)
            throws STACRUDException, STAInvalidQueryException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                QueryOptions options = QUERY_OPTIONS_FACTORY
                        .createQueryOptions("$expand=Locations,HistoricalLocations");
                Optional<ThingDTO> existing =
                        thingDao.findByStaIdentifier(id, options, entityClass);
                if (existing.isPresent()) {
                    ThingDTO merged = merge(existing.get(), newEntity);
                    if (newEntity.getLocations() != null) {
                        boolean changedLocations = processLocations(merged, newEntity.getLocations());
                        if (changedLocations) {
                            generateHistoricalLocation(merged);
                        }
                    }
                    thingDao.update(POJOWrapper(merged));
                    return merged;
                } else {
                    throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
                }
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
    }

    @Override
    protected void deleteEntity(String identifier) throws STACRUDException, STAInvalidQueryException {
        synchronized (getLock(identifier)) {
            if (thingDao.existsByStaIdentifier(identifier, entityClass)) {
                QueryOptions options = QUERY_OPTIONS_FACTORY
                        .createQueryOptions("$expand=HistoricalLocations");
                ThingDTO thing = thingDao.findByStaIdentifier(identifier, options, entityClass).get();
                // delete datastreams
                Condition predicate = dsQC.withThingStaIdentifier(identifier)
                        .and(StaEntity.DATASTREAM.STA_IDENTIFIER.isNotNull());
                List<DatastreamDTO> relatedDatastreams = datastreamDao.findAll(
                        predicate,
                        null,
                        DatastreamDTO.class);
                for (DatastreamDTO ds : relatedDatastreams) {
                        getDatastreamService().delete(ds.getId());
                }
                // delete historicalLocation
                for (HistoricalLocationDTO hloc : thing.getHistoricalLocations()) {
                    getHistoricalLocationService().delete(hloc.getId());
                }

                // delete ThingLocation links
                thingLocationDao.deleteByThingId(Long.parseLong(identifier));
                // delete properties
                if (thing.getProperties() != null) {
                    thingDao.deleteThingParameters(Long.valueOf(thing.getId()));
                }
                thingDao.deleteByStaIdentifier(identifier);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }

    }

    @Override
    protected ThingDTO merge(ThingDTO existing, ThingDTO toMerge) throws STACRUDException {
        if (existing.equals(toMerge)) {
            return existing;
        }
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        // properties
        if (toMerge.getProperties() != null) {
            synchronized (getLock(String.valueOf(existing.getProperties().hashCode()))) {
                thingDao.saveThingParameters(existing.getId(), toMerge.getProperties());
                existing.setProperties(toMerge.getProperties());
            }
        }
        return existing;
    }

    @Override
    protected String checkPropertyName(String property) {
        return thingDao.checkPropertyName(property).getName();
    }

    @Override
    protected Field<String> getStaEntityId() {
        return StaEntity.THING.STA_IDENTIFIER;
    }

    @Override
    protected AtomicLong getStaEntityTS() {
        return TS;
    }
}
