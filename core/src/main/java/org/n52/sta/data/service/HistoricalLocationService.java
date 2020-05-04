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

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.n52.janmayen.http.HTTPStatus;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.HistoricalLocationEntityDefinition;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.query.HistoricalLocationQuerySpecifications;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.HistoricalLocationRepository;
import org.n52.sta.data.repositories.LocationRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class HistoricalLocationService
        extends AbstractSensorThingsEntityServiceImpl<HistoricalLocationRepository, HistoricalLocationEntity,
        HistoricalLocationEntity> {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalLocationService.class);

    private static final HistoricalLocationQuerySpecifications hlQS = new HistoricalLocationQuerySpecifications();

    private final LocationRepository locationRepository;

    @Autowired
    public HistoricalLocationService(HistoricalLocationRepository repository,
                                     LocationRepository locationRepository) {
        super(repository, HistoricalLocationEntity.class, EntityGraphRepository.FetchGraph.FETCHGRAPH_THING);
        this.locationRepository = locationRepository;
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.HistoricalLocation, EntityTypes.HistoricalLocations};
    }

    @Override
    protected HistoricalLocationEntity fetchExpandEntities(HistoricalLocationEntity entity,
                                                           ExpandFilter expandOption)
            throws STACRUDException, STAInvalidQueryException {
        for (ExpandItem expandItem : expandOption.getItems()) {
            String expandProperty = expandItem.getPath();
            if (HistoricalLocationEntityDefinition.NAVIGATION_PROPERTIES.contains(expandProperty)) {
                switch (expandProperty) {
                case STAEntityDefinition.LOCATIONS:
                    Page<LocationEntity> locations = getLocationService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
                                                                   STAEntityDefinition.HISTORICAL_LOCATIONS,
                                                                   expandItem.getQueryOptions());
                    entity.setLocations(locations.get().collect(Collectors.toSet()));
                    break;
                case STAEntityDefinition.THING:
                    PlatformEntity things = getThingService()
                            .getEntityByRelatedEntityRaw(entity.getStaIdentifier(),
                                                         STAEntityDefinition.HISTORICAL_LOCATIONS,
                                                         null,
                                                         expandItem.getQueryOptions());
                    entity.setThing(things);
                    break;
                default:
                    throw new RuntimeException("This can never happen!");
                }
            } else {
                throw new STAInvalidQueryException("Invalid expandOption supplied. Cannot find " + expandProperty +
                                                           " on Entity of type 'HistoricalLocation'");
            }
        }
        return entity;
    }

    @Override
    protected Specification<HistoricalLocationEntity> byRelatedEntityFilter(String relatedId,
                                                                            String relatedType,
                                                                            String ownId) {
        Specification<HistoricalLocationEntity> filter;
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
            throw new IllegalStateException("Trying to filter by unrelated type: " + relatedType + "not found!");
        }

        if (ownId != null) {
            filter = filter.and(hlQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public HistoricalLocationEntity createEntity(HistoricalLocationEntity historicalLocation)
            throws STACRUDException {
        synchronized (getLock(historicalLocation.getStaIdentifier())) {
            if (!historicalLocation.isProcesssed()) {
                check(historicalLocation);
                HistoricalLocationEntity created = processThing(historicalLocation);
                processLocations(created);
                return created;
            }
            return getRepository().save(historicalLocation);
        }
    }

    private void check(HistoricalLocationEntity historicalLocation) throws STACRUDException {
        if (historicalLocation.getThing() == null && historicalLocation.getLocations() != null) {
            throw new STACRUDException("The HistoricalLocation to create is invalid", HTTPStatus.BAD_REQUEST);
        }
    }

    private HistoricalLocationEntity processThing(HistoricalLocationEntity historicalLocation)
            throws STACRUDException {
        PlatformEntity thing = getThingService().createOrUpdate(historicalLocation.getThing());
        historicalLocation.setThing(thing);
        HistoricalLocationEntity created = getRepository().save(historicalLocation);
        created.setProcesssed(true);
        getThingService().updateEntity(thing.addHistoricalLocation(created));
        return created.setLocations(historicalLocation.getLocations());
    }

    private void processLocations(HistoricalLocationEntity historicalLocation) throws STACRUDException {
        Set<LocationEntity> locations = new LinkedHashSet<>();
        for (LocationEntity l : historicalLocation.getLocations()) {
            Optional<LocationEntity> location =
                    locationRepository.findByStaIdentifier(l.getStaIdentifier(),
                                                           EntityGraphRepository.FetchGraph.FETCHGRAPH_HIST_LOCATION);
            location.get().addHistoricalLocation(historicalLocation);
            locations.add(getLocationService().createOrUpdate(location.get()));
        }
        historicalLocation.setLocations(locations);
    }

    @Override
    public HistoricalLocationEntity updateEntity(String id, HistoricalLocationEntity entity, HttpMethod method)
            throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            synchronized (getLock(id)) {
                Optional<HistoricalLocationEntity> existing = getRepository().findByStaIdentifier(id);
                if (existing.isPresent()) {
                    HistoricalLocationEntity merged = merge(existing.get(), entity);
                    return getRepository().save(merged);
                }
                throw new STACRUDException("Unable to update. Entity not found.", HTTPStatus.NOT_FOUND);
            }
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HTTPStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HTTPStatus.BAD_REQUEST);
    }

    @Override
    public HistoricalLocationEntity updateEntity(HistoricalLocationEntity entity) {
        return getRepository().save(entity);
    }

    @Override
    public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                HistoricalLocationEntity historicalLocation = getRepository()
                        .findByStaIdentifier(id,
                                          EntityGraphRepository.FetchGraph.FETCHGRAPH_LOCATIONHISTLOCATION,
                                          EntityGraphRepository.FetchGraph.FETCHGRAPH_THING)
                        .get();
                updateLocations(historicalLocation);
                updateThing(historicalLocation);
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException("Unable to delete. Entity not found.", HTTPStatus.NOT_FOUND);
            }
        }
    }

    @Override
    public void delete(HistoricalLocationEntity entity) throws STACRUDException {
        delete(entity.getStaIdentifier());
    }

    @Override
    protected HistoricalLocationEntity createOrUpdate(HistoricalLocationEntity entity)
            throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity, HttpMethod.PATCH);
        }
        return createEntity(entity);
    }

    private void updateLocations(HistoricalLocationEntity historicalLocation) throws STACRUDException {
        for (LocationEntity location : historicalLocation.getLocations()) {
            location.getHistoricalLocations().remove(historicalLocation);
            getLocationService().updateEntity(location);
        }
    }

    private void updateThing(HistoricalLocationEntity historicalLocation) throws STACRUDException {
        getThingService().updateEntity(historicalLocation.getThing().setHistoricalLocations(null));
    }

    @Override
    public HistoricalLocationEntity merge(HistoricalLocationEntity existing, HistoricalLocationEntity toMerge) {
        if (toMerge.getTime() != null) {
            existing.setTime(toMerge.getTime());
        }
        if (toMerge.getLocations() != null) {
            existing.getLocations().addAll(toMerge.getLocations());
        }
        return existing;
    }
}
