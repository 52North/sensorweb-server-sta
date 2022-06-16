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

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.n52.janmayen.stream.Streams;
import org.n52.shetland.filter.ExpandFilter;
import org.n52.shetland.filter.ExpandItem;
import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.filter.FilterClause;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.path.SelectPath;
import org.n52.sta.http.controller.RequestContext;
import org.n52.sta.http.util.path.StaPath;
import org.n52.svalbard.odata.core.QueryOptionsFactory;

public class SerializationContext {

    private final String serviceUri;

    private final QueryOptions queryOptions;

    private final ObjectMapper mapper;

    private final Optional<Set<String>> selectFilter;

    private final Optional<Set<ExpandItem>> expandFilter;

    SerializationContext(String serviceUri, QueryOptions queryOptions, ObjectMapper mapper) {
        Objects.requireNonNull(serviceUri, "serviceUri must not be null!");
        Objects.requireNonNull(queryOptions, "queryOptions must not be null!");
        Objects.requireNonNull(mapper, "mapper must not be null");

        this.serviceUri = serviceUri;
        this.queryOptions = queryOptions;
        this.mapper = mapper;

        this.selectFilter = getSelects(queryOptions);
        this.expandFilter = getExpands(queryOptions);

        SimpleModule module = new SimpleModule();
        module.addSerializer(CollectionNode.class, new CollectionNodeSerializer(this));
        mapper.registerModule(module);

        SimpleModule geometryModule = new SimpleModule();
        geometryModule.addSerializer(new GeometrySerializer());
        mapper.registerModule(geometryModule);
    }

    public static SerializationContext create(RequestContext requestContext, ObjectMapper mapperConfig) {
        Objects.requireNonNull(requestContext, "requestContext must not be null");
        Objects.requireNonNull(mapperConfig, "mapperConfig must not be null!");

        StaPath< ? extends Identifiable> path = requestContext.getPath();

        ObjectMapper mapper = mapperConfig.copy();
        String serviceUri = requestContext.getServiceUri();
        QueryOptions queryOptions = requestContext.getQueryOptions();
        if (path.getPathType() == SelectPath.PathType.property || path.getPathType() == SelectPath.PathType.value) {
            // We replace all queryOptions with a single $select=<property> as they do not make sense when requesting
            // a property directly
            Set<FilterClause> filters = new HashSet<>(2);
            filters.add(new SelectFilter(requestContext.getPath().getPathSegments().get(0).getProperty().get()));
            queryOptions = QueryOptionsFactory.createQueryOptions(filters);
        }
        SerializationContext context = new SerializationContext(serviceUri, queryOptions, mapper);

        // jackson registration register serialization context
        StaBaseSerializer< ? extends Identifiable> serializer = path.createSerializer(context);
        context.register(serializer);
        return context;
    }

    public static SerializationContext createInternal(SerializationContext context, QueryOptions queryOptions) {
        Objects.requireNonNull(context, "context must not be null");
        ObjectMapper mapper = context.mapper;
        String serviceUri = context.serviceUri;
        return new SerializationContext(serviceUri, queryOptions, mapper);
    }

    /**
     * Registers specified serializer at the context's object mapper.
     *
     * @param serializer
     *        the serializer to register
     */
    public <T extends Identifiable> void register(StaBaseSerializer<T> serializer) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(serializer.getType(), serializer);
        mapper.registerModule(module);
    }

    public String getServiceUri() {
        return serviceUri;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public ObjectWriter createWriter() {
        return mapper.writer();
    }

    public boolean isSelected(String name) {
        return selectFilter.map(selects -> selects.isEmpty() || selects.contains(name))
                           .orElse(true);
    }

    public boolean isExpanded(String member) {
        if (!expandFilter.isPresent()) {
            return false;
        }
        Set<ExpandItem> expands = expandFilter.get();
        return Streams.stream(expands)
                      .anyMatch(item -> member.equals(item.getPath()));
    }

    public boolean isCounted() {
        return queryOptions.hasCountFilter()
                && queryOptions.getCountFilter()
                               .getValue();
    }

    public Optional<QueryOptions> getQueryOptionsForExpanded(String member) {
        if (!isExpanded(member)) {
            return Optional.empty();
        }
        Set<ExpandItem> expands = expandFilter.get();
        return Streams.stream(expands)
                      .filter(item -> member.equals(item.getPath()))
                      .findFirst()
                      .map(ExpandItem::getQueryOptions);
    }

    private Optional<Set<ExpandItem>> getExpands(QueryOptions queryOptions) {
        Optional<ExpandFilter> optionalFilter = getExpandFilter(queryOptions);
        return optionalFilter.map(ExpandFilter::getItems);
    }

    private Optional<ExpandFilter> getExpandFilter(QueryOptions queryOptions) {
        return queryOptions != null
                ? Optional.ofNullable(queryOptions.getExpandFilter())
                : Optional.empty();
    }

    private Optional<Set<String>> getSelects(QueryOptions queryOptions) {
        Optional<SelectFilter> optionalFilter = getSelectFilter(queryOptions);
        return optionalFilter.map(SelectFilter::getItems);
    }

    private Optional<SelectFilter> getSelectFilter(QueryOptions queryOptions) {
        return queryOptions != null
                ? Optional.ofNullable(queryOptions.getSelectFilter())
                : Optional.empty();
    }

}
