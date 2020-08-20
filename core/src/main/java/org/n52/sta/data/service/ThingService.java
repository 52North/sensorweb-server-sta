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

import org.joda.time.DateTime;
import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.ThingQuerySpecifications;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.IdentifierRepository;
import org.n52.sta.data.repositories.ThingRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class ThingService
        extends AbstractSensorThingsEntityServiceImpl<ThingRepository, PlatformEntity, PlatformEntity> {

    protected static final ThingQuerySpecifications tQS = new ThingQuerySpecifications();
    private static final Logger logger = LoggerFactory.getLogger(ThingService.class);

    public ThingService(ThingRepository repository) {
        super(repository, PlatformEntity.class);
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.Thing, EntityTypes.Things};
    }

    @Override protected PlatformEntity fetchExpandEntities(PlatformEntity entity, ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            switch (expandProperty) {
            case STAEntityDefinition.HISTORICAL_LOCATIONS:
                Page<HistoricalLocationEntity> hLocs = getHistoricalLocationService()
                        .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                               STAEntityDefinition.THINGS,
                                                               expandItem.getQueryOptions());
                entity.setHistoricalLocations(hLocs.get().collect(Collectors.toSet()));
                break;
            case STAEntityDefinition.DATASTREAMS:
                Page<AbstractDatasetEntity> datastreams = getDatastreamService()
                        .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                               STAEntityDefinition.THINGS,
                                                               expandItem.getQueryOptions());
                entity.setDatasets(datastreams.get().collect(Collectors.toSet()));
                break;
            case STAEntityDefinition.LOCATIONS:
                Page<LocationEntity> locations = getLocationService()
                        .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
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
    protected Specification<PlatformEntity> byRelatedEntityFilter(String relatedId,
                                                                  String relatedType,
                                                                  String ownId) {
        Specification<PlatformEntity> filter;
        switch (relatedType) {
        case STAEntityDefinition.HISTORICAL_LOCATIONS: {
            filter = tQS.withHistoricalLocationStaIdentifier(relatedId);
            break;
        }
        case STAEntityDefinition.DATASTREAMS: {
            filter = tQS.withDatastreamStaIdentifier(relatedId);
            break;
        }
        case STAEntityDefinition.LOCATIONS: {
            filter = tQS.withLocationStaIdentifier(relatedId);
            break;
        }
        default:
            throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(tQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public PlatformEntity createOrfetch(PlatformEntity newThing) throws STACRUDException {
        PlatformEntity thing = newThing;
        if (!thing.isProcessed()) {
            if (thing.getStaIdentifier() != null && !thing.isSetName()) {
                Optional<PlatformEntity> optionalEntity =
                        getRepository().findByStaIdentifier(thing.getStaIdentifier());
                if (optionalEntity.isPresent()) {
                    return optionalEntity.get();
                } else {
                    throw new STACRUDException(String.format(NO_S_WITH_ID_S_FOUND,
                                                             StaConstants.THING,
                                                             thing.getStaIdentifier()));
                }
            }
            if (thing.getStaIdentifier() == null) {
                if (getRepository().existsByName(thing.getName())) {
                    return getRepository().findByName(thing.getName()).orElse(null);
                } else {
                    // Autogenerate Identifier
                    String uuid = UUID.randomUUID().toString();
                    thing.setIdentifier(uuid);
                    thing.setStaIdentifier(uuid);
                }
            }
            synchronized (getLock(thing.getStaIdentifier())) {
                if (getRepository().existsByStaIdentifier(thing.getStaIdentifier())) {
                    throw new STACRUDException(IDENTIFIER_ALREADY_EXISTS, HTTPStatus.CONFLICT);
                } else {
                    thing.setProcessed(true);
                    boolean locationChanged = processLocations(thing, thing.getLocations());
                    thing = getRepository().intermediateSave(thing);
                    processDatastreams(thing);
                    boolean hasUnpersistedHLocs = thing.hasHistoricalLocations() &&
                            thing.getHistoricalLocations().stream().anyMatch(p -> p.getId() == null);
                    if (locationChanged || hasUnpersistedHLocs) {
                        generateHistoricalLocation(thing);
                    }
                    thing = getRepository().save(thing);
                }
            }
        }
        return thing;
    }

    @Override
    @Transactional
    public PlatformEntity updateEntity(String id, PlatformEntity newEntity, HttpMethod method) throws STACRUDException {
        // checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<PlatformEntity> existing =
                        getRepository().findByStaIdentifier(id,
                                                            IdentifierRepository.FetchGraph.FETCHGRAPH_LOCATION,
                                                            IdentifierRepository.FetchGraph.FETCHGRAPH_HIST_LOCATION);
                if (existing.isPresent()) {
                    PlatformEntity merged = merge(existing.get(), newEntity);
                    if (newEntity.hasLocationEntities()) {
                        boolean changedLocations = processLocations(merged, newEntity.getLocations());
                        merged = getRepository().save(merged);
                        if (changedLocations) {
                            generateHistoricalLocation(merged);
                        }
                    }
                    return getRepository().save(merged);
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
    protected PlatformEntity merge(PlatformEntity existing, PlatformEntity toMerge) {
        mergeName(existing, toMerge);
        mergeDescription(existing, toMerge);
        if (toMerge.hasProperties()) {
            existing.setProperties(toMerge.getProperties());
        }
        return existing;
    }

    /*
    private void checkUpdate(PlatformEntity thing) throws STACRUDException {
        if (thing.hasLocationEntities()) {
            for (LocationEntity location : thing.getLocations()) {
                checkInlineLocation(location);
            }
        }
        if (thing.hasDatastreams()) {
            for (DatastreamEntity datastream : thing.getDatastreams()) {
                checkInlineDatastream(datastream);
            }
        }
    }
    */

    @Override
    public void delete(String identifier) throws STACRUDException {
        synchronized (getLock(identifier)) {
            if (getRepository().existsByStaIdentifier(identifier)) {
                PlatformEntity thing =
                        getRepository().findByStaIdentifier(identifier,
                                                            EntityGraphRepository.FetchGraph.FETCHGRAPH_DATASTREAMS,
                                                            EntityGraphRepository.FetchGraph.FETCHGRAPH_HIST_LOCATION)
                                       .get();
                // delete datastreams
                for (AbstractDatasetEntity ds : thing.getDatasets()) {
                    getDatastreamService().delete(ds);
                }
                // delete historicalLocation
                for (HistoricalLocationEntity hloc : thing.getHistoricalLocations()) {
                    getHistoricalLocationService().delete(hloc);
                }
                getRepository().deleteByStaIdentifier(identifier);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    @Override
    public void delete(PlatformEntity entity) throws STACRUDException {
        delete(entity.getStaIdentifier());
    }

    @Override
    public PlatformEntity createOrUpdate(PlatformEntity entity) throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createOrfetch(entity);
    }

    @Override public String checkPropertyName(String property) {
        return tQS.checkPropertyName(property);
    }

    protected void processDatastreams(PlatformEntity thing) throws STACRUDException {
        if (thing.hasDatasets()) {
            Set<AbstractDatasetEntity> datastreams = new HashSet<>();
            for (AbstractDatasetEntity datastream : thing.getDatasets()) {
                datastream.setThing(thing);
                datastreams.add(getDatastreamService().createOrfetch(datastream));
            }
            thing.setDatasets(datastreams);
        }
    }

    private boolean processLocations(PlatformEntity thing, Set<LocationEntity> oldLocations) throws
            STACRUDException {
        boolean didPersist = false;
        if (oldLocations != null) {
            Set<LocationEntity> locations = new HashSet<>();
            thing.setLocations(new HashSet<>());
            for (LocationEntity location : oldLocations) {
                Long id = location.getId();
                LocationEntity persistedLoc = getLocationService().createOrfetch(location);
                locations.add(persistedLoc);
                if (!Objects.equals(id, persistedLoc.getId())) {
                    didPersist = true;
                }
            }
            thing.setLocations(locations);
        }
        return didPersist;
    }

    private void generateHistoricalLocation(PlatformEntity thing) throws STACRUDException {
        if (thing == null) {
            throw new STACRUDException("Error processing HistoricalLocations. Thing does not exist!");
        }
        // Persist nested HistoricalLocations
        if (thing.hasHistoricalLocations()) {
            Set<HistoricalLocationEntity> historicalLocations = thing.getHistoricalLocations();
            thing.setHistoricalLocations(null);
            for (HistoricalLocationEntity historicalLocation : historicalLocations) {
                // Check if historicalLocation is not already persisted
                if (historicalLocation.getId() == null) {
                    getHistoricalLocationService().createOrUpdate(historicalLocation);
                }
            }
        }

        // Create new HistoricalLocation based on current location
        if (thing.hasLocationEntities()) {
            Set<HistoricalLocationEntity> historicalLocations = thing.hasHistoricalLocations()
                    ? new LinkedHashSet<>(thing.getHistoricalLocations())
                    : new LinkedHashSet<>();
            HistoricalLocationEntity historicalLocation = new HistoricalLocationEntity();
            historicalLocation.setIdentifier(UUID.randomUUID().toString());
            historicalLocation.setThing(thing);
            historicalLocation.setTime(DateTime.now().toDate());
            historicalLocation.setProcessed(true);
            HistoricalLocationEntity createdHistoricalLocation =
                    getHistoricalLocationService().createOrUpdate(historicalLocation);
            if (createdHistoricalLocation != null) {
                historicalLocations.add(createdHistoricalLocation);
            }
            for (LocationEntity location : thing.getLocations()) {
                HashSet<HistoricalLocationEntity> hlocs = new HashSet<>();
                hlocs.add(createdHistoricalLocation);
                location.setHistoricalLocations(hlocs);
                getLocationService().createOrUpdate(location);
            }
            thing.setHistoricalLocations(historicalLocations);
        }
    }
}
