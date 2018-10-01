/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.n52.sta.service.response.PropertyResponse;

/**
 * Abstract class to handle property requests
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public interface AbstractPropertyRequestHandler {

    /**
     * Handles a request for a entity property and creates a response
     *
     * @param uriInfo information for the property request URI
     * @return response that contains data for the property reponse
     * @throws ODataApplicationException
     */
    public PropertyResponse handlePropertyRequest(UriInfo uriInfo) throws ODataApplicationException;

}
