/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.ID_ANNOTATION;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.uri.UriParameter;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.QThingEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.sta.data.repositories.ThingRepository;
import org.n52.sta.mapping.ThingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class ThingService implements AbstractSensorThingsEntityService {

    @Autowired
    private ThingMapper mapper;

    @Autowired
    private ThingRepository repository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private HistoricalLocationService historicalLocationService;

    @Autowired
    private DatastreamService datastreamService;

    private static QThingEntity qthing = QThingEntity.thingEntity;

    @Override
    public EntityCollection getEntityCollection() {
        EntityCollection retEntitySet = new EntityCollection();
        repository.findAll().forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(Long id) {
        return getEntityForId(String.valueOf(id));
    }

    @Override
    public Entity getRelatedEntity(Entity sourceEntity) {
        // source Entity (datastream) should always exists (checked beforehand in Request Handler)
        Long sourceId = (Long)sourceEntity.getProperty(ID_ANNOTATION).getValue();
        Optional<DatastreamEntity> datastream = datastreamService.getRawEntityForId(sourceId);
        return mapper.createEntity(datastream.get().getThing());        
    }

    @Override
    public Entity getRelatedEntity(Entity sourceEntity, List<UriParameter> keyPredicates) {
        //TODO: Implement
        return null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Entity sourceEntity) {
        Iterable<ThingEntity> things;
        Long sourceId = (Long) sourceEntity.getProperty(ID_ANNOTATION).getValue();

        switch (sourceEntity.getType()) {
            case "iot.Location": {

                // source Entity (loc) should always exists (checked beforehand in Request Handler)
                //TODO: Refactor to use 1 query instead of 2
                Optional<LocationEntity> loc = locationService.getRawEntityForId(sourceId);
                things = repository.findAll(qthing.locationEntities.contains(loc.get()));
                break;
            }
            case "iot.HistoricalLocation": {

                // source Entity (loc) should always exists (checked beforehand in Request Handler)
                Optional<HistoricalLocationEntity> loc = historicalLocationService.getRawEntityForId(sourceId);
                //TODO: Refactor to use 1 query instead of 2
                things = repository.findAll(qthing.historicalLocationEntities.contains(loc.get()));
                break;
            }
            default:
                return null;
        }
        EntityCollection retEntitySet = new EntityCollection();
        things.forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    private Entity getEntityForId(String id) {
        Optional<ThingEntity> entity = getRawEntityForId(Long.valueOf(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    protected Optional<ThingEntity> getRawEntityForId(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsEntity(Long id) {
        return true;
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        return true;
    }

    @Override
    public boolean existsRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        return true;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Long sourceId, EdmEntityType sourceEntityType) {
        return getEntityCollection();
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OptionalLong getIdForRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Entity getRelatedEntity(Long sourceId, EdmEntityType sourceEntityType, Long targetId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
