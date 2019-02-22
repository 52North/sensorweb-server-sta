/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import org.apache.olingo.server.api.ODataApplicationException;
import org.n52.sta.service.request.SensorThingsRequest;
import org.n52.sta.service.response.PropertyResponse;

/**
 * Abstract class to handle property requests
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractPropertyRequestHandler<T> {

    /**
     * Handles a request for a entity property and creates a response
     *
     * @param request {@Link SensorThingsRequest) for an entity property
     * @return response that contains data for the property reponse
     * @throws ODataApplicationException
     */
    public abstract T handlePropertyRequest(SensorThingsRequest request) throws ODataApplicationException;

}
