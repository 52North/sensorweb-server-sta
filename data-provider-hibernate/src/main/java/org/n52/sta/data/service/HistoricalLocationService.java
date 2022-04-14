/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.api.old.entity.HistoricalLocationDTO;
import org.n52.sta.data.CommonSTAServiceImpl;
import org.n52.sta.data.query.HistoricalLocationQuerySpecifications;
import org.n52.sta.data.repositories.EntityGraphRepository;
import org.n52.sta.data.repositories.HistoricalLocationRepository;
import org.n52.sta.data.repositories.LocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
@Transactional
public class HistoricalLocationService
        extends CommonSTAServiceImpl<
            HistoricalLocationRepository, HistoricalLocationDTO, HistoricalLocationEntity> {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalLocationService.class);

    private static final HistoricalLocationQuerySpecifications hlQS = new HistoricalLocationQuerySpecifications();

    private final LocationRepository locationRepository;

    @Autowired
    public HistoricalLocationService(HistoricalLocationRepository repository,
            LocationRepository locationRepository,
            EntityManager em) {
        super(repository, em, HistoricalLocationEntity.class);
        this.locationRepository = locationRepository;
    }

    @Override
    protected EntityGraphRepository.FetchGraph[] createFetchGraph(ExpandFilter expandOption)
            throws STAInvalidQueryException {
        Set<EntityGraphRepository.FetchGraph> fetchGraphs = new HashSet<>(6);
        if (expandOption != null) {
            for (ExpandItem expandItem : expandOption.getItems()) {
                // We cannot handle nested $filter or $expand
                if (expandItem.getQueryOptions().hasFilterFilter() || expandItem.getQueryOptions().hasExpandFilter()) {
                    continue;
                }
                String expandProperty = expandItem.getPath();
                switch (expandProperty) {
                    case STAEntityDefinition.LOCATIONS:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_LOCATIONS);
                        break;
                    case STAEntityDefinition.THING:
                    // fallthru
                    // The UML in Section 8.2 of the OGC STA v1.0 defines the relations as "Things"
                    // The Definition in Section 8.2.3 of the OGC STA v1.0 defines the relations as "Thing"
                    // We will allow both for now
                    case STAEntityDefinition.THINGS:
                        fetchGraphs.add(EntityGraphRepository.FetchGraph.FETCHGRAPH_PLATFORM);
                        break;
                    default:
                        throw new STAInvalidQueryException(String.format(INVALID_EXPAND_OPTION_SUPPLIED,
                                expandProperty,
                                StaConstants.HISTORICAL_LOCATION));
                }
            }
        }
        return fetchGraphs.toArray(new EntityGraphRepository.FetchGraph[0]);
    }

    @Override
    protected HistoricalLocationEntity fetchExpandEntitiesWithFilter(HistoricalLocationEntity entity,
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
                    Page<LocationEntity> locations = getLocationService()
                            .getEntityCollectionByRelatedEntityRaw(entity.getStaIdentifier(),
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
                            .getEntityByIdRaw(entity.getThing().getId(), expandItem.getQueryOptions()));
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
                throw new IllegalStateException(String.format(TRYING_TO_FILTER_BY_UNRELATED_TYPE, relatedType));
        }

        if (ownId != null) {
            filter = filter.and(hlQS.withStaIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public HistoricalLocationEntity createOrfetch(HistoricalLocationEntity historicalLocation)
            throws STACRUDException {
        synchronized (getLock(historicalLocation.getStaIdentifier())) {
            if (!historicalLocation.isProcessed()) {
                check(historicalLocation);
                HistoricalLocationEntity created = processThing(historicalLocation);
                processLocations(created);
                return created;
            }
            return getRepository().save(historicalLocation);
        }
    }

    @Override
    public HistoricalLocationEntity updateEntity(String id, HistoricalLocationEntity entity, String method)
            throws STACRUDException {
        if ("PATCH".equals(method)) {
            return updateEntity(id, entity);
        } else if ("PUT".equals(method)) {
            throw new STACRUDException(HTTP_PUT_IS_NOT_YET_SUPPORTED, HTTPStatus.NOT_IMPLEMENTED);
        } else {
            throw new STACRUDException(INVALID_HTTP_METHOD_FOR_UPDATING_ENTITY, HTTPStatus.BAD_REQUEST);
        }

    }

    public HistoricalLocationEntity updateEntity(String id, HistoricalLocationEntity entity)
            throws STACRUDException {
        synchronized (getLock(id)) {
            Optional<HistoricalLocationEntity> existing = getRepository().findByStaIdentifier(id);
            if (existing.isPresent()) {
                HistoricalLocationEntity merged = merge(existing.get(), entity);
                HistoricalLocationEntity result = getRepository().save(merged);
                Hibernate.initialize(result.getParameters());
                return result;
            }
            throw new STACRUDException(UNABLE_TO_UPDATE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
        }
    }

    @Override
    public HistoricalLocationEntity createOrUpdate(HistoricalLocationEntity entity)
            throws STACRUDException {
        if (entity.getStaIdentifier() != null && getRepository().existsByStaIdentifier(entity.getStaIdentifier())) {
            return updateEntity(entity.getStaIdentifier(), entity);
        }
        return createOrfetch(entity);
    }

    @Override
    public String checkPropertyName(String property) {
        return hlQS.checkPropertyName(property);
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

    @Override
    public void delete(String id) throws STACRUDException {
        synchronized (getLock(id)) {
            if (getRepository().existsByStaIdentifier(id)) {
                HistoricalLocationEntity historicalLocation = getRepository()
                        .findByStaIdentifier(id,
                                EntityGraphRepository.FetchGraph.FETCHGRAPH_LOCATIONHISTLOCATION,
                                EntityGraphRepository.FetchGraph.FETCHGRAPH_PLATFORM)
                        .get();
                updateLocations(historicalLocation);
                updateThing(historicalLocation);
                getRepository().deleteByStaIdentifier(id);
            } else {
                throw new STACRUDException(UNABLE_TO_DELETE_ENTITY_NOT_FOUND, HTTPStatus.NOT_FOUND);
            }
        }
    }

    private void check(HistoricalLocationEntity historicalLocation) throws STACRUDException {
        if (historicalLocation.getThing() == null && historicalLocation.getLocations() != null) {
            throw new STACRUDException("The HistoricalLocation to create is invalid", HTTPStatus.BAD_REQUEST);
        }
    }

    private HistoricalLocationEntity processThing(HistoricalLocationEntity historicalLocation)
            throws STACRUDException {
        PlatformEntity thing = getThingService().createOrfetch(historicalLocation.getThing());
        historicalLocation.setThing(thing);
        HistoricalLocationEntity created = getRepository().save(historicalLocation);
        created.setProcessed(true);
        getThingService().save(thing.addHistoricalLocation(created));
        created.setLocations(historicalLocation.getLocations());
        return created;
    }

    private void processLocations(HistoricalLocationEntity historicalLocation) throws STACRUDException {
        Set<LocationEntity> locations = new LinkedHashSet<>();
        for (LocationEntity l : historicalLocation.getLocations()) {
            Optional<LocationEntity> location
                    = locationRepository.findByStaIdentifier(l.getStaIdentifier(),
                            EntityGraphRepository.FetchGraph.FETCHGRAPH_HIST_LOCATIONS);
            if (location.isPresent()) {
                location.get().addHistoricalLocation(historicalLocation);
                locations.add(getLocationService().createOrUpdate(location.get()));
            } else {
                // We assume that l has historicalLocation linked to it correctly already and just was not persisted
                locations.add(getLocationService().createOrUpdate(l));
            }
        }
        historicalLocation.setLocations(locations);
    }

    private void updateLocations(HistoricalLocationEntity historicalLocation) throws STACRUDException {
        for (LocationEntity location : historicalLocation.getLocations()) {
            location.getHistoricalLocations().remove(historicalLocation);
            getLocationService().save(location);
        }
    }

    private void updateThing(HistoricalLocationEntity historicalLocation) throws STACRUDException {
        getThingService().save(historicalLocation.getThing().setHistoricalLocations(null));
    }
}
