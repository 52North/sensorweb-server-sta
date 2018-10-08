/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.service.response.EntityResponse;
import org.n52.sta.utils.EntityQueryParams;
import org.n52.sta.utils.UriResourceNavigationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation for handling Entity requests
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EntityRequestHandlerImpl implements AbstractEntityRequestHandler {

    @Autowired
    private EntityServiceRepository serviceRepository;

    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    @Override
    public EntityResponse handleEntityCollectionRequest(List<UriResource> resourcePaths) throws ODataApplicationException {
        EntityResponse response = null;

        // handle request depending on the number of UriResource paths
        // e.g the case: sta/Things(id)
        if (resourcePaths.size() == 1) {
            response = createResponseForEntity(resourcePaths);

            // e.g. the case: sta/Things(id)/Locations(id)
        } else {
            response = createResponseForNavigation(resourcePaths);

        }
        return response;
    }

    private EntityResponse createResponseForEntity(List<UriResource> resourcePaths) throws ODataApplicationException {

        // determine the response EntitySet
        UriResourceEntitySet uriResourceEntitySet = navigationResolver.resolveRootUriResource(resourcePaths.get(0));
        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();

        // fetch the data from backend for this requested Entity and deliver as Entity
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        AbstractSensorThingsEntityService<?> responseService = serviceRepository.getEntityService(uriResourceEntitySet.getEntityType().getName());
        Entity responseEntity = responseService.getEntity(navigationResolver.getEntityIdFromKeyParams(keyPredicates));

        if (responseEntity == null) {
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }

        // set Entity response information
        EntityResponse response = new EntityResponse();
        response.setEntitySet(responseEntitySet);
        response.setEntity(responseEntity);

        return response;
    }

    private EntityResponse createResponseForNavigation(List<UriResource> resourcePaths) throws ODataApplicationException {
        // determine the target query parameters and fetch Entity for it
        EntityQueryParams requestParams = navigationResolver.resolveUriResourceNavigationPaths(resourcePaths);

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
                        .getRelatedEntity(requestParams.getSourceId(), requestParams.getSourceEntityType(), navigationResolver.getEntityIdFromKeyParams(navKeyPredicates));
            }
            if (responseEntity == null) {
                throw new ODataApplicationException("Entity not found.",
                        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
            }
        }

        // set EntityCollection response information
        EntityResponse response = new EntityResponse();
        response.setEntitySet(requestParams.getTargetEntitySet());
        response.setEntity(responseEntity);
        return response;
    }
}
