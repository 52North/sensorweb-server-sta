/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.request;

import java.util.List;
import org.apache.olingo.server.api.uri.UriResource;
import org.n52.sta.service.query.QueryOptions;

/**
 * Encapsulates parameters for an OData complient Sensor Things API request that
 * will be used to resolve the requested entities.
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class SensorThingsRequest {

    private List<UriResource> resourcePaths;

    private QueryOptions queryOptions;

    public SensorThingsRequest() {
    }

    /**
     *
     * @param resourcePaths list of {@link UriResource}
     * @param queryOptions {@link QueryOptions} for the request
     */
    public SensorThingsRequest(List<UriResource> resourcePaths, QueryOptions queryOptions) {
        this.resourcePaths = resourcePaths;
        this.queryOptions = queryOptions;
    }

    public List<UriResource> getResourcePaths() {
        return resourcePaths;
    }

    public void setResourcePaths(List<UriResource> resourcePaths) {
        this.resourcePaths = resourcePaths;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public void setQueryOptions(QueryOptions queryOptions) {
        this.queryOptions = queryOptions;
    }

}
