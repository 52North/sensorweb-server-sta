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

import java.util.Objects;
import java.util.Optional;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.api.EditorException;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.EntityProvider;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.domain.DefaultDomainService;
import org.n52.sta.api.domain.DomainService;
import org.n52.sta.api.domain.aggregate.AggregateException;
import org.n52.sta.api.domain.aggregate.EntityAggregate;
import org.n52.sta.api.domain.aggregate.ThingAggregate;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.path.Request;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThingService extends EntityService<Thing> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingService.class);

    private final DomainService<Thing> domainService;

    private Optional<EntityEditor<Thing>> thingEditor;

    public ThingService(EntityProvider<Thing> provider) {
        this(provider, null);
    }

    public ThingService(EntityProvider<Thing> provider, DomainService<Thing> domainService) {
        Objects.requireNonNull(provider, "provider must not be null");
        this.thingEditor = Optional.empty();
        this.domainService = domainService == null
                ? new DefaultDomainService<>(provider)
                : domainService;
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        return domainService.exists(id);
    }

    @Override
    public Optional<Thing> getEntity(Request req) throws ProviderException {
        return domainService.getEntity(req);
    }

    @Override
    public Optional<Thing> getEntity(String id, QueryOptions queryOptions) throws ProviderException {
        return domainService.getEntity(id, queryOptions);
    }

    @Override
    public EntityPage<Thing> getEntities(Request req) throws ProviderException {
        return domainService.getEntities(req);
    }

    @Override
    public Thing save(Thing entity) throws EditorException {
        try {
            return createAggregate(entity).save();
        } catch (AggregateException e) {
            LOGGER.error("Could not create entity: {}", entity, e);
            throw new ProviderException("Could not create Thing!");
        }
    }

    @Override
    public Thing update(Thing entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null!");
        try {
            String id = entity.getId();
            Thing thing = getOrThrow(id);
            return createAggregate(thing).save(entity);
        } catch (AggregateException e) {
            LOGGER.error("Could not update entity: {}", entity, e);
            throw new ProviderException("Could not update Thing!");
        }
    }

    @Override
    public void delete(String id) throws EditorException {
        Thing entity = getOrThrow(id);
        try {
            createAggregate(entity).delete();
        } catch (AggregateException e) {
            LOGGER.error("Could not delete entity: {}", entity, e);
            throw new ProviderException("Could not delete Thing!");
        }
    }

    public void setThingEditor(EntityEditor<Thing> editor) {
        thingEditor = Optional.ofNullable(editor);
    }

    private EntityAggregate<Thing> createAggregate(Thing entity) {
        return new ThingAggregate(entity, thingEditor.orElse(null));
    }

    private Thing getOrThrow(String id) throws ProviderException {
        return domainService.getEntity(id, QueryOptionsFactory.createEmpty())
                            .orElseThrow(() -> new ProviderException("Id '" + id + "' does not exist."));
    }

}
