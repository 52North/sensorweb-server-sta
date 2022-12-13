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

package org.n52.sta.api.domain;

import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.Relation;

import java.util.Objects;
import java.util.Optional;

/**
 * References either an external object or an internal observation.
 * <p>
 * Used in combination with {@link Relation} to ensure both properties are mutually exclusive.
 */
public final class TargetReference {

    private final String externalObject;

    private final Optional<Observation> object;

    private TargetReference(Observation observation) {
        Objects.requireNonNull(observation, "observation must not be null");
        this.object = Optional.of(observation);
        this.externalObject = null;
    }

    private TargetReference(String externalObject) {
        Objects.requireNonNull(externalObject, "externalObject must not be null!");
        if (externalObject.isEmpty()) {
            throw new IllegalArgumentException("externalObject cannot be empty");
        }
        this.externalObject = externalObject;
        this.object = Optional.empty();
    }

    public static TargetReference objectExternal(String externalObject) {
        return new TargetReference(externalObject);
    }

    public static TargetReference objectInternal(Observation observation) {
        return new TargetReference(observation);
    }

    public boolean isObjectPresent() {
        return object.isPresent();
    }

    public String getExternalObject() {
        return externalObject;
    }

    public Observation getObject() {
        return object.get();
    }

}
