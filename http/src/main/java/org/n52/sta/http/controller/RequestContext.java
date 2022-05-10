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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.svalbard.odata.core.QueryOptionsFactory;

public class RequestContext {
    
    private final String serviceUri;

    private final QueryOptions queryOptions;

    public static RequestContext create(String serviceUri, HttpServletRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        QueryOptions queryOptions = parseQueryOptions(request);

        // TODO parse StaPath

        return new RequestContext(serviceUri, queryOptions);
    }
    
    RequestContext(String serviceUri, QueryOptions queryOptions) {
        Objects.requireNonNull(serviceUri, "serviceUri must not be null");
        Objects.requireNonNull(queryOptions, "request must not be null");
        this.serviceUri = serviceUri;
        this.queryOptions = queryOptions;
    }

    private static QueryOptions parseQueryOptions(HttpServletRequest request) {
        String queryString = request.getQueryString();
        QueryOptionsFactory factory = new QueryOptionsFactory();
        return Optional.ofNullable(queryString).map(decodeQueryString())
                .map(factory::createQueryOptions)
                .orElse(factory.createEmpty());
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
        return queryOptions;
    }

}
