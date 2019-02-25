/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import org.apache.olingo.server.api.ODataApplicationException;

/**
 * Abstract class to handle Entity requests
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractEntityRequestHandler<T, S> {

    /**
     * Handle a request for a Entity an creates a response
     *
     * @param request {@Link SensorThingsRequest} for an Entity
     * @return response that contains data for the Entity
     * @throws ODataApplicationException
     */
    public abstract S handleEntityRequest(T request) throws ODataApplicationException;

}
