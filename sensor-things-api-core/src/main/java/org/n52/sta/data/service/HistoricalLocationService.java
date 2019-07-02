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

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.sta.data.query.HistoricalLocationQuerySpecifications;
import org.n52.sta.data.repositories.HistoricalLocationRepository;
import org.n52.sta.data.repositories.LocationRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.mapping.HistoricalLocationMapper;
import org.n52.sta.service.query.QueryOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.n52.sta.edm.provider.entities.LocationEntityProvider.ET_LOCATION_NAME;
import static org.n52.sta.edm.provider.entities.ThingEntityProvider.ET_THING_NAME;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
@DependsOn({"springApplicationContext"})
public class HistoricalLocationService extends AbstractSensorThingsEntityService<HistoricalLocationRepository, HistoricalLocationEntity> {

    private HistoricalLocationMapper mapper;

    @Autowired
    private LocationRepository locationRepository;

    private HistoricalLocationQuerySpecifications hlQS = new HistoricalLocationQuerySpecifications();

    public HistoricalLocationService(HistoricalLocationRepository repository, HistoricalLocationMapper mapper) {
        super(repository);
        this.mapper = mapper;
    }

    @Override
    public EntityTypes getType() {
        return EntityTypes.HistoricalLocation;
    }

    @Override
    public EntityCollection getEntityCollection(QueryOptions queryOptions) throws ODataApplicationException {
        EntityCollection retEntitySet = new EntityCollection();
        Specification<HistoricalLocationEntity> filter = getFilterPredicate(HistoricalLocationEntity.class, queryOptions);

        getRepository().findAll(filter, createPageableRequest(queryOptions)).forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(String id) {
        Optional<HistoricalLocationEntity> entity = getRepository().findByIdentifier(id);
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(String sourceId, EdmEntityType sourceEntityType, QueryOptions queryOptions) throws ODataApplicationException {
        Specification<HistoricalLocationEntity> filter = getFilter(sourceId, sourceEntityType);
        filter = filter.and(getFilterPredicate(HistoricalLocationEntity.class, queryOptions));

        Iterable<HistoricalLocationEntity> locations = getRepository().findAll(filter, createPageableRequest(queryOptions));
        EntityCollection retEntitySet = new EntityCollection();
        locations.forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public long getRelatedEntityCollectionCount(String sourceId, EdmEntityType sourceEntityType) {
        Specification<HistoricalLocationEntity> filter = getFilter(sourceId, sourceEntityType);
        return getRepository().count(filter);
    }

    public Specification<HistoricalLocationEntity> getFilter(String sourceId, EdmEntityType sourceEntityType) {
        Specification<HistoricalLocationEntity> filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case "iot.Location": {
                filter = hlQS.withRelatedLocationIdentifier(sourceId);
                break;
            }
            case "iot.Thing": {
                filter = hlQS.withRelatedThingIdentifier(sourceId);
                break;
            }
            default:
                return null;
        }
        return filter;
    }

    @Override
    public boolean existsRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.existsRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public boolean existsRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Specification<HistoricalLocationEntity> filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case "iot.Location": {
                filter = hlQS.withRelatedLocationIdentifier(sourceId);
                break;
            }
            case "iot.Thing": {
                filter = hlQS.withRelatedThingIdentifier(sourceId);
                break;
            }
            default:
                return false;
        }
        if (targetId != null) {
            filter = filter.and(hlQS.withIdentifier(targetId));
        }
        return getRepository().count(filter) > 0;
    }

