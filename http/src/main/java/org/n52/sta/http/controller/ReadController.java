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
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
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

@RestController
public class ReadController {

    private final String serviceUri;

    private final EntityServiceLookup lookup;

    private final ObjectMapper mapper;

    private final PathFactory pathFactory;

    public ReadController(
            @Value("${server.config.service-root-url}")
                    String serviceUri,
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
    public ResponseEntity<StreamingResponseBody> handleGetRequest(HttpServletRequest request)
            throws Exception {
        RequestContext requestContext = RequestContext.create(serviceUri, request, pathFactory);
        SerializationContext serializationContext = SerializationContext.create(requestContext, mapper);
        return ResponseEntity.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(getAndWriteToResponse(requestContext, serializationContext));
    }

    private StreamingResponseBody getAndWriteToResponse(RequestContext requestContext, SerializationContext context)
            throws Exception {
        StaPath<? extends Identifiable> path = requestContext.getPath();
        EntityService<? extends Identifiable> entityService = getEntityService(path.getEntityType());
        Request request = requestContext.getRequest();
        switch (path.getPathType()) {
            case collection:
                EntityPage<? extends Identifiable> collection = entityService.getEntities(request);
                return writeCollection(collection, context);
            case entity:
                //fallthru
            case property:
                Optional<? extends Identifiable> entity = entityService.getEntity(request);
                return writeEntity(entity.orElseThrow(() -> new STANotFoundException("no such entity")), context);
            default:
                throw new STACRUDException("could not recognize PathType!");
        }
    }

    private <T extends Identifiable> StreamingResponseBody writeEntity(T entity, SerializationContext context) {
        return outputStream -> {
            OutputStream out = new BufferedOutputStream(outputStream);
            ObjectWriter writer = context.createWriter();
            // TODO apply service-root-uri
            writer.writeValue(out, entity);
        };
    }

    private <T extends Identifiable> StreamingResponseBody writeCollection(EntityPage<T> page,
                                                                           SerializationContext context) {
        return outputStream -> {
            try (OutputStream out = new BufferedOutputStream(outputStream)) {
                ObjectWriter writer = context.createWriter();
                // TODO apply service-root-uri
                CollectionNode<T> node = new CollectionNode<>(page, "http://");
                writer.writeValue(out, node);
            }
        };
    }

    private <T extends Identifiable> EntityService<T> getEntityService(Class<T> type) {
        return lookup.getService(type)
                     .orElseThrow(() -> new IllegalStateException("No service registered for type '"
                                                                          + type.getSimpleName()
                                                                          + "'"));
    }

}
