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
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.sta.data.query.ThingQuerySpecifications;
import org.n52.sta.data.repositories.ThingRepository;
import org.n52.sta.data.service.EntityServiceRepository.EntityTypes;
import org.n52.sta.serdes.model.ElementWithQueryOptions;
import org.n52.sta.serdes.model.ElementWithQueryOptions.ThingWithQueryOptions;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
@DependsOn({"springApplicationContext"})
public class ThingService extends AbstractSensorThingsEntityService<ThingRepository, PlatformEntity> {

    private static final Logger logger = LoggerFactory.getLogger(ThingService.class);
    private static final ThingQuerySpecifications tQS = new ThingQuerySpecifications();

    public ThingService(ThingRepository repository) {
        super(repository, PlatformEntity.class);
    }

    @Override
    public EntityTypes[] getTypes() {
        return new EntityTypes[] {EntityTypes.Thing, EntityTypes.Things};
    }

    @Override
    protected ElementWithQueryOptions createWrapper(Object entity, QueryOptions queryOptions) {
        return new ThingWithQueryOptions((PlatformEntity) entity, queryOptions);
    }

    @Override
    protected Specification<PlatformEntity> byRelatedEntityFilter(String relatedId,
                                                                  String relatedType,
                                                                  String ownId) {
        Specification<PlatformEntity> filter;
        switch (relatedType) {
        case STAEntityDefinition.HISTORICAL_LOCATIONS: {
            filter = tQS.withRelatedHistoricalLocationIdentifier(relatedId);
            break;
        }
        case STAEntityDefinition.DATASTREAMS: {
            filter = tQS.withRelatedDatastreamIdentifier(relatedId);
            break;
        }
        case STAEntityDefinition.LOCATIONS: {
            filter = tQS.withRelatedLocationIdentifier(relatedId);
            break;
        }
        default:
            return null;
        }

        if (ownId != null) {
            filter = filter.and(tQS.withIdentifier(ownId));
        }
        return filter;
    }

    @Override
    public PlatformEntity createEntity(PlatformEntity newThing) throws STACRUDException {
        PlatformEntity thing = newThing;
        if (!thing.isProcesssed()) {
            if (thing.getIdentifier() != null && !thing.isSetName()) {
                Optional<PlatformEntity> optionalEntity =
                        getRepository().findByIdentifier(thing.getIdentifier());
                if (optionalEntity.isPresent()) {
                    return optionalEntity.get();
                } else {
                    throw new STACRUDException("No Thing with id '" + thing.getIdentifier() + "' found");
                }
            }
            if (thing.getIdentifier() == null) {
                if (getRepository().existsByName(thing.getName())) {
                    Optional<PlatformEntity> optional = getRepository().findByName(thing.getName());
                    return optional.orElse(null);
                } else {
                    // Autogenerate Identifier
                    thing.setIdentifier(UUID.randomUUID().toString());
                }
            } else if (getRepository().existsByIdentifier(thing.getIdentifier())) {
                throw new STACRUDException("Identifier already exists!", HTTPStatus.BAD_REQUEST);
            }
            thing.setProcesssed(true);
            processLocations(thing, thing.getLocations());
            thing = getRepository().intermediateSave(thing);
            processHistoricalLocations(thing);
            processDatastreams(thing);
            thing = getRepository().save(thing);
        }
        return thing;

    }

