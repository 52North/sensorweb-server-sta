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
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.domain.aggregate.AggregateException;
import org.n52.sta.api.domain.aggregate.EntityAggregate;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.EntityProvider;
import org.n52.sta.api.exception.ProviderException;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.exception.ServiceException;
import org.n52.sta.api.path.Request;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEntityService<T extends Identifiable> implements EntityService<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityService.class);

    protected EntityProvider<T> provider;

    protected Optional<EntityEditor<T>> editor;

    public AbstractEntityService(EntityProvider<T> provider) {
        Objects.requireNonNull(provider, "provider must not be null");
        this.provider = provider;
    }

    public Optional<T> getEntity(String id) throws ProviderException {
        return getEntity(id, QueryOptionsFactory.createEmpty());
    }

    public Optional<T> getEntity(String id, QueryOptions queryOptions) throws ProviderException {
        return provider.getEntity(id, queryOptions);
    }

    @Override
    public boolean exists(String id) throws ProviderException {
        return provider.exists(id);
    }

    @Override
    public Optional<T> getEntity(Request req) throws ProviderException {
        return provider.getEntity(req);
    }

    @Override
    public EntityPage<T> getEntities(Request req) throws ProviderException {
        return provider.getEntities(req);
    }

    @Override
    public T save(T entity) throws EditorException {
        try {
            return createAggregate(entity).save();
        } catch (AggregateException e) {
            LOGGER.error("Could not create entity: {}", entity, e);
            throw new EditorException("Could not create Entity!", e);
        }
    }

    @Override
    public T update(T entity) throws EditorException {
        Objects.requireNonNull(entity, "entity must not be null!");
        try {
            String id = entity.getId();
            T stored = getOrThrow(id);
            return createAggregate(stored).save(entity);
        } catch (AggregateException e) {
            LOGGER.error("Could not update entity: {}", entity, e);
            throw new EditorException("Could not update Entity!", e);
        }
    }

    @Override
    public void delete(String id) throws EditorException {
        T entity = getOrThrow(id);
        try {
            createAggregate(entity).delete();
        } catch (AggregateException e) {
            LOGGER.error("Could not delete entity: {}", entity, e);
            throw new EditorException("Could not delete Entity!", e);
        }
    }

    protected abstract EntityAggregate<T> createAggregate(T entity);

    protected T getOrThrow(String id) throws ProviderException {
        return provider.getEntity(id, QueryOptionsFactory.createEmpty())
                       .orElseThrow(() -> new ProviderException("Id '" + id + "' does not exist."));
    }

    public void setEditor(EntityEditor<T> editor) {
        this.editor = Optional.ofNullable(editor);
    }

    @Override
    public EntityEditor<T> unwrapEditor() {
        return editor.orElseThrow(() -> new ServiceException("no editor registered"));
    }

    @Override
    public EntityProvider<T> unwrapProvider() {
        return provider;
    }
}
