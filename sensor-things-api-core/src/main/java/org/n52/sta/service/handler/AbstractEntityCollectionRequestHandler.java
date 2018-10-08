/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import java.util.List;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.response.EntityCollectionResponse;

/**
 * Abstract class to handle EntityCollection requests
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public interface AbstractEntityCollectionRequestHandler {

    /**
     * Handles a request for a EntityCollection and creates a response
     *
     * @param uriInfo
     *            information for the EntityCollection request URI
     * @param string
     * @return response that contains data for the EntityCollection reponse
     * @throws ODataApplicationException
     */
    public abstract EntityCollectionResponse handleEntityCollectionRequest(List<UriResource> resourcePaths,
            QueryOptions queryOptions) throws ODataApplicationException;

}
