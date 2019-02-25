/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.utils;

import java.util.List;
import java.util.Locale;
import java.util.OptionalLong;
import org.apache.olingo.commons.api.data.Entity;

import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class to resolve URI resources
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class UriResourceNavigationResolver {

    @Autowired
    private EntityServiceRepository serviceRepository;

    /**
     * Resolves the root URI resource as UriResourceEntitySet
     *
     * @param uriResource the URI resource paths to resolve
     * @return the root UriResourceEntitySet
     * @throws ODataApplicationException
     */
    public UriResourceEntitySet resolveRootUriResource(UriResource uriResource) throws ODataApplicationException {

        if (!(uriResource instanceof UriResourceEntitySet)) {
            throw new ODataApplicationException("Only EntitySet is supported",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
        return uriResourceEntitySet;
    }

    /**
     * Resolves complex URI resource Navigation paths to determine the target
     * {@link NavigationLink}
     *
     * @param navigationResourcePaths the URI resource paths to resolve
     * @return the target {@link NavigationLink}
     * @throws ODataApplicationException
     */
    public EntityQueryParams resolveUriResourceNavigationPaths(List<UriResource> navigationResourcePaths) throws ODataApplicationException {
        UriResourceEntitySet uriResourceEntitySet = resolveRootUriResource(navigationResourcePaths.get(0));
        EdmEntitySet targetEntitySet = uriResourceEntitySet.getEntitySet();

        List<UriParameter> sourceKeyPredicates = uriResourceEntitySet.getKeyPredicates();
        EdmEntityType sourceEntityType = uriResourceEntitySet.getEntityType();

        Long sourceEntityId = getEntityIdFromKeyParams(sourceKeyPredicates);
        boolean entityExists = serviceRepository.getEntityService(uriResourceEntitySet.getEntityType().getName())
                .existsEntity(sourceEntityId);

        if (!entityExists) {
            throw new ODataApplicationException("Entity not found.",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }

        EdmEntityType targetEntityType = null;

        for (int navigationCount = 1; navigationCount < navigationResourcePaths.size() - 1; navigationCount++) {
            UriResource targetSegment = navigationResourcePaths.get(navigationCount);

            if (targetSegment instanceof UriResourceNavigation) {
                UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) targetSegment;
                EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
                targetEntityType = edmNavigationProperty.getType();

                List<UriParameter> navKeyPredicates = uriResourceNavigation.getKeyPredicates();
                OptionalLong targetIdOpt = OptionalLong.empty();

                // e.g. /Things(1)/Location
                if (navKeyPredicates.isEmpty()) {

                    targetIdOpt = serviceRepository.getEntityService(targetEntityType.getName())
                            .getIdForRelatedEntity(sourceEntityId, sourceEntityType);
                } else { // e.g. /Things(1)/Locations(1)

                    targetIdOpt = serviceRepository.getEntityService(targetEntityType.getName())
                            .getIdForRelatedEntity(sourceEntityId, sourceEntityType, getEntityIdFromKeyParams(navKeyPredicates));
                }
                if (!targetIdOpt.isPresent()) {
                    throw new ODataApplicationException("Entity not found.",
                            HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
                }

                sourceEntityType = targetEntityType;
                sourceEntityId = targetIdOpt.getAsLong();

                targetEntitySet = getNavigationTargetEntitySet(targetEntitySet, edmNavigationProperty);
            }
        }
        UriResource lastSegment = navigationResourcePaths.get(navigationResourcePaths.size() - 1);
        if (lastSegment instanceof UriResourceNavigation) {
            EdmNavigationProperty edmNavigationProperty = ((UriResourceNavigation) lastSegment).getProperty();
            targetEntitySet = getNavigationTargetEntitySet(targetEntitySet, edmNavigationProperty);
        }
        EntityQueryParams params = new EntityQueryParams();
        params.setSourceEntityType(sourceEntityType);
        params.setSourceKeyPredicates(sourceEntityId);
        params.setTargetEntitySet(targetEntitySet);

        return params;
    }

    /**
     * Determines the target EntitySet for a navigation property
     *
     * @param startEdmEntitySet the EntitySet to start with
     * @param edmNavigationProperty the navigation property from one entity type
     * to another
     * @return the target EntitySet for the navigation property
     * @throws ODataApplicationException
     */
    public static EdmEntitySet getNavigationTargetEntitySet(EdmEntitySet startEdmEntitySet,
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
     * @param requestParams parameters for the Entity request
     * @return the requested Entity
     * @throws ODataApplicationException
     */
    public Entity resolveComplexEntityRequest(UriResource lastSegment, EntityQueryParams requestParams) throws ODataApplicationException {

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