    @Override
    public Optional<String> getIdForRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.getIdForRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Optional<String> getIdForRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Optional<HistoricalLocationEntity> historicalLocation = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        return historicalLocation.map(historicalLocationEntity -> Optional.of(historicalLocationEntity.getIdentifier())).orElseGet(Optional::empty);
    }

    @Override
    public Entity getRelatedEntity(String sourceId, EdmEntityType sourceEntityType) {
        return this.getRelatedEntity(sourceId, sourceEntityType, null);
    }

    @Override
    public Entity getRelatedEntity(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Optional<HistoricalLocationEntity> location = this.getRelatedEntityRaw(sourceId, sourceEntityType, targetId);
        return location.map(historicalLocationEntity -> mapper.createEntity(historicalLocationEntity)).orElse(null);
    }

    /**
     * Retrieves HistoricalLocation Entity with Relation to sourceEntity from
     * Database. Returns empty if HistoricalLocation is not found or Entities
     * are not related.
     *
     * @param sourceId         Id of the Source Entity
     * @param sourceEntityType Type of the Source Entity
     * @param targetId         Id of the Thing to be retrieved
     * @return Optional<HistoricalLocationEntity> Requested Entity
     */
    private Optional<HistoricalLocationEntity> getRelatedEntityRaw(String sourceId, EdmEntityType sourceEntityType, String targetId) {
        Specification<HistoricalLocationEntity> filter;
        switch (sourceEntityType.getFullQualifiedName().getFullQualifiedNameAsString()) {
            case "iot.Location": {
                filter = hlQS.withRelatedLocationIdentifier(sourceId);
                break;
            }
            case "iot.Thing": {
                filter = hlQS.withRelatedThingIdentifier(sourceId);
                break;
            }
            default:
                return Optional.empty();
        }

        if (targetId != null) {
            filter = filter.and(hlQS.withIdentifier(targetId));
        }
        return getRepository().findOne(filter);
    }

    @Override
    public long getCount(QueryOptions queryOptions) throws ODataApplicationException {
        return getRepository().count(getFilterPredicate(HistoricalLocationEntity.class, queryOptions));
    }

    @Override
    public HistoricalLocationEntity create(HistoricalLocationEntity historicalLocation) throws ODataApplicationException {
        if (!historicalLocation.isProcesssed()) {
            check(historicalLocation);
            HistoricalLocationEntity created = processThing(historicalLocation);
            processLocations(created);
            return created;
        }
        return getRepository().save(historicalLocation);
    }

    private void check(HistoricalLocationEntity historicalLocation) throws ODataApplicationException {
        if (historicalLocation.getThing() == null && historicalLocation.getLocations() != null) {
            throw new ODataApplicationException("The datastream to create is invalid",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
        }
    }

    private HistoricalLocationEntity processThing(HistoricalLocationEntity historicalLocation) throws ODataApplicationException {
        PlatformEntity thing = getThingService().createOrUpdate(historicalLocation.getThing());
        historicalLocation.setThing(thing);
        HistoricalLocationEntity created = getRepository().save(historicalLocation);
        created.setProcesssed(true);
        getThingService().update(thing.addHistoricalLocation(created));
        return created.setLocations(historicalLocation.getLocations());
    }

    private void processLocations(HistoricalLocationEntity historicalLocation) throws ODataApplicationException {
        Set<LocationEntity> locations = new LinkedHashSet<>();
        for (LocationEntity l : historicalLocation.getLocations()) {
            LocationEntity location = locationRepository.getOneByIdentifier(l.getIdentifier());
            location.addHistoricalLocation(historicalLocation);
            locations.add(getLocationService().createOrUpdate(location));
        }
        historicalLocation.setLocations(locations);
    }

    @Override
    public HistoricalLocationEntity update(HistoricalLocationEntity entity, HttpMethod method) throws ODataApplicationException {
        if (HttpMethod.PATCH.equals(method)) {
            Optional<HistoricalLocationEntity> existing = getRepository().findByIdentifier(entity.getIdentifier());
            if (existing.isPresent()) {
                HistoricalLocationEntity merged = mapper.merge(existing.get(), entity);
                return getRepository().save(merged);
            }
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        } else if (HttpMethod.PUT.equals(method)) {
            throw new ODataApplicationException("Http PUT is not yet supported!",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.getDefault());
        }
        throw new ODataApplicationException("Invalid http method for updating entity!",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.getDefault());
    }

    @Override
    public HistoricalLocationEntity update(HistoricalLocationEntity entity) {
        return getRepository().save(entity);
    }

    @Override
    public void delete(String id) throws ODataApplicationException {
        if (getRepository().existsByIdentifier(id)) {
            HistoricalLocationEntity historicalLocation = getRepository().getOneByIdentifier(id);
            updateLocations(historicalLocation);
            updateThing(historicalLocation);
            getRepository().deleteByIdentifier(id);
        } else {
            throw new ODataApplicationException("Entity not found.", HttpStatusCode.NOT_FOUND.getStatusCode(),
                    Locale.ROOT);
        }
    }

    @Override
    public void delete(HistoricalLocationEntity entity) {
        // delete historicalLocation
        entity.getLocations().forEach(l -> {
            try {
                l.getHistoricalLocations().remove(entity);
                getLocationService().update(l);
            } catch (ODataApplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        getRepository().saveAndFlush(entity);
        getRepository().deleteByIdentifier(entity.getIdentifier());
    }

    @Override
    protected HistoricalLocationEntity createOrUpdate(HistoricalLocationEntity entity) throws ODataApplicationException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return update(entity, HttpMethod.PATCH);
        }
        return create(entity);
    }

    private void updateLocations(HistoricalLocationEntity historicalLocation) throws ODataApplicationException {
        for (LocationEntity location : historicalLocation.getLocations()) {
            location.getHistoricalLocations().remove(historicalLocation);
            getLocationService().update(location);
        }
    }

    private void updateThing(HistoricalLocationEntity historicalLocation) throws ODataApplicationException {
        getThingService().update(historicalLocation.getThing().setHistoricalLocations(null));
    }

    private AbstractSensorThingsEntityService<?, PlatformEntity> getThingService() {
        return (AbstractSensorThingsEntityService<?, PlatformEntity>) getEntityService(EntityTypes.Thing);
    }

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

        try {
            collections.put(ET_THING_NAME,
                    Collections.singleton(entity.getThing().getIdentifier()));
        } catch (NullPointerException e) {
        }
        Set<String> set = new HashSet<>();
        entity.getLocations().forEach((en) -> {
            set.add(en.getIdentifier());
        });
        collections.put(ET_LOCATION_NAME, set);
        return collections;
    }
}
