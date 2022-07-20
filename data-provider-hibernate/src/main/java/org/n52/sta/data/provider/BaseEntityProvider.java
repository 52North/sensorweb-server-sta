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

package org.n52.sta.data.provider;

import java.util.Objects;

import org.n52.sta.api.EntityProvider;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.config.EntityPropertyMapping;

public abstract class BaseEntityProvider<T extends Identifiable> implements EntityProvider<T> {

    protected final EntityPropertyMapping propertyMapping;

    protected BaseEntityProvider(EntityPropertyMapping propertyMapping) {
        Objects.requireNonNull(propertyMapping, "propertyMapping must not be null!");
        this.propertyMapping = propertyMapping;
    }
    
    /**
     * Assert that id is neither null or empty.
     *
     * @param id
     *        the id
     * @throws IllegalArgumentException
     *         if id is invalid
     */
    protected void assertIdentifier(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Invalid 'id': " + id);
        }
    }

}
