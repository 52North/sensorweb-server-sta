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

import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.uri.UriParameter;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.sta.data.repositories.HistoricalLocationRepository;
import org.n52.sta.mapping.HistoricalLocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class HistoricalLocationService implements AbstractSensorThingsEntityService {

    @Autowired
    private HistoricalLocationRepository repository;

    @Autowired
    private HistoricalLocationMapper mapper;
    

    @Override
    public EntityCollection getEntityCollection() {
        EntityCollection retEntitySet = new EntityCollection();
        repository.findAll().forEach(t -> retEntitySet.getEntities().add(mapper.createEntity(t)));
        return retEntitySet;
    }

    @Override
    public Entity getEntity(List<UriParameter> keyPredicates) {
        return getEntityForId(keyPredicates.get(0).getText());
    }

    @Override
    public Entity getRelatedEntity(Entity sourceEntity) {
        //TODO: implement
        return null;
    }

    @Override
    public Entity getRelatedEntity(Entity sourceEntity, List<UriParameter> keyPredicates) {
        //TODO: implement
        return null;
    }

    @Override
    public EntityCollection getRelatedEntityCollection(Entity sourceEntity) {
        //TODO: implement
        return null;
    }
    
    private Entity getEntityForId(String id) {
        Optional<HistoricalLocationEntity> entity = getRawEntityForId(Long.valueOf(id));
        return entity.isPresent() ? mapper.createEntity(entity.get()) : null;
    }

    protected Optional<HistoricalLocationEntity> getRawEntityForId(Long id) {
        return repository.findById(id);
    }
}
