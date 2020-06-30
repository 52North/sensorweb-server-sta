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
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.mapped.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.shetland.ogc.sta.model.ThingEntityDefinition;
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

    private static final Logger logger = LoggerFactory.getLogger(ThingService.class);
    private static final ThingQuerySpecifications tQS = new ThingQuerySpecifications();

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
            if (ThingEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                switch (expandProperty) {
                case STAEntityDefinition.HISTORICAL_LOCATIONS:
                    Page<HistoricalLocationEntity> hLocs = getHistoricalLocationService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                                   STAEntityDefinition.THINGS,
                                                                   expandItem.getQueryOptions());
                    entity.setHistoricalLocations(hLocs.get().collect(Collectors.toSet()));
                    break;
                case STAEntityDefinition.DATASTREAMS:
                    Page<DatastreamEntity> datastreams = getDatastreamService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                                   STAEntityDefinition.THINGS,
                                                                   expandItem.getQueryOptions());
                    entity.setDatastreams(datastreams.get().collect(Collectors.toSet()));
                    break;
                case STAEntityDefinition.LOCATIONS:
                    Page<LocationEntity> locations = getLocationService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                                   STAEntityDefinition.THINGS,
                                                                   expandItem.getQueryOptions());
                    entity.setLocations(locations.get().collect(Collectors.toSet()));
                    break;
                default:
                    throw new RuntimeException("This can never happen!");
                }
            } else {
                throw new STAInvalidQueryException("Invalid expandOption supplied. Cannot find " + expandProperty +
                                                           " on Entity of type 'Thing'");
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
            throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }

        if (ownId != null) {
            filter = filter.and(tQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public PlatformEntity createEntity(PlatformEntity newThing) throws STACRUDException {
        PlatformEntity thing = newThing;
        if (!thing.isProcessed()) {
            if (thing.getStaIdentifier() != null && !thing.isSetName()) {
                Optional<PlatformEntity> optionalEntity =
                        getRepository().findByStaIdentifier(thing.getStaIdentifier());
                if (optionalEntity.isPresent()) {
                    return optionalEntity.get();
                } else {
                    throw new STACRUDException("No Thing with id '" + thing.getStaIdentifier() + "' found");
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
                    throw new STACRUDException("Identifier already exists!", HTTPStatus.CONFLICT);
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
                    throw new STACRUDException("Unable to update. Entity not found.", HTTPStatus.NOT_FOUND);
                }
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HTTPStatus.BAD_REQUEST);
    }

    @Override
    protected PlatformEntity updateEntity(PlatformEntity entity) {
        return getRepository().save(entity);
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
                thing.getDatastreams().forEach(d -> {
                    try {
                        getDatastreamService().delete(d);
                    } catch (STACRUDException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                // delete historicalLocation
                thing.getHistoricalLocations().forEach(hl -> {
                    try {
                        getHistoricalLocationService().delete(hl);
                    } catch (STACRUDException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
                getRepository().deleteByStaIdentifier(identifier);
            } else {
                throw new STACRUDException("Unable to delete. Entity not found.", HTTPStatus.NOT_FOUND);
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
        return createEntity(entity);
    }

    private void processDatastreams(PlatformEntity thing) throws STACRUDException {
        if (thing.hasDatastreams()) {
            Set<DatastreamEntity> datastreams = new LinkedHashSet<>();
            for (DatastreamEntity datastream : thing.getDatastreams()) {
                datastream.setThing(thing);
                DatastreamEntity optionalDatastream = getDatastreamService().createEntity(datastream);
                datastreams.add(optionalDatastream != null ? optionalDatastream : datastream);
            }
            thing.setDatastreams(datastreams);
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
                LocationEntity persistedLoc = getLocationService().createEntity(location);
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
