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

package org.n52.sta.http.controller;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.shetland.ogc.sta.exception.STANotFoundException;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.path.Request;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.http.serialize.out.CollectionNode;
import org.n52.sta.http.serialize.out.SerializationContext;
import org.n52.sta.http.util.path.PathFactory;
import org.n52.sta.http.util.path.StaPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@RestController
public class ReadController {

    private final String serviceUri;

    private final EntityServiceLookup lookup;

    private final ObjectMapper mapper;

    private final PathFactory pathFactory;

    public ReadController(
                          @Value("${server.config.service-root-url}") String serviceUri,
                          EntityServiceLookup lookup,
                          PathFactory pathFactory,
                          ObjectMapper mapper) {
        Objects.requireNonNull(serviceUri, "serviceUri must not be null!");
        Objects.requireNonNull(lookup, "lookup must not be null!");
        Objects.requireNonNull(pathFactory, "pathFactory must not be null!");
        Objects.requireNonNull(mapper, "mapper must not be null!");
        this.serviceUri = serviceUri;
        this.lookup = lookup;
        this.mapper = mapper;
        this.pathFactory = pathFactory;
    }

    @GetMapping(value = "/**")
    public ResponseEntity<StreamingResponseBody> handleGetRequest(HttpServletRequest servletRequest)
            throws Exception {
        RequestContext requestContext = RequestContext.create(serviceUri, servletRequest, pathFactory);
        SerializationContext serializationContext = SerializationContext.create(requestContext, mapper);
        StaPath< ? extends Identifiable> path = requestContext.getPath();
        EntityService< ? extends Identifiable> entityService = getEntityService(path.getEntityType());
        Request request = requestContext.getRequest();

        StreamingResponseBody body = null;
        MediaType contentType = MediaType.APPLICATION_JSON;
        switch (path.getPathType()) {
            case collection:
                body = serializeCollection(entityService.getEntities(request), serializationContext);
                break;
            case entity:
            case property:
                body = serializeEntity(getEntity(entityService, request), serializationContext);
                break;
            case value:
                body = serializePropertyValue(getEntity(entityService, request), serializationContext);
                contentType = MediaType.TEXT_PLAIN;
                break;
            default:
                throw new STAInvalidUrlException("Unknown request type!");
        }
        return ResponseEntity.ok()
                             .contentType(contentType)
                             .body(body);
    }

    private Identifiable getEntity(EntityService< ? extends Identifiable> entityService, Request request)
            throws STANotFoundException {
        return entityService.getEntity(request)
                            .orElseThrow(() -> new STANotFoundException("no matching entity found"));
    }

    private <T extends Identifiable> StreamingResponseBody serializeEntity(T entity, SerializationContext context) {
        return outputStream -> {
            OutputStream out = new BufferedOutputStream(outputStream);
            ObjectWriter writer = context.createWriter();
            writer.writeValue(out, entity);
        };
    }

    private <T extends Identifiable> StreamingResponseBody serializeCollection(EntityPage<T> page,
            SerializationContext context) {
        return outputStream -> {
            try (OutputStream out = new BufferedOutputStream(outputStream)) {
                ObjectWriter writer = context.createWriter();
                CollectionNode<T> node = new CollectionNode<>(page, "http://");
                writer.writeValue(out, node);
            }
        };
    }

    private <T extends Identifiable> StreamingResponseBody serializePropertyValue(T entity,
            SerializationContext context) {
        return outputStream -> {
            OutputStream out = new BufferedOutputStream(outputStream);
            ObjectWriter writer = context.createWriter();
            String jsonValue = writer.writeValueAsString(entity);
            // We extract the plain value from json representation
            String plainValue = jsonValue.length() > 2
                    ? jsonValue.substring(jsonValue.indexOf("\"", 3) + 3, jsonValue.length() - 3)
                    : "";
            out.write(plainValue.getBytes(StandardCharsets.UTF_8));
            out.flush();
        };
    }

    private <T extends Identifiable> EntityService<T> getEntityService(Class<T> type) {
        return lookup.getService(type)
                     .orElseThrow(() -> new IllegalStateException("No service registered for type '"
                             + type.getSimpleName()
                             + "'"));
    }

}
