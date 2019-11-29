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

import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.query.HistoricalLocationQuerySpecifications;
import org.n52.sta.data.repositories.HistoricalLocationRepository;
import org.n52.sta.data.repositories.LocationRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.exception.STACRUDException;
import org.n52.sta.serdes.model.ElementWithQueryOptions;
import org.n52.sta.serdes.model.ElementWithQueryOptions.HistoricalLocationWithQueryOptions;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.utils.QueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
public class HistoricalLocationService
        extends AbstractSensorThingsEntityService<HistoricalLocationRepository, HistoricalLocationEntity> {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalLocationService.class);

    private static final HistoricalLocationQuerySpecifications hlQS = new HistoricalLocationQuerySpecifications();

    private final LocationRepository locationRepository;

    @Autowired
    public HistoricalLocationService(HistoricalLocationRepository repository,
                                     LocationRepository locationRepository) {
        super(repository, HistoricalLocationEntity.class);
        this.locationRepository = locationRepository;
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.HistoricalLocation, EntityTypes.HistoricalLocations};
    }

    @Override
    protected ElementWithQueryOptions createWrapper(Object entity, QueryOptions queryOptions) {
        return new HistoricalLocationWithQueryOptions((HistoricalLocationEntity) entity, queryOptions);
    }

    @Override
    protected Specification<HistoricalLocationEntity> byRelatedEntityFilter(String relatedId,
                                                                            String relatedType,
                                                                            String ownId) {
        Specification<HistoricalLocationEntity> filter;
        switch (relatedType) {
            case STAEntityDefinition.LOCATIONS: {
                filter = hlQS.withRelatedLocationIdentifier(relatedId);
                break;
            }
            case STAEntityDefinition.THINGS: {
                filter = hlQS.withRelatedThingIdentifier(relatedId);
                break;
            }
            default:
                return null;
        }

        if (ownId != null) {
            filter = filter.and(hlQS.withIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public HistoricalLocationEntity createEntity(HistoricalLocationEntity historicalLocation)
            throws STACRUDException {
        if (!historicalLocation.isProcesssed()) {
            check(historicalLocation);
            HistoricalLocationEntity created = processThing(historicalLocation);
            processLocations(created);
            return created;
        }
        return getRepository().save(historicalLocation);
    }

    private void check(HistoricalLocationEntity historicalLocation) throws STACRUDException {
        if (historicalLocation.getThing() == null && historicalLocation.getLocations() != null) {
            throw new STACRUDException("The datastream to create is invalid", HttpStatus.BAD_REQUEST);
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
            LocationEntity location = locationRepository.getOneByIdentifier(l.getIdentifier());
            location.addHistoricalLocation(historicalLocation);
            locations.add(getLocationService().createOrUpdate(location));
        }
        historicalLocation.setLocations(locations);
    }

    @Override
    public HistoricalLocationEntity updateEntity(String id, HistoricalLocationEntity entity, HttpMethod method)
            throws STACRUDException {
        if (HttpMethod.PATCH.equals(method)) {
            Optional<HistoricalLocationEntity> existing = getRepository().findByIdentifier(id);
            if (existing.isPresent()) {
                HistoricalLocationEntity merged = merge(existing.get(), entity);
                return getRepository().save(merged);
            }
            throw new STACRUDException("Unable to update. Entity not found.", HttpStatus.NOT_FOUND);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new STACRUDException("Http PUT is not yet supported!", HttpStatus.NOT_IMPLEMENTED);
        }
        throw new STACRUDException("Invalid http method for updating entity!", HttpStatus.BAD_REQUEST);
    }

    @Override
    public HistoricalLocationEntity updateEntity(HistoricalLocationEntity entity) {
        return getRepository().save(entity);
    }

    @Override
    public void delete(String id) throws STACRUDException {
        if (getRepository().existsByIdentifier(id)) {
            HistoricalLocationEntity historicalLocation = getRepository().getOneByIdentifier(id);
            updateLocations(historicalLocation);
            updateThing(historicalLocation);
            getRepository().deleteByIdentifier(id);
        } else {
            throw new STACRUDException("Unable to delete. Entity not found.", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void delete(HistoricalLocationEntity entity) {
        // delete historicalLocation
        entity.getLocations().forEach(l -> {
            try {
                l.getHistoricalLocations().remove(entity);
                getLocationService().updateEntity(l);
            } catch (STACRUDException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        getRepository().saveAndFlush(entity);
        getRepository().deleteByIdentifier(entity.getIdentifier());
    }

    @Override
    protected HistoricalLocationEntity createOrUpdate(HistoricalLocationEntity entity)
            throws STACRUDException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return updateEntity(entity.getIdentifier(), entity, HttpMethod.PATCH);
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

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, PlatformEntity> getThingService() {
        return (AbstractSensorThingsEntityService<?, PlatformEntity>) getEntityService(EntityTypes.Thing);
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, LocationEntity> getLocationService() {
        return (AbstractSensorThingsEntityService<?, LocationEntity>) getEntityService(EntityTypes.Location);
    }

    /* (non-Javadoc)
     * @see org.n52.sta.mapping.AbstractMapper#getRelatedCollections(java.lang.Object)
     */
    @Override
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        HistoricalLocationEntity entity = (HistoricalLocationEntity) rawObject;

        if (entity.hasThing()) {
            collections.put(STAEntityDefinition.THING,
                    Collections.singleton(entity.getThing().getIdentifier()));
        }

        if (entity.hasLocationEntities()) {
            collections.put(STAEntityDefinition.LOCATION,
                    entity.getLocations()
                            .stream()
                            .map(LocationEntity::getIdentifier)
                            .collect(Collectors.toSet()));
        }
        return collections;
    }

    @Override
    public HistoricalLocationEntity merge(HistoricalLocationEntity existing, HistoricalLocationEntity toMerge) {
        if (toMerge.getTime() != null) {
            existing.setTime(toMerge.getTime());
        }
        return existing;
    }
}
