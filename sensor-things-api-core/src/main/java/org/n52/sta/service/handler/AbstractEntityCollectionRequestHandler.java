/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.sta.service.request.SensorThingsRequest;

/**
 * Abstract class to handle EntityCollection requests
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractEntityCollectionRequestHandler<T> {

    /**
     * Handles a request for a EntityCollection and creates a response
     *
     * @param request {@Link SensorThingsRequest} for a en EntityCollection
     * @return response that contains data for the EntityCollection
     * @throws ODataApplicationException
     */
    public abstract T handleEntityCollectionRequest(SensorThingsRequest request) throws ODataApplicationException;

}
