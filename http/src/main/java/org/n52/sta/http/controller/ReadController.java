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
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.http.serialize.out.CollectionNode;
import org.n52.sta.http.serialize.out.SerializationContext;
import org.n52.sta.http.serialize.out.StaBaseSerializer;
import org.n52.sta.http.serialize.out.ThingJsonSerializer;
import org.n52.sta.http.util.StaUriValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class ReadController {

    private final String serviceUri;

    private final StaUriValidator validator;

    private final EntityServiceLookup lookup;

    private final ObjectMapper mapper;

    public ReadController(
            @Value("${server.config.service-root-url}") String serviceUri,
            StaUriValidator validator,
            EntityServiceLookup lookup,
            ObjectMapper mapper) {
        Objects.requireNonNull(serviceUri, "serviceUri must not be null!");
        Objects.requireNonNull(validator, "validator must not be null!");
        Objects.requireNonNull(lookup, "lookup must not be null!");
        Objects.requireNonNull(mapper, "mapper must not be null!");
        this.serviceUri = serviceUri;
        this.validator = validator;
        this.lookup = lookup;
        this.mapper = mapper;
    }

    @GetMapping(value = "/Observations")
    public StreamingResponseBody getObservations(HttpServletRequest request) throws STAInvalidUrlException {
        return validateAndProcess(request, null);
    }

    @GetMapping(value = "/Datastreams")
    public StreamingResponseBody getDatastreams(HttpServletRequest request) throws STAInvalidUrlException {
        return validateAndProcess(request, null);
    }

    @GetMapping(value = "/Things")
    public StreamingResponseBody getThings(HttpServletRequest request) throws STAInvalidUrlException {
        return validateAndProcess(request, ThingJsonSerializer::new);
    }

    @GetMapping(value = "/Sensors")
    public StreamingResponseBody getSensors(HttpServletRequest request) throws STAInvalidUrlException {
        return validateAndProcess(request, null);
    }

    @GetMapping(value = "/Locations")
    public StreamingResponseBody getLocations(HttpServletRequest request) throws STAInvalidUrlException {
        return validateAndProcess(request, null);
    }

    @GetMapping(value = "/HistoricalLocations")
    public StreamingResponseBody getHistoricalLocations(HttpServletRequest request) throws STAInvalidUrlException {
        return validateAndProcess(request, null);
    }

    @GetMapping(value = "/FeaturesOfInterest")
    public StreamingResponseBody getFeaturesOfInterest(HttpServletRequest request) throws STAInvalidUrlException {
        return validateAndProcess(request, null);
    }

    @GetMapping(value = "/ObservedProperties")
    public StreamingResponseBody getObservedProperties(HttpServletRequest request) throws STAInvalidUrlException {
        return validateAndProcess(request, null);
    }

    private <T extends Identifiable> StreamingResponseBody validateAndProcess(HttpServletRequest request,
            Function<SerializationContext, StaBaseSerializer<T>> factory) throws STAInvalidUrlException {
        validateRequestPath(request);
        RequestContext requestContext = RequestContext.create(serviceUri, request);
        SerializationContext context = SerializationContext.create(requestContext, mapper);
        StaBaseSerializer<T> serializer = factory.apply(context);
        context.register(serializer);
        return getAndWriteToResponse(context, serializer.getType());
    }

    private <T extends Identifiable> StreamingResponseBody getAndWriteToResponse(SerializationContext context,Class<T> type) throws ProviderException {
        EntityPage<T> collection = getCollection(context, type);
        return writeCollection(collection, context);
    }

    private <T extends Identifiable> EntityPage<T> getCollection(SerializationContext context, Class<T> type) throws ProviderException {
        EntityService<T> entityService = getEntityService(type);
        return entityService.getEntities(context.getQueryOptions());
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
                .orElseThrow(() -> new IllegalStateException("No service registered for collection '"
                        + type.getSimpleName() + "'"));
    }

    private void validateRequestPath(HttpServletRequest request) throws STAInvalidUrlException {
        String path = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        validator.validateRequestPath(path);
    }

}
