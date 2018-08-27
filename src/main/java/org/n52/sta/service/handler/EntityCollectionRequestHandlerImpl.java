/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import org.n52.sta.utils.NavigationLink;
import java.util.List;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.n52.sta.data.AbstractSensorThingsEntityService;
import org.n52.sta.data.EntityServiceRepository;
import org.n52.sta.service.response.EntityCollectionResponse;
import org.n52.sta.utils.UriResourceNavigationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation for handling EntityCollection requests
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EntityCollectionRequestHandlerImpl implements AbstractEntityCollectionRequestHandler {

    @Autowired
    private EntityServiceRepository serviceRepository;

    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    @Override
    public EntityCollectionResponse handleEntityCollectionRequest(UriInfo uriInfo) throws ODataApplicationException {
        EntityCollectionResponse response = null;

        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

        // handle request depending on the number of UriResource paths
        // e.g the case: sta/Things
        if (resourcePaths.size() == 1) {
            response = createResponseForEntitySet(resourcePaths);

            // e.g. the case: sta/Things(id)/Locations
        } else {
            response = createResponseForNavigation(resourcePaths);

        }
        return response;
    }

    private EntityCollectionResponse createResponseForEntitySet(List<UriResource> resourcePaths) throws ODataApplicationException {

        // determine the response EntitySet
        UriResourceEntitySet uriResourceEntitySet = navigationResolver.resolveRootUriResource(resourcePaths.get(0));
        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();

        // fetch the data from backend for this requested EntitySetName and deliver as EntityCollection
        AbstractSensorThingsEntityService responseService = serviceRepository.getEntityService(uriResourceEntitySet.getEntityType().getName());
        EntityCollection responseEntityCollection = responseService.getEntityCollection();

        // set EntityCollection response information
        EntityCollectionResponse response = new EntityCollectionResponse();
        response.setEntitySet(responseEntitySet);
        response.setEntityCollection(responseEntityCollection);

        return response;
    }

    private EntityCollectionResponse createResponseForNavigation(List<UriResource> resourcePaths) throws ODataApplicationException {

        // determine the last NavigationLink and fetch EntityCollection for it
        NavigationLink lastNavigationLink = navigationResolver.resolveUriResourceNavigationPaths(resourcePaths);
        EntityCollection responseEntityCollection = serviceRepository.getEntityService(lastNavigationLink.getTargetEntitySet().getEntityType().getName())
                .getRelatedEntityCollection(lastNavigationLink.getSourceEntity());

        // set EntityCollection response information
        EntityCollectionResponse response = new EntityCollectionResponse();
        response.setEntitySet(lastNavigationLink.getTargetEntitySet());
        response.setEntityCollection(responseEntityCollection);
        return response;
    }

}
