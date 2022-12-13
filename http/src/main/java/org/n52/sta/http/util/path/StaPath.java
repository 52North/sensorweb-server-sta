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

package org.n52.sta.http.util.path;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.path.PathSegment;
import org.n52.sta.api.path.SelectPath;
import org.n52.sta.http.serialize.out.SerializationContext;
import org.n52.sta.http.serialize.out.StaBaseSerializer;

/**
 * Holds a URI referencing an STA entity.
 */
public class StaPath<T extends Identifiable> implements SelectPath<T> {

    private final Class<T> entityType;

    private final SelectPath.PathType pathType;

    private final List<PathSegment> pathSegments;

    private final Function<SerializationContext, StaBaseSerializer<T>> serializerFactory;

    private boolean isRef;

    public StaPath(PathType pathType,
                   PathSegment pathSegment,
                   Function<SerializationContext, StaBaseSerializer<T>> serializerFactory,
                   Class<T> type) {

        this.entityType = type;
        this.pathType = pathType;
        this.serializerFactory = serializerFactory;
        this.pathSegments = new ArrayList<>();
        this.pathSegments.add(pathSegment);
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public PathType getPathType() {
        return pathType;
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return pathSegments;
    }

    public void addPathSegment(PathSegment pathSegment) {
        if (pathSegment != null) {
            pathSegments.add(pathSegment);
        }
    }

    public StaBaseSerializer<T> createSerializer(SerializationContext serializationContext) {
        return serializerFactory.apply(serializationContext);
    }

    @Override
    public boolean isRef() {
        return isRef;
    }

    public void setRef(boolean ref) {
        isRef = ref;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("StaPath {\ntype: ")
               .append(pathType)
               .append("\npath:\n");
        for (PathSegment seg : pathSegments) {
            builder.append("    ")
                   .append(seg.toString())
                   .append("\n");
        }
        builder.append("}");
        return builder.toString();
    }

}
