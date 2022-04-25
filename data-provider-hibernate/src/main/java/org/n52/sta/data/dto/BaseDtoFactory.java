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
package org.n52.sta.data.dto;

import java.util.Set;

import org.n52.janmayen.stream.Streams;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.sta.api.dto.BaseDto;

@SuppressWarnings("unchecked")
public abstract class BaseDtoFactory<T extends BaseDto, F extends BaseDtoFactory<T, F>> {

    private final T dto;

    protected BaseDtoFactory(T dto) {
        this.dto = dto;
    }

    protected F withMetadata(DescribableEntity entity) {
        return withMetadata(entity.getStaIdentifier(), entity.getName(), entity.getDescription());
    }

    public F withMetadata(String id, String name, String description) {
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        return (F) this;
    }

    protected F setProperties(DescribableEntity entity) {
        Set<ParameterEntity<?>> parameters = entity.getParameters();
        Streams.stream(parameters).forEach(this::addProperty);
        return (F) this;
    }

    protected F addProperty(ParameterEntity<?> entity) {
        addProperty(entity.getName(), entity.getValue());
        return (F) this;
    }

    public F addProperty(String key, Object value) {
        dto.addProperty(key, value);
        return (F) this;
    }

    public T get() {
        return dto;
    }

}
