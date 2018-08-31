/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.service.response.PropertyResponse;
import org.n52.sta.utils.EntityQueryParams;
import org.n52.sta.utils.UriResourceNavigationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 ** Implementation for handling property requests
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class PropertyRequestHandlerImpl implements AbstractPropertyRequestHandler {

    @Autowired
    private EntityServiceRepository serviceRepository;

    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    @Override
    public PropertyResponse handlePrimitiveRequest(UriInfo uriInfo) throws ODataApplicationException {
        PropertyResponse response = null;

        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

        // handle request depending on the number of UriResource paths
        // e.g the case: sta/Things(id)/description
        if (resourcePaths.size() == 2) {
            response = resolvePropertyForEntity(resourcePaths);

            // e.g. the case: sta/Things(id)/Locations(id)/name
        } else {
            response = resolvePropertyForNavigation(resourcePaths);

        }
        return response;
    }

    private PropertyResponse resolvePropertyForEntity(List<UriResource> resourcePaths) throws ODataApplicationException {
        // determine the response EntitySet
        UriResourceEntitySet uriResourceEntitySet = navigationResolver.resolveRootUriResource(resourcePaths.get(0));
        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();

        // fetch the data from backend for this requested Entity and deliver as Entity
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        AbstractSensorThingsEntityService responseService = serviceRepository.getEntityService(uriResourceEntitySet.getEntityType().getName());
        Entity targetEntity = responseService.getEntity(navigationResolver.getEntityIdFromKeyParams(keyPredicates));

        if (targetEntity == null) {
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }
        UriResourceProperty uriProperty = (UriResourceProperty) resourcePaths.get(resourcePaths.size() - 1);

        PropertyResponse response = resolveProperty(targetEntity, uriProperty, responseEntitySet);

        return response;
    }

    private PropertyResponse resolvePropertyForNavigation(List<UriResource> resourcePaths) throws ODataApplicationException {
        // determine the target query parameters and fetch Entity for it
        EntityQueryParams queryParams = navigationResolver.resolveUriResourceNavigationPaths(resourcePaths.subList(0, resourcePaths.size()));

        UriResource lastEntitySegment = resourcePaths.get(resourcePaths.size() - 2);
        Entity targetEntity = null;
        PropertyResponse response = null;
        if (lastEntitySegment instanceof UriResourceNavigation) {

            List<UriParameter> navKeyPredicates = ((UriResourceNavigation) lastEntitySegment).getKeyPredicates();

            // e.g. /HistoricalLocations(id)/Thing/description
            if (navKeyPredicates.isEmpty()) {
                targetEntity = serviceRepository.getEntityService(queryParams.getTargetEntitySet().getEntityType().getName())
                        .getRelatedEntity(queryParams.getSourceId(), queryParams.getSourceEntityType());

            } else { // e.g. /Things(id)/Locations(id)/description
                targetEntity = serviceRepository.getEntityService(queryParams.getTargetEntitySet().getEntityType().getName())
                        .getRelatedEntity(queryParams.getSourceId(), queryParams.getSourceEntityType(), navigationResolver.getEntityIdFromKeyParams(navKeyPredicates));
            }
            if (targetEntity == null) {
                throw new ODataApplicationException("Entity not found.",
                        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
            }

            UriResourceProperty uriProperty = (UriResourceProperty) resourcePaths.get(resourcePaths.size() - 1);

            response = resolveProperty(targetEntity, uriProperty, queryParams.getTargetEntitySet());

        }
        return response;
    }

    private PropertyResponse resolveProperty(Entity targetEntity, UriResourceProperty uriProperty, EdmEntitySet targetEntitySet) throws ODataApplicationException {
        EdmProperty edmProperty = uriProperty.getProperty();
        String edmPropertyName = edmProperty.getName();

        // retrieve the property data from the entity
        Property property = targetEntity.getProperty(edmPropertyName);
        if (property == null) {
            throw new ODataApplicationException("Property not found",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        // set Entity response information
        PropertyResponse response = new PropertyResponse();
        response.setEdmPropertyType(edmProperty.getType());
        response.setProperty(property);
        response.setResponseEdmEntitySet(targetEntitySet);

        return response;
    }

}
