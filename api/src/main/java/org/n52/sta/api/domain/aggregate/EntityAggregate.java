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

package org.n52.sta.api.domain.aggregate;

import java.util.Objects;
import java.util.Optional;

import org.n52.sta.api.EditorException;
import org.n52.sta.api.EntityEditor;
import org.n52.sta.api.entity.Identifiable;

public abstract class EntityAggregate<T extends Identifiable> {

    private final T entity;

    private final Optional<EntityEditor<T>> optionalEditor;

    protected EntityAggregate(T entity) {
        this(entity, null);
    }

    protected EntityAggregate(T entity, EntityEditor<T> editor) {
        Objects.requireNonNull(entity, "entity must not be null!");
        this.entity = entity;
        this.optionalEditor = Optional.ofNullable(editor);
    }

    public T save() throws AggregateException {
        return save(null);
    }

    public T save(T oldEntity) throws AggregateException {
        assertEditor();
        EntityEditor<T> editor = optionalEditor.get();
        try {
            T newEntity = oldEntity == null
                    ? editor.save(entity)
                    : editor.update(entity);
            return newEntity;
        } catch (EditorException e) {
            throw new AggregateException("Could not save entity!", e);
        }
    }

    public T delete() throws AggregateException {
        assertEditor();
        EntityEditor<T> editor = optionalEditor.get();
        try {
            editor.delete(entity.getId());
            return entity;
        } catch (EditorException e) {
            throw new AggregateException("Could not delete entity!", e);
        }
    }
    
    protected void assertRequired(Object reference, String message) throws InvalidAggregateException {
        if (reference == null) {
            throw new InvalidAggregateException(message);
        }
    }

    private void assertEditor() {
        if (!optionalEditor.isPresent()) {
            throw new IllegalStateException("Aggregate is read only!");
        }
    }

}
