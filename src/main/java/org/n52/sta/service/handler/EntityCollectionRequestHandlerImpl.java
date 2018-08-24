/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.handler;

import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.n52.sta.data.AbstractSensorThingsEntityService;
import org.n52.sta.data.HistoricalLocationService;
import org.n52.sta.data.LocationService;
import org.n52.sta.data.SensorService;
import org.n52.sta.data.ThingService;
import org.n52.sta.service.response.EntityCollectionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EntityCollectionRequestHandlerImpl extends AbstractEntityCollectionRequestHandler {

    @Autowired
    ThingService thingService;

    @Autowired
    LocationService locationService;

    @Autowired
    HistoricalLocationService historicalLocationService;

    @Autowired
    SensorService sensorService;

    @Override
    public EntityCollectionResponse handleEntityCollectionRequest(UriInfo uriInfo) throws ODataApplicationException {
        EntityCollectionResponse response = null;

        // retrieve the EntitySet for the first URI segment from the uriInfo
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResource uriResource = resourcePaths.get(0);

        if (!(uriResource instanceof UriResourceEntitySet)) {
            throw new ODataApplicationException("Only EntitySet is supported",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;

        // e.g the case: sta/Things
        if (resourcePaths.size() == 1) {
            response = createResponseForEntitySet(uriResourceEntitySet);

            // e.g. the case: sta/Things(id)/Locations
        } else if (resourcePaths.size() == 2) {
            response = createResponseForSimpleNavigation(uriResourceEntitySet, uriResource);

            // e.g. the case: sta/Things(id)/Locations(id)/HistoricalLocations
        } else {
            response = createResponseForComplexNavigation(uriResourceEntitySet, resourcePaths);
        }
        return response;
    }

    private EntityCollectionResponse createResponseForEntitySet(UriResourceEntitySet uriResourceEntitySet) {
        EntityCollectionResponse response = new EntityCollectionResponse();

        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();
        response.setEntitySet(responseEntitySet);

        // fetch the data from backend for this requested EntitySetName and deliver as EntitySet
        AbstractSensorThingsEntityService responseService = getEntityService(uriResourceEntitySet.getEntityType().getName());
        EntityCollection responseEntityCollection = responseService.getEntityCollection();
        response.setEntityCollection(responseEntityCollection);

        return response;
    }

    private EntityCollectionResponse createResponseForSimpleNavigation(UriResourceEntitySet uriResourceEntitySet, UriResource lastSegment) throws ODataApplicationException {
        EntityCollectionResponse response = null;

        // only navigation segments will be supported here
        if (lastSegment instanceof UriResourceNavigation) {
            response = new EntityCollectionResponse();
            UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) lastSegment;
            EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
            EdmEntityType targetEntityType = edmNavigationProperty.getType();

            // retrieve the entity set for response
            EdmEntitySet responseEntitySet = getNavigationTargetEntitySet(uriResourceEntitySet.getEntitySet(), edmNavigationProperty);
            response.setEntitySet(responseEntitySet);

            // fetch the entity for the first URI segment
            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
            Entity sourceEntity = getEntityService(uriResourceEntitySet.getEntityType().getName()).getEntity(keyPredicates);

            if (sourceEntity == null) {
                throw new ODataApplicationException("Entity not found.",
                        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
            }

            // fetch the response entity collection
            EntityCollection responseEntityCollection = getEntityService(targetEntityType.getName()).getRelatedEntityCollection(sourceEntity);
            response.setEntityCollection(responseEntityCollection);
        }
        return response;
    }

    private EntityCollectionResponse createResponseForComplexNavigation(UriResourceEntitySet uriResourceEntitySet, List<UriResource> resourcePaths) throws ODataApplicationException {
        EntityCollectionResponse response = null;
        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();

        // fetch the entity for the first URI segment
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        Entity sourceEntity = getEntityService(uriResourceEntitySet.getEntityType().getName()).getEntity(keyPredicates);
        EdmEntityType responseEntityType = null;

        if (sourceEntity == null) {
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }

        int navigationCount = 1;
        while (navigationCount < resourcePaths.size()) {
            UriResource targetSegment = resourcePaths.get(navigationCount);

            if (targetSegment instanceof UriResourceNavigation) {
                UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) targetSegment;
                EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
                EdmEntityType targetEntityType = edmNavigationProperty.getType();

                List<UriParameter> navKeyPredicates = uriResourceNavigation.getKeyPredicates();
                Entity targetEntity = null;

                if (navKeyPredicates.isEmpty()) { // e.g. DemoService.svc/Products(1)/Category
                    targetEntity = getEntityService(targetEntityType.getName()).getRelatedEntity(sourceEntity);
                } else {
                    targetEntity = getEntityService(targetEntityType.getName()).getRelatedEntity(sourceEntity, keyPredicates);
                }
                if (targetEntity == null) {
                    throw new ODataApplicationException("Entity not found.",
                            HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
                }

                sourceEntity = targetEntity;
                responseEntityType = targetEntityType;

                responseEntitySet = getNavigationTargetEntitySet(responseEntitySet, edmNavigationProperty);
            }
        }

        EntityCollection responseEntityCollection = getEntityService(responseEntityType.getName()).getRelatedEntityCollection(sourceEntity);

        response = new EntityCollectionResponse();
        response.setEntitySet(responseEntitySet);
        response.setEntityCollection(responseEntityCollection);
        return response;
    }

    /**
     * Multiplexes Request to different Service Implementations based on
     * requested Entity
     *
     * @param Name of Entity requested
     * @return EntityCollection retrieved from backend
     */
    private AbstractSensorThingsEntityService getEntityService(String entityTypeName) {
        AbstractSensorThingsEntityService entityService = null;

        switch (entityTypeName) {
            case "Thing": {
                entityService = thingService;
                break;
            }
            case "Location": {
                entityService = locationService;
                break;
            }
            case "HistoricalLocation": {
                entityService = historicalLocationService;
                break;
            }
            case "Sensor": {
                entityService = sensorService;
                break;
            }
            default: {
                //TODO: check if we need to do error handling for invalid endpoints

            }
        }
        return entityService;
    }

    private static EdmEntitySet getNavigationTargetEntitySet(EdmEntitySet startEdmEntitySet,
            EdmNavigationProperty edmNavigationProperty)
            throws ODataApplicationException {

        EdmEntitySet navigationTargetEntitySet = null;

        String navPropName = edmNavigationProperty.getName();
        EdmBindingTarget edmBindingTarget = startEdmEntitySet.getRelatedBindingTarget(navPropName);
        if (edmBindingTarget == null) {
            throw new ODataApplicationException("Not supported.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        if (edmBindingTarget instanceof EdmEntitySet) {
            navigationTargetEntitySet = (EdmEntitySet) edmBindingTarget;
        } else {
            throw new ODataApplicationException("Not supported.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        return navigationTargetEntitySet;
    }

}
