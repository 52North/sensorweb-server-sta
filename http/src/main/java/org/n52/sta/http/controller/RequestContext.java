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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.n52.shetland.filter.SelectFilter;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.sta.api.entity.Identifiable;
import org.n52.sta.api.path.Request;
import org.n52.sta.api.path.SelectPath;
import org.n52.sta.http.util.path.PathFactory;
import org.n52.sta.http.util.path.StaPath;
import org.n52.svalbard.odata.core.QueryOptionsFactory;
import org.springframework.web.util.UrlPathHelper;

public final class RequestContext {

    private final String serviceUri;

    private final Request request;

    private RequestContext(String serviceUri, StaPath< ? extends Identifiable> path, QueryOptions queryOptions) {
        Objects.requireNonNull(serviceUri, "serviceUri must not be null");
        this.request = new Request(path, queryOptions);
        this.serviceUri = serviceUri;
    }

    public static RequestContext create(String serviceUri, HttpServletRequest request, PathFactory pathFactory)
            throws STAInvalidUrlException {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(pathFactory, "pathFactory must not be null");
        StaPath< ? extends Identifiable> path = parsePath(request, pathFactory);
        QueryOptions queryOptions = path.isRef()
                ? createRefQueryOptions()
                : parseQueryOptions(request);
        return new RequestContext(serviceUri, path, queryOptions);
    }

    private static QueryOptions createRefQueryOptions() {
        SelectFilter refFilterClause = new SelectFilter(StaConstants.PROP_SELF_LINK);
        return new QueryOptions(Collections.singleton(refFilterClause));
    }

    private static QueryOptions parseQueryOptions(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return Optional.ofNullable(queryString)
                       .map(decodeQueryString())
                       .map(QueryOptionsFactory::createQueryOptions)
                       .orElse(QueryOptionsFactory.createEmpty());
    }

    private static StaPath< ? extends Identifiable> parsePath(HttpServletRequest request, PathFactory pathFactory)
            throws STAInvalidUrlException {
        return pathFactory.parse((String) request.getAttribute(UrlPathHelper.PATH_ATTRIBUTE));
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

    public String getServiceUri() {
        return serviceUri;
    }

    public QueryOptions getQueryOptions() {
        return request.getQueryOptions();
    }

    public Request getRequest() {
        return request;
    }

    public StaPath< ? extends Identifiable> getPath() {
        Optional<SelectPath< ? extends Identifiable>> path = request.getPath();
        // We are inside an actual RequestContext so Path should be always present
        return (StaPath< ? extends Identifiable>) path.orElseThrow(() -> new IllegalStateException("No path present!"));
    }

}
