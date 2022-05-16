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
package org.n52.sta.api;

import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.service.EntityService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class EntityServiceLookup {

    private final Map<Class<? extends Identifiable>, EntityService<? extends Identifiable>> entityServicesByType;

    public EntityServiceLookup() {
        this.entityServicesByType = new HashMap<>();
    }

    public boolean contains(Class<? extends Identifiable> type) {
        return entityServicesByType.containsKey(type);
    }

    public Set<Class<?>> getRegisteredEntityTypes() {
        return new HashSet<>(entityServicesByType.keySet());
    }

    @SuppressWarnings("unchecked")
    public <T extends Identifiable> Optional<EntityService<T>> getService(Class<T> type) {
        if (type == null) {
            return Optional.empty();
        }
        EntityProvider<?> entityProvider = entityServicesByType.get(type);
        return Optional.ofNullable((EntityService<T>) entityProvider);
    }

    public <T extends Identifiable> void addEntityService(Class<T> type, EntityService<T> service) {
        Objects.requireNonNull(type, "type must not be null!");
        Objects.requireNonNull(service, "service must not be null!");
        entityServicesByType.put(type, service);
    }

}
