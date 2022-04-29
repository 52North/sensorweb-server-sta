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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.api.EntityPage;
import org.n52.sta.api.EntityServiceLookup;
import org.n52.sta.api.ProviderException;
import org.n52.sta.api.entity.Datastream;
import org.n52.sta.api.entity.FeatureOfInterest;
import org.n52.sta.api.entity.HistoricalLocation;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.entity.Location;
import org.n52.sta.api.entity.Observation;
import org.n52.sta.api.entity.ObservedProperty;
import org.n52.sta.api.entity.Sensor;
import org.n52.sta.api.entity.Thing;
import org.n52.sta.api.service.EntityService;
import org.n52.sta.http.serialize.entity.SelectFilterMixin;
import org.n52.sta.http.serialize.json.CollectionNode;
import org.n52.sta.http.util.StaUriValidator;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

@RestController
public class ReadController {

    public static final String SELECT_FILTER = "selectFilter";

    private final StaUriValidator validator;

    private final EntityServiceLookup lookup;

    private final ObjectMapper mapper;

    public ReadController(
            StaUriValidator validator,
            EntityServiceLookup lookup,
            ObjectMapper mapper
    ) {
        Objects.requireNonNull(validator, "validator must not be null!");
        Objects.requireNonNull(lookup, "lookup must not be null!");
        Objects.requireNonNull(mapper, "mapper must not be null!");
        this.validator = validator;
        this.lookup = lookup;
        this.mapper = mapper;
    }

    @GetMapping(value = "/Observations")
    public void getObservations(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws STAInvalidUrlException, IOException {
        getAndWriteToResponse(Observation.class, request, response);
    }

    @GetMapping(value = "/Datastreams")
    public void getDatastreams(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws STAInvalidUrlException, IOException {
        getAndWriteToResponse(Datastream.class, request, response);
    }

    @GetMapping(value = "/Things")
    public void getThings(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws STAInvalidUrlException, IOException {
        getAndWriteToResponse(Thing.class, request, response);
    }

    @GetMapping(value = "/Sensors")
    public void getSensors(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws STAInvalidUrlException, IOException {
        getAndWriteToResponse(Sensor.class, request, response);
    }

    @GetMapping(value = "/Locations")
    public void getLocations(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws STAInvalidUrlException, IOException {
        getAndWriteToResponse(Location.class, request, response);
    }

    @GetMapping(value = "/HistoricalLocations")
    public void getHistoricalLocations(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws STAInvalidUrlException, IOException {
        getAndWriteToResponse(HistoricalLocation.class, request, response);
    }

    @GetMapping(value = "/FeaturesOfInterest")
    public void getFeaturesOfInterest(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws STAInvalidUrlException, IOException {
        getAndWriteToResponse(FeatureOfInterest.class, request, response);
    }

    @GetMapping(value = "/ObservedProperties")
    public void getObservedProperties(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws STAInvalidUrlException, IOException {
        getAndWriteToResponse(ObservedProperty.class, request, response);
    }

    private <T extends Identifiable> void getAndWriteToResponse(
            Class<T> type,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ProviderException, STAInvalidUrlException {
        QueryOptions queryOptions = parseQueryOptions(request);
        EntityPage<T> collection = getCollection(type, queryOptions);
        writeCollection(collection, queryOptions, response);
    }

    private <T extends Identifiable> void writeCollection(
            EntityPage<T> page,
            QueryOptions query,
            HttpServletResponse response
    ) throws IOException {
        // TODO apply service-root-uri
        CollectionNode<T> node = new CollectionNode<>(page, "http://");

        Class<T> type = page.getEntityType();
        // copy to add query specific select-filter
        ObjectMapper filteringMapper = mapper.copy();
        filteringMapper.addMixIn(type, SelectFilterMixin.class);
        ObjectWriter writer = filteringMapper.writer(createSelectFilter(query));
        writer.writeValue(response.getOutputStream(), node);
    }

    private <T extends Identifiable> EntityPage<T> getCollection(
            Class<T> type,
            QueryOptions query
    ) throws ProviderException, STAInvalidUrlException {
        EntityService<T> entityService = getEntityService(type);
        return entityService.getEntities(query);
    }

    private QueryOptions parseQueryOptions(HttpServletRequest request) throws STAInvalidUrlException {
        validateRequestPath(request);
        String queryString = request.getQueryString();
        QueryOptionsFactory factory = new QueryOptionsFactory();
        return Optional.ofNullable(queryString).map(decodeQueryString())
                .map(factory::createQueryOptions)
                .orElse(factory.createDummy());
    }

    private void validateRequestPath(HttpServletRequest request) throws STAInvalidUrlException {
        String path = (String) request.getAttribute(HandlerMapping.LOOKUP_PATH);
        validator.validateRequestPath(path);
    }

    private FilterProvider createSelectFilter(QueryOptions queryOptions) {
        Set<String> selects = getSelects(queryOptions);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        return filterProvider.addFilter(SELECT_FILTER,
                SimpleBeanPropertyFilter.filterOutAllExcept(selects));
    }

    private Set<String> getSelects(QueryOptions queryOptions) {
        Optional<SelectFilter> optionalFilter = getSelectFilter(queryOptions);
        return optionalFilter.map(SelectFilter::getItems).orElse(Collections.emptySet());
    }

    private Optional<SelectFilter> getSelectFilter(QueryOptions queryOptions) {
        return queryOptions != null
                ? Optional.ofNullable(queryOptions.getSelectFilter())
                : Optional.empty();
    }

    private static Function<String, String> decodeQueryString() {
        return query -> {
            try {
                return URLDecoder.decode(query, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("Encoding not found!");
            }
        };
    }

    private <T extends Identifiable> EntityService<T> getEntityService(Class<T> type) {
        return lookup.getService(type)
                .orElseThrow(() -> new IllegalStateException("No service registered for collection '"
                    + type.getSimpleName() + "'"));
    }

}