    @Override
    public PlatformEntity updateEntity(String id, PlatformEntity entity, HttpMethod method) throws STACRUDException {
        checkUpdate(entity);
        if (HttpMethod.PATCH.equals(method)) {
            Optional<PlatformEntity> existing = getRepository().findByIdentifier(id);
            if (existing.isPresent()) {
                PlatformEntity merged = merge(existing.get(), entity);
                if (entity.hasLocationEntities()) {
                    processLocations(merged, entity.getLocations());
                    merged = getRepository().save(merged);
                    processHistoricalLocations(merged);
                }
                return getRepository().save(merged);
            } else {
                throw new STACRUDException("Unable to update. Entity not found.", HTTPStatus.NOT_FOUND);
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
        if (getRepository().existsByIdentifier(identifier)) {
            PlatformEntity thing = getRepository().getOneByIdentifier(identifier);
            // delete datastreams
            thing.getDatastreams().forEach(d -> {
                try {
                    getDatastreamService().delete(d.getIdentifier());
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
            getRepository().deleteByIdentifier(identifier);
        } else {
            throw new STACRUDException("Unable to delete. Entity not found.", HTTPStatus.NOT_FOUND);
        }
    }

    @Override
    public void delete(PlatformEntity entity) {
        getRepository().deleteByIdentifier(entity.getIdentifier());
    }

    @Override
    protected PlatformEntity createOrUpdate(PlatformEntity entity) throws STACRUDException {
        if (entity.getIdentifier() != null && getRepository().existsByIdentifier(entity.getIdentifier())) {
            return updateEntity(entity.getIdentifier(), entity, HttpMethod.PATCH);
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

    private void processLocations(PlatformEntity thing, Set<LocationEntity> oldLocations) throws STACRUDException {
        if (oldLocations != null) {
            Set<LocationEntity> locations = new HashSet<>();
            thing.setLocations(new HashSet<>());
            for (LocationEntity location : oldLocations) {
                LocationEntity optionalLocation = getLocationService().createEntity(location);
                locations.add(optionalLocation != null ? optionalLocation : location);
            }
            thing.setLocations(locations);
        }
    }

    private void processHistoricalLocations(PlatformEntity thing) throws STACRUDException {
        if (thing != null && thing.hasLocationEntities()) {
            Set<HistoricalLocationEntity> historicalLocations = thing.hasHistoricalLocations()
                    ? new LinkedHashSet<>(thing.getHistoricalLocations())
                    : new LinkedHashSet<>();
            HistoricalLocationEntity historicalLocation = new HistoricalLocationEntity();
            historicalLocation.setIdentifier(UUID.randomUUID().toString());
            historicalLocation.setThing(thing);
            historicalLocation.setTime(DateTime.now().toDate());
            historicalLocation.setProcesssed(true);
            HistoricalLocationEntity createdHistoricalLocation =
                    getHistoricalLocationService().createOrUpdate(historicalLocation);
            if (createdHistoricalLocation != null) {
                historicalLocations.add(createdHistoricalLocation);
            }
            for (LocationEntity location : thing.getLocations()) {
                location.setHistoricalLocations(historicalLocations);
                getLocationService().createOrUpdate(location);
            }
            thing.setHistoricalLocations(historicalLocations);
        }
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, LocationEntity> getLocationService() {
        return (AbstractSensorThingsEntityService<?, LocationEntity>) getEntityService(EntityTypes.Location);
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, HistoricalLocationEntity> getHistoricalLocationService() {
        return (AbstractSensorThingsEntityService<?, HistoricalLocationEntity>) getEntityService(
                EntityTypes.HistoricalLocation);
    }

    @SuppressWarnings("unchecked")
    private AbstractSensorThingsEntityService<?, DatastreamEntity> getDatastreamService() {
        return (AbstractSensorThingsEntityService<?, DatastreamEntity>) getEntityService(
                EntityTypes.Datastream);
    }

    @Override
    public Map<String, Set<String>> getRelatedCollections(Object rawObject) {
        Map<String, Set<String>> collections = new HashMap<>();
        PlatformEntity entity = (PlatformEntity) rawObject;

        if (entity.hasLocationEntities()) {
            collections.put(
                    STAEntityDefinition.LOCATION,
                    entity.getLocations()
                          .stream()
                          .map(LocationEntity::getIdentifier)
                          .collect(Collectors.toSet()));
        }

        if (entity.hasHistoricalLocations()) {
            collections.put(
                    STAEntityDefinition.HISTORICAL_LOCATION,
                    entity.getHistoricalLocations()
                          .stream()
                          .map(HistoricalLocationEntity::getIdentifier)
                          .collect(Collectors.toSet()));
        }

        if (entity.hasDatastreams()) {
            collections.put(STAEntityDefinition.DATASTREAM,
                            entity.getDatastreams()
                                  .stream()
                                  .map(DatastreamEntity::getIdentifier)
                                  .collect(Collectors.toSet()));
        }
        return collections;
    }
}
