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
import org.n52.sta.api.domain.aggregate.LocationAggregate;
import org.n52.sta.api.domain.service.DefaultDomainService;
import org.n52.sta.api.domain.service.DomainService;
import org.n52.sta.api.entity.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class LocationService implements EntityService<Location>, EntityEditor<Location> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

    private final EntityProvider<Location> locationProvider;

    private final DomainService<Location> domainService;

    private Optional<EntityEditor<Location>> locationEditor;

    public LocationService(EntityProvider<Location> provider) {
        this(provider, new DefaultDomainService<>(provider));
    }

    public LocationService(EntityProvider<Location> provider, DomainService<Location> domainService) {
        Objects.requireNonNull(provider, "provider must not be null");
        this.locationProvider = provider;
        this.domainService = domainService == null
                ? new DefaultDomainService<>(provider)
                : domainService;
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        return domainService.exists(id);
    }

    @Override
    public Optional<Location> getEntity(String id, QueryOptions options) throws ProviderException {
        return domainService.getEntity(id, options);
    }

    @Override
    public EntityPage<Location> getEntities(QueryOptions options) throws ProviderException {
        return domainService.getEntities(options);
    }

    @Override
    public Location create(Location entity) throws ProviderException {
        try {
            return createAggregate(entity).save();
        } catch (AggregateException e) {
            LOGGER.error("Could not create entity: {}", entity, e);
            throw new ProviderException("Could not create Location!");
        }
    }

    @Override
    public Location update(Location entity) throws ProviderException {
        Objects.requireNonNull(entity, "entity must not be null!");
        try {
            String id = entity.getId();
            Location location = getOrThrow(id);
            return createAggregate(location).save(entity);
        } catch (AggregateException e) {
            LOGGER.error("Could not update entity: {}", entity, e);
            throw new ProviderException("Could not update Location!");
        }
    }

    @Override
    public void delete(String id) throws ProviderException {
        Location entity = getOrThrow(id);
        try {
            createAggregate(entity).delete();
        } catch (AggregateException e) {
            LOGGER.error("Could not delete entity: {}", entity, e);
            throw new ProviderException("Could not delete Location!");
        }
    }

    public void setLocationEditor(EntityEditor<Location> editor) {
        locationEditor = Optional.ofNullable(editor);
    }

    private EntityAggregate<Location> createAggregate(Location entity) {
        return new LocationAggregate(entity, domainService, locationEditor.orElse(null));
    }

    private Location getOrThrow(String id) throws ProviderException {
        return domainService.getEntity(id)
                .orElseThrow(() -> new ProviderException("Id '" + id + "' does not exist."));
    }

}
