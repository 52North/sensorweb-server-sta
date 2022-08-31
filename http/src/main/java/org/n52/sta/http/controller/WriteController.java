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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectWriter;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.api.exception.EditorException;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.path.SelectPath;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.http.serialize.in.StaEntityDeserializer;
import org.n52.sta.http.serialize.out.SerializationContext;
import org.n52.sta.http.util.path.PathFactory;
import org.n52.sta.http.util.path.StaPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@ConditionalOnProperty(name = "server.feature.http.writable", havingValue = "true", matchIfMissing = false)
public class WriteController {

    private final String serviceUri;

    private final EntityServiceLookup lookup;

    private final ObjectMapper mapper;

    private final PathFactory pathFactory;

    public WriteController(@Value("${server.config.service-root-url}") String serviceUri,
                           EntityServiceLookup lookup,
                           PathFactory pathFactory,
                           ObjectMapper mapper) {
        Objects.requireNonNull(serviceUri, "serviceUri must not be null!");
        Objects.requireNonNull(lookup, "lookup must not be null!");
        Objects.requireNonNull(pathFactory, "pathFactory must not be null!");
        Objects.requireNonNull(mapper, "mapper must not be null!");
        this.serviceUri = serviceUri;
        this.lookup = lookup;
        this.pathFactory = pathFactory;
        this.mapper = mapper;
    }

    @PatchMapping
    public ResponseEntity<StreamingResponseBody> handlePatchRequest(@RequestBody JsonNode node,
                                                                   HttpServletRequest request)
            throws STAInvalidUrlException, EditorException, IOException {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @DeleteMapping("/**")
    public ResponseEntity<String> handleDeleteRequest(HttpServletRequest request)
            throws STAInvalidUrlException, EditorException, IOException {
        StaPath< ? extends Identifiable> path = parsePath(request);
        Class< ? extends Identifiable> entityType = path.getEntityType();
        return delete(path, entityType);
    }


    @PostMapping("/**")
    public ResponseEntity<StreamingResponseBody> handlePostRequest(@RequestBody JsonNode node,
            HttpServletRequest request)
            throws STAInvalidUrlException, EditorException, IOException {
        RequestContext requestContext = RequestContext.create(serviceUri, request, pathFactory);
        SerializationContext serializationContext = SerializationContext.create(requestContext, mapper);

        StaPath< ? extends Identifiable> path = parsePath(request);
        Class< ? extends Identifiable> entityType = path.getEntityType();
        Identifiable saved = save(node, entityType);

        return ResponseEntity.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(serializeEntity(saved, serializationContext));
    }

    private <T extends Identifiable> StreamingResponseBody serializeEntity(T entity, SerializationContext context) {
        return outputStream -> {
            OutputStream out = new BufferedOutputStream(outputStream);
            ObjectWriter writer = context.createWriter();
            writer.writeValue(out, entity);
        };
    }

    private StaPath< ? extends Identifiable> parsePath(HttpServletRequest request) throws STAInvalidUrlException {
        String lookupPath = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        StaPath< ? extends Identifiable> path = pathFactory.parse(lookupPath);
        SelectPath.PathType pathType = path.getPathType();
        if (pathType == SelectPath.PathType.property) {
            throw new STAInvalidUrlException("Invalid path for POST request: " + path);
        }
        return path;
    }

    private <T extends Identifiable> T save(JsonNode node, Class<T> type)
            throws EditorException, IOException {
        ObjectMapper mapperConfig = registerTypeDeserializer(type);
        ObjectReader reader = mapperConfig.readerFor(type);
        return getService(type).save(reader.readValue(node));
    }

    private <T extends Identifiable> ResponseEntity<String> delete(StaPath<T> path, Class<? extends Identifiable> type) {

        if (path.getPathType() != SelectPath.PathType.entity) {
            return ResponseEntity.badRequest().body("illegal path. can only delete entities!");
        }

        //TODO: Implement deletion of related Enties
        //TODO: e.g. /Datastreams(13)/Sensor
        if (path.getPathSegments().size() > 1 || path.getPathSegments().get(0).getIdentifier().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Deletion of entities addressed indirectly " +
                                                                                  "is not implemented yet!");
        }

        String identifier = path.getPathSegments().get(0).getIdentifier().get();
        getService(type).delete(identifier);

        return ResponseEntity.ok("");
    }

    private <T extends Identifiable> ObjectMapper registerTypeDeserializer(Class<T> type) {
        ObjectMapper mapperConfig = mapper.copy();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(type, new StaEntityDeserializer<>(type, mapperConfig));
        return mapperConfig.registerModule(module);
    }

    private <T extends Identifiable> EntityService<T> getService(Class<T> type) {
        return lookup.getService(type)
                     .orElseThrow(() -> {
                         String typeName = type.getSimpleName();
                         String msg = "No service registered for type '%s'";
                         return new IllegalStateException(String.format(msg, typeName));
                     });

    }
}
