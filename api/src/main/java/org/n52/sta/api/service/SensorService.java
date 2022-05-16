/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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
package org.n52.sta.api.service;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.EntityProvider;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.domain.aggregate.AggregateException;
import org.n52.sta.api.domain.aggregate.EntityAggregate;
import org.n52.sta.api.domain.aggregate.SensorAggregate;
import org.n52.sta.api.domain.service.DefaultDomainService;
import org.n52.sta.api.domain.service.DomainService;
import org.n52.sta.api.entity.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class SensorService implements EntityService<Sensor>, EntityEditor<Sensor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorService.class);

    private final EntityProvider<Sensor> sensorProvider;

    private final DomainService<Sensor> domainService;

    private Optional<EntityEditor<Sensor>> sensorEditor;

    public SensorService(EntityProvider<Sensor> provider) {
        this(provider, new DefaultDomainService<>(provider));
    }

    public SensorService(EntityProvider<Sensor> provider, DomainService<Sensor> domainService) {
        Objects.requireNonNull(provider, "provider must not be null");
        this.sensorProvider = provider;
        this.domainService = domainService == null
                ? new DefaultDomainService<>(provider)
                : domainService;
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        return domainService.exists(id);
    }

    @Override
    public Optional<Sensor> getEntity(String id, QueryOptions options) throws ProviderException {
        return domainService.getEntity(id, options);
    }

    @Override
    public EntityPage<Sensor> getEntities(QueryOptions options) throws ProviderException {
        return domainService.getEntities(options);
    }

    @Override
    public Sensor create(Sensor entity) throws ProviderException {
        try {
            return createAggregate(entity).save();
        } catch (AggregateException e) {
            LOGGER.error("Could not create entity: {}", entity, e);
            throw new ProviderException("Could not create Sensor!");
        }
    }

    @Override
    public Sensor update(Sensor entity) throws ProviderException {
        Objects.requireNonNull(entity, "entity must not be null!");
        try {
            String id = entity.getId();
            Sensor sensor = getOrThrow(id);
            return createAggregate(sensor).save(entity);
        } catch (AggregateException e) {
            LOGGER.error("Could not update entity: {}", entity, e);
            throw new ProviderException("Could not update Sensor!");
        }
    }

    @Override
    public void delete(String id) throws ProviderException {
        Sensor entity = getOrThrow(id);
        try {
            createAggregate(entity).delete();
        } catch (AggregateException e) {
            LOGGER.error("Could not delete entity: {}", entity, e);
            throw new ProviderException("Could not delete Sensor!");
        }
    }

    public void setSensorEditor(EntityEditor<Sensor> editor) {
        sensorEditor = Optional.ofNullable(editor);
    }

    private EntityAggregate<Sensor> createAggregate(Sensor entity) {
        return new SensorAggregate(entity, domainService, sensorEditor.orElse(null));
    }

    private Sensor getOrThrow(String id) throws ProviderException {
        return domainService.getEntity(id)
                .orElseThrow(() -> new ProviderException("Id '" + id + "' does not exist."));
    }

}
