/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.utils.EntityQueryParams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class to handle Entity requests
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractEntityRequestHandler<T, S> {

    @Autowired
    private EntityServiceRepository serviceRepository;

    /**
     * Handle a request for a Entity an creates a response
     *
     * @param request {@Link SensorThingsRequest} for an Entity
     * @return response that contains data for the Entity
     * @throws ODataApplicationException
     */
    public abstract S handleEntityRequest(T request) throws ODataApplicationException;

    /**
     * Resolves a simple entity request where the root UriResource contains the
     * Entity request information (e.g. ./Thing(1)
     *
     * @param uriResourceEntitySet the root UriResource that contains EntitySet
     * information about the requested Entity
     * @return the requested Entity
     * @throws ODataApplicationException
     */
    public Entity resolveSimpleEntityRequest(UriResourceEntitySet uriResourceEntitySet) throws ODataApplicationException {
        // fetch the data from backend for this requested Entity and deliver as Entity
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        AbstractSensorThingsEntityService<?, ?> responseService = getEntityService(uriResourceEntitySet);
        Entity responseEntity = responseService.getEntity(getEntityIdFromKeyParams(keyPredicates));

        if (responseEntity == null) {
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }
        return responseEntity;
    }

    /**
     * Resolves a complex entity request that conatins one or more navigation
     * paths (e.g. ./Thing(1)/Datastreams(1)/Observations(1)
     *
     * @param resourcePaths the URI resource paths of the request
     * @param requestParams parameters for the Entity request
     * @return the requested Entity
     * @throws ODataApplicationException
     */
    public Entity resolveComplexEntityRequest(List<UriResource> resourcePaths, EntityQueryParams requestParams) throws ODataApplicationException {

        UriResource lastSegment = resourcePaths.get(resourcePaths.size() - 1);
        Entity responseEntity = null;

        if (lastSegment instanceof UriResourceNavigation) {

            List<UriParameter> navKeyPredicates = ((UriResourceNavigation) lastSegment).getKeyPredicates();

            // e.g. /Things(1)/Location
            if (navKeyPredicates.isEmpty()) {
                responseEntity = serviceRepository.getEntityService(requestParams.getTargetEntitySet().getEntityType().getName())
                        .getRelatedEntity(requestParams.getSourceId(), requestParams.getSourceEntityType());

            } else { // e.g. /Things(1)/Locations(1)
                responseEntity = serviceRepository.getEntityService(requestParams.getTargetEntitySet().getEntityType().getName())
                        .getRelatedEntity(requestParams.getSourceId(), requestParams.getSourceEntityType(), getEntityIdFromKeyParams(navKeyPredicates));
            }
            if (responseEntity == null) {
                throw new ODataApplicationException("Entity not found.",
                        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
            }
        }
        return responseEntity;
    }

    public Long getEntityIdFromKeyParams(List<UriParameter> keyParams) {
        return Long.parseLong(keyParams.get(0).getText());
    }

    private AbstractSensorThingsEntityService<?, ?> getEntityService(UriResourceEntitySet uriResourceEntitySet) {
        return getUriResourceEntitySet(uriResourceEntitySet.getEntityType().getName());
    }

    private AbstractSensorThingsEntityService<?, ?> getUriResourceEntitySet(String type) {
        return serviceRepository.getEntityService(type);
    }

}
