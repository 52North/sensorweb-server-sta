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
     * @param resourcePaths list of {@Link UriResource}
     * @param queryOptions {@Link QueryOptions} for the request
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
