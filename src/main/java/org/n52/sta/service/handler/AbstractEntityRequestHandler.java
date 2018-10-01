/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import java.util.List;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.n52.sta.service.response.EntityResponse;

/**
 * Abstract class to handle Entity requests
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public interface AbstractEntityRequestHandler {

    /**
     * Handle a request for a Entity an creates a response
     *
     * @param uriInfo information for the Entity request URI
     * @return response that contains data for the Entity reponse
     * @throws ODataApplicationException
     */
    public abstract EntityResponse handleEntityCollectionRequest(List<UriResource> resourcePaths) throws ODataApplicationException;

}
