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

package org.n52.sta.http.serialize.out;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.locationtech.jts.geom.Geometry;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.util.DateTimeHelper;
import org.n52.sta.api.entity.Identifiable;

public abstract class StaBaseSerializer<T extends Identifiable> extends StdSerializer<T> implements StaSerializer<T> {

    private static final String ENCODINGTYPE_GEOJSON = "application/vnd.geo+json";

    private final String serviceUri;

    private final String collectionName;

    private final transient SerializationContext context;

    protected StaBaseSerializer(SerializationContext context, String collectionName, Class<T> type) {
        super(type);
        Objects.requireNonNull(context, "context must not be null!");
        Objects.requireNonNull(collectionName, "collectionName must not be null!");

        this.serviceUri = removeTrailingSlash(context.getServiceUri());
        this.collectionName = collectionName;
        this.context = context;
        context.register(this);
    }

    private static String removeTrailingSlash(String serviceUri) {
        return serviceUri.endsWith("/")
                ? serviceUri.substring(0, serviceUri.length() - 1)
                : serviceUri;
    }

    @Override
    public Class<T> getType() {
        return handledType();
    }

    protected void writeStringProperty(String name, Supplier<String> value, JsonGenerator gen) throws IOException {
        writeProperty(name, fieldName -> gen.writeStringField(fieldName, value.get()));
    }

    protected void writeObjectProperty(String name, Supplier<Object> value, JsonGenerator gen) throws IOException {
        writeProperty(name, fieldName -> gen.writeObjectField(fieldName, value.get()));
    }

    protected void writeGeometryAndEncodingType(String name, Supplier<Geometry> geometry, JsonGenerator gen)
            throws IOException {
        if (context.isSelected(name)) {
            Geometry value = geometry.get();
            if (value != null) {
                gen.writeObjectField(name, value);
                gen.writeStringField(StaConstants.PROP_ENCODINGTYPE, ENCODINGTYPE_GEOJSON);
            } else {
                gen.writeStringField(name, null);
                gen.writeStringField(StaConstants.PROP_ENCODINGTYPE, null);
            }
        }
    }

    protected void writeTimeProperty(String name, Supplier<Time> value, JsonGenerator gen) throws IOException {
        String time = Optional.ofNullable(value.get())
                              .map(DateTimeHelper::format)
                              .orElse(null);
        writeProperty(name, fieldName -> gen.writeObjectField(fieldName, time));
    }

    protected void writeProperty(String name, ThrowingFieldWriter fieldWriter) throws IOException {
        if (context.isSelected(name)) {
            fieldWriter.writeIfSelected(name);
        }
    }

    protected <E extends Identifiable> void writeMemberCollection(
                                                                  String member,
                                                                  String parentId,
                                                                  JsonGenerator gen,
                                                                  Function<SerializationContext, StaBaseSerializer<E>> serializerFactory,
                                                                  ThrowingMemberWriter<E> memberWriter)
            throws IOException {
        // wrap to write as array
        writeMemberInternal(member, parentId, gen, serializerFactory, serializer -> {
            gen.writeArrayFieldStart(member);
            memberWriter.writeIfSelected(serializer);
            gen.writeEndArray();
        });
    }

    protected <E extends Identifiable> void writeMember(
                                                        String member,
                                                        String parentId,
                                                        JsonGenerator gen,
                                                        Function<SerializationContext, StaBaseSerializer<E>> serializerFactory,
                                                        ThrowingMemberWriter<E> memberWriter)
            throws IOException {
        writeMemberInternal(member, parentId, gen, serializerFactory, memberWriter);
    }

    private <E extends Identifiable> void writeMemberInternal(String member,
                                                              String parentId,
                                                              JsonGenerator gen,
                                                              Function<SerializationContext, StaBaseSerializer<E>> serializerFactory,
                                                              ThrowingMemberWriter<E> memberWriter)
            throws IOException {
        if (context.isSelected(member)) {
            Optional<StaBaseSerializer<E>> serializer = context.getQueryOptionsForExpanded(member)
                                                               .map(expandQueryOptions -> SerializationContext.create(context,
                                                                                                                      expandQueryOptions))
                                                               .map(serializerFactory::apply);
            if (serializer.isPresent()) {
                memberWriter.writeIfSelected(serializer.get());
            } else {
                writeNavLink(member, parentId, gen);
            }
        }
    }

    private void writeNavLink(String member, String parentId, JsonGenerator gen) throws IOException {
        String navLink = createNavigationLink(parentId, member);
        String iotNavLinkProperty = String.format("%s%s", member, StaConstants.AT_IOT_NAVIGATIONLINK);
        gen.writeStringField(iotNavLinkProperty, navLink);
    }

    protected String createNavigationLink(String id, String member) {
        return String.format("%s/%s", createSelfLink(id), member);
    }

    protected String createSelfLink(String id) {
        return String.format("%s/%s(%s)", serviceUri, collectionName, id);
    }

    protected SerializationContext getSerializationContext() {
        return context;
    }

    @FunctionalInterface
    protected interface ThrowingFieldWriter {
        void writeIfSelected(String name) throws IOException;
    }

    @FunctionalInterface
    protected interface ThrowingMemberWriter<T extends Identifiable> {
        void writeIfSelected(StaBaseSerializer<T> context) throws IOException;
    }

}
