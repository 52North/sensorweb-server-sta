/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.response.EntityCollectionResponse;
import org.n52.sta.utils.EntityQueryParams;
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
    public EntityCollectionResponse handleEntityCollectionRequest(List<UriResource> resourcePaths, QueryOptions queryOptions) throws ODataApplicationException {
        EntityCollectionResponse response = null;

        // handle request depending on the number of UriResource paths
        // e.g the case: sta/Things
        if (resourcePaths.size() == 1) {
            response = createResponseForEntitySet(resourcePaths, queryOptions);

            // e.g. the case: sta/Things(id)/Locations
        } else {
            response = createResponseForNavigation(resourcePaths, queryOptions);

        }
        return response;
    }

    private EntityCollectionResponse createResponseForEntitySet(List<UriResource> resourcePaths,
            QueryOptions queryOptions) throws ODataApplicationException {

        // determine the response EntitySet
        UriResourceEntitySet uriResourceEntitySet = navigationResolver.resolveRootUriResource(resourcePaths.get(0));
        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();

        // fetch the data from backend for this requested EntitySetName and
        // deliver as EntityCollection
        AbstractSensorThingsEntityService<?> responseService =
                serviceRepository.getEntityService(uriResourceEntitySet.getEntityType().getName());
        EntityCollection responseEntityCollection = responseService.getEntityCollection(queryOptions);
        
        long count = responseService.getCount();
        
        if (queryOptions.hasCountOption()) {
            responseEntityCollection.setCount(Long.valueOf(count).intValue());
        }
        
        responseEntityCollection.setNext(createNext(count, queryOptions, null));
        
        // set EntityCollection response information
        EntityCollectionResponse response = new EntityCollectionResponse();
        response.setEntitySet(responseEntitySet);
        response.setEntityCollection(responseEntityCollection);

        return response;
    }

    private EntityCollectionResponse createResponseForNavigation(List<UriResource> resourcePaths, QueryOptions queryOptions) throws ODataApplicationException {

        // determine the target query parameters and fetch EntityCollection for it
        EntityQueryParams queryParams = navigationResolver.resolveUriResourceNavigationPaths(resourcePaths);

        AbstractSensorThingsEntityService<?> entityService = serviceRepository.getEntityService(queryParams.getTargetEntitySet().getEntityType().getName());
        EntityCollection responseEntityCollection = entityService
                .getRelatedEntityCollection(queryParams.getSourceId(), queryParams.getSourceEntityType(), queryOptions);
        
        long count = entityService.getRelatedEntityCollectionCount(queryParams.getSourceId(), queryParams.getSourceEntityType());
        
        responseEntityCollection.setNext(createNext(count, queryOptions, queryParams));
        // set EntityCollection response information
        EntityCollectionResponse response = new EntityCollectionResponse();
        response.setEntitySet(queryParams.getTargetEntitySet());
        response.setEntityCollection(responseEntityCollection);
        return response;
    }

    private URI createNext(long maxCount, QueryOptions queryOptions, EntityQueryParams queryParams) {
        int currentCount = queryOptions.hasSkipOption()
                ? queryOptions.getSkipOption().getValue() + queryOptions.getTopOption().getValue()
                : queryOptions.getTopOption().getValue();
        if (maxCount > currentCount) {
            StringBuilder builder = new StringBuilder(queryOptions.getBaseURI());
            for (UriResource resource : queryOptions.getUriInfo().getUriResourceParts()) {
                builder.append("/").append(resource.toString());
                if (queryParams != null && resource.toString().startsWith(queryParams.getSourceEntityType().getName())) {
                    builder.append("(").append(queryParams.getSourceId()).append(")");
                }
            }
            builder.append("?");
            builder.append(SystemQueryOptionKind.SKIP).append("=").append(currentCount);
            builder.append("&").append(SystemQueryOptionKind.TOP).append("=").append(queryOptions.getTopOption().getValue());
            try {
                return new URI(builder.toString());
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

}
