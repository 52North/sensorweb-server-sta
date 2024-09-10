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
import org.n52.sta.api.dto.HistoricalLocationDTO;
import org.n52.sta.api.dto.LocationDTO;
import org.n52.sta.api.dto.SensorDTO;
import org.n52.sta.api.dto.ThingDTO;
import org.n52.sta.data.cloudnative.condition.HistoricalLocationQueryConditions;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.dao.HistoricalLocationDao;
import org.n52.sta.data.cloudnative.dao.impl.HistoricalLocationDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.LocationDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.LocationHistoricalLocationDaoImpl;
import org.n52.sta.data.cloudnative.dao.impl.ThingDaoImpl;
import org.n52.sta.data.cloudnative.schema.tables.pojos.HistoricalLocation;
import org.n52.sta.data.cloudnative.schema.tables.pojos.LocationHistoricalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.n52.sta.api.RequestUtils.QUERY_OPTIONS_FACTORY;
import static org.n52.sta.data.cloudnative.dao.StaEntityDao.INVALID_EXPAND_OPTION_SUPPLIED;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class HistoricalLocationService
        extends AbstractSensorThingsEntityServiceImpl<
        HistoricalLocationDao,
        HistoricalLocationDTO> {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalLocationService.class);

    private static final HistoricalLocationQueryConditions hlQS = new HistoricalLocationQueryConditions();

    private final LocationDaoImpl locationDao;
    private final HistoricalLocationDaoImpl historicalLocationDao;
    private final LocationHistoricalLocationDaoImpl locationHistoricalLocationDao;

    private final AtomicLong TS = new AtomicLong();

    public HistoricalLocationService(HistoricalLocationDaoImpl historicalLocationDao,
                                     LocationDaoImpl locationDao,
                                     LocationHistoricalLocationDaoImpl locationHistoricalLocationDao,
                                     Class<HistoricalLocationDTO> entityClass) {
        super(historicalLocationDao, entityClass);
        this.locationDao = locationDao;
        this.historicalLocationDao = historicalLocationDao;
        this.locationHistoricalLocationDao = locationHistoricalLocationDao;
    }

    @Override
    protected HistoricalLocationDTO fetchExpandEntitiesWithFilter(HistoricalLocationDTO entity,
                                                                  ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            // We have already handled $expand without filter and expand
            if (!(expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter())) {
                continue;
            }
            String expandProperty = expandItem.getPath();
            switch (expandProperty) {
                case STAEntityDefinition.LOCATIONS:
                    Page<LocationDTO> locations = getLocationService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getId(),
                                    STAEntityDefinition.HISTORICAL_LOCATIONS,
                                    expandItem.getQueryOptions());
                    entity.setLocations(locations.get().collect(Collectors.toSet()));
                    break;
                case STAEntityDefinition.THING:
                    // fallthru
                    // The UML in Section 8.2 of the OGC STA v1.0 defines the relations as "Things"
                    // The Definition in Section 8.2.3 of the OGC STA v1.0 defines the relations as "Thing"
                    // We will allow both for now
                case STAEntityDefinition.THINGS:
                    entity.setThing(getThingService()
                            .getEntityByIdRaw(Long.valueOf(entity.getThing().getId()),
                                    expandItem.getQueryOptions()));
                    break;
                default:
                    throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                            expandProperty,
                            StaConstants.HISTORICAL_LOCATION));
            }
        }
        return entity;
    }

    @Override
    protected Condition byRelatedEntityFilter(String relatedId, String relatedType, String ownId)
            throws STAInvalidQueryException {
        Condition filter;
        switch (relatedType) {
            case STAEntityDefinition.LOCATIONS: {
                filter = hlQS.withLocationStaIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.THINGS: {
                filter = hlQS.withThingStaIdentifier(relatedId);
                break;
            }
            default:
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(hlQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    protected HistoricalLocationDTO createOrfetch(HistoricalLocationDTO entity)
            throws STACRUDException, STAInvalidQueryException {

        if (entity.getId() != null) {
            Optional<HistoricalLocationDTO> optionalEntity =
                    historicalLocationDao.findByStaIdentifier(entity.getId(),null, entityClass);
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                        StaConstants.SENSOR,
                        entity.getId()));
            }
        }
        if (entity.getId() == null) {
            entity.setId(NULL_ID_MASK);
        }

        synchronized (getLock(entity.getId())) {
            // overwrite the sta_identifier
            entity.setId(getUniqueTimestamp().toString());
            check(entity);
            HistoricalLocationDTO created = processThing(entity);
            processLocations(created);
            historicalLocationDao.save(POJOWrapper(entity));
        }
        return entity;
    }

    private HistoricalLocation POJOWrapper(HistoricalLocationDTO entity) {
        HistoricalLocation hLocPOJO = new HistoricalLocation();
        hLocPOJO.setHistoricalLocationId(Long.valueOf(entity.getId()));
        hLocPOJO.setIdentifier(entity.getId());
        hLocPOJO.setStaIdentifier(entity.getId());
        LocalDateTime time = ((TimeInstant) entity.getTime()).getValue()
                .toDate()
                .toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
        hLocPOJO.setTime(time);
        hLocPOJO.setFkPlatformId(Long.valueOf(entity.getThing().getId()));
        return hLocPOJO;
    }
    private void check(HistoricalLocationDTO historicalLocation) throws STACRUDException {
        if (historicalLocation.getThing() == null && historicalLocation.getLocations() != null) {
            throw new STACRUDException("The HistoricalLocation to create is invalid", HTTPStatus.BAD_REQUEST);
        }
    }
    private HistoricalLocationDTO processThing(HistoricalLocationDTO historicalLocation)
            throws STAInvalidQueryException, STACRUDException {

        ThingDTO thing = getThingService().createOrfetch(historicalLocation.getThing());
        historicalLocation.setThing(thing);
        return historicalLocation;
    }

    private void processLocations(HistoricalLocationDTO historicalLocation)
            throws STAInvalidQueryException, STACRUDException {
        Set<LocationHistoricalLocation> locationHistoricalLocations = new LinkedHashSet<>();
        for (LocationDTO l : historicalLocation.getLocations()) {
            Optional<LocationDTO> location =
                    locationDao.findByStaIdentifier(l.getId(), null, LocationDTO.class);
            if (location.isPresent()) {
                location.get().addHistoricalLocation(historicalLocation);
                // because the location<->historical location link is invisible to location table
                // we do not update the location table
                // and update the LocationHistoricalLocation table instead
                LocationHistoricalLocation locHloc = new LocationHistoricalLocation();
                locHloc.setFkHistoricalLocationId(Long.valueOf(historicalLocation.getId()));
                locHloc.setFkLocationId(Long.valueOf(location.get().getId()));
                locationHistoricalLocations.add(locHloc);
            } else {
                // create location and update LocationHistoricalLocation table
                LocationHistoricalLocation locHloc = new LocationHistoricalLocation();
                locHloc.setFkLocationId(Long.valueOf(getLocationService().createOrUpdate(l).getId()));
                locHloc.setFkHistoricalLocationId(Long.valueOf(historicalLocation.getId()));
                locationHistoricalLocations.add(locHloc);
            }
        }
        // Update LocationHistoricalLocation table
        locationHistoricalLocationDao.saveAll(locationHistoricalLocations);
    }

    @Override
    protected HistoricalLocationDTO createOrUpdate(HistoricalLocationDTO entity)
            throws STACRUDException, STAInvalidQueryException {
        if (entity.getId() != null && historicalLocationDao.existsByStaIdentifier(entity.getId(), entityClass)) {
            return updateEntity(entity.getId(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override
    protected HistoricalLocationDTO updateEntity(String id, HistoricalLocationDTO entity, HttpMethod method)
            throws STACRUDException, STAInvalidQueryException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<HistoricalLocationDTO> existing =
                        historicalLocationDao.findByStaIdentifier(id, null, entityClass);
                if (existing.isPresent()) {
                    HistoricalLocationDTO merged = merge(existing.get(), entity);
                    historicalLocationDao.update(POJOWrapper(merged));
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
    protected void deleteEntity(String id)
            throws STACRUDException, STAInvalidQueryException {
        synchronized (getLock(id)) {
            if (historicalLocationDao.existsByStaIdentifier(id, entityClass)) {
                HistoricalLocationDTO historicalLocation =
                        historicalLocationDao.findByStaIdentifier(id, null, entityClass).get();

                // delete location<->historicalLocation records from link table
                locationHistoricalLocationDao.deleteByHistoricalLocationId(Long.valueOf(historicalLocation.getId()));

                // the link between thing<->historicalLocation is invisible to Thing table
                // so we do not have to update Thing table

                historicalLocationDao.deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }


    @Override
    protected HistoricalLocationDTO merge(HistoricalLocationDTO existing, HistoricalLocationDTO toMerge)
            throws STACRUDException {
        if (toMerge.getTime() != null) {
            existing.setTime(toMerge.getTime());
        }
        // update LocationHistoricalLocation link table here
        Set<LocationHistoricalLocation> locationHistoricalLocationSet = new LinkedHashSet<>();
        if (toMerge.getLocations() != null) {
            for (LocationDTO location : toMerge.getLocations()) {
                LocationHistoricalLocation lHloc = new LocationHistoricalLocation();
                lHloc.setFkHistoricalLocationId(Long.valueOf(existing.getId()));
                lHloc.setFkLocationId(Long.valueOf(location.getId()));
                locationHistoricalLocationSet.add(lHloc);
            }
            locationHistoricalLocationDao.saveAll(locationHistoricalLocationSet);
        }
        return existing;
    }

    @Override
    protected String checkPropertyName(String property) {
        return historicalLocationDao.checkPropertyName(property).getName();
    }

    @Override
    Field<String> getStaEntityId() {
        return StaEntity.HISTORICAL_LOCATION.STA_IDENTIFIER;
    }

    @Override
    AtomicLong getStaEntityTS() {
        return TS;
    }
}
