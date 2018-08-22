/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.n52.sta.data.AbstractSensorThingsEntityService;
import org.n52.sta.data.HistoricalLocationService;
import org.n52.sta.data.LocationService;
import org.n52.sta.data.SensorService;
import org.n52.sta.data.ThingService;
import org.n52.sta.edm.provider.entities.ThingEntityProvider;
import org.n52.sta.utils.EntityAnnotator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsEntityCollectionProcessor implements EntityCollectionProcessor {

    private static final ContentType ET_COLLECTION_PROCESSOR_CONTENT_TYPE = ContentType.JSON_NO_METADATA;

    @Autowired
    ThingService thingService;

    @Autowired
    LocationService locationService;

    @Autowired
    HistoricalLocationService historicalLocationService;

    @Autowired
    SensorService sensorService;

    @Autowired
    EntityAnnotator entityAnnotator;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        EdmEntitySet set = getNavigationTargetEntitySet(uriInfo);

        EdmEntitySet responseEntitySet = null;
        EntityCollection responseEntityCollection = null;

        // retrieve the EntitySet for the first URI segment from the uriInfo
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResource uriResource = resourcePaths.get(0);

        if (!(uriResource instanceof UriResourceEntitySet)) {
            throw new ODataApplicationException("Only EntitySet is supported",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
        EdmEntitySet startEntitySet = uriResourceEntitySet.getEntitySet();

        // e.g the case: sta/Things
        if (resourcePaths.size() == 1) {
            responseEntitySet = startEntitySet;

            // fetch the data from backend for this requested EntitySetName and deliver as EntitySet
            AbstractSensorThingsEntityService responseService = getEntityCollection(responseEntitySet.getEntityType().getName());
            responseEntityCollection = responseService.getEntityCollection();

            // e.g. the case: sta/Things(id)/Locations
        } else if (resourcePaths.size() == 2) {
            UriResource lastSegment = resourcePaths.get(1);
            // only navigation segments will be supported here
            if (lastSegment instanceof UriResourceNavigation) {
                UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) lastSegment;
                EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
                EdmEntityType targetEntityType = edmNavigationProperty.getType();

                // retrieve the entity set for repsonse
                responseEntitySet = getNavigationTargetEntitySet(startEntitySet, edmNavigationProperty);

                // fetch the entity for the first URI segment
                List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
            }

        } else {
            int navigationCount = 1;
            List<UriParameter> startPredicates = uriResourceEntitySet.getKeyPredicates();

            while (navigationCount < resourcePaths.size()) {

                UriResource lastSegment = resourcePaths.get(navigationCount);

                if (lastSegment instanceof UriResourceNavigation) {

                    EdmNavigationProperty edmNavigationProperty = ((UriResourceNavigation) lastSegment).getProperty();
                    EdmEntityType targetEntityType = edmNavigationProperty.getType();
                    responseEntitySet = getNavigationTargetEntitySet(startEntitySet, edmNavigationProperty);
                    List<UriParameter> targetPredicates = ((UriResourceNavigation) lastSegment).getKeyPredicates();

//                    Entity sourceEntity 
                    //TODO check path validity
                    startEntitySet = responseEntitySet;
                    startPredicates = targetPredicates;
//                    String targetEntitSetName = ((UriResourceNavigation) lastSegment).getProperty().getName()
                } else {
                    throw new ODataApplicationException("Not supported",
                            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
                }
            }
        }

        InputStream serializedContent = createResponseContent(responseEntityCollection, responseEntitySet, request.getRawBaseUri());

        // 4th: configure the response object: set the body, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ET_COLLECTION_PROCESSOR_CONTENT_TYPE.toContentTypeString());
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    /**
     * Multiplexes Request to different Service Implementations based on
     * requested Entity
     *
     * @param Name of Entity requested
     * @return EntityCollection retrieved from backend
     */
    private AbstractSensorThingsEntityService getEntityCollection(String entityTypeName) {
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

    public EdmEntitySet getNavigationTargetEntitySet(final UriInfoResource uriInfo) throws ODataApplicationException {

        EdmEntitySet entitySet;
        final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

        // First must be entity set (hence function imports are not supported here).
        if (resourcePaths.get(0) instanceof UriResourceEntitySet) {
            entitySet = ((UriResourceEntitySet) resourcePaths.get(0)).getEntitySet();
        } else {
            throw new ODataApplicationException("Invalid resource type.",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        int navigationCount = 0;
        while (entitySet != null
                && ++navigationCount < resourcePaths.size()
                && resourcePaths.get(navigationCount) instanceof UriResourceNavigation) {
            final UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) resourcePaths.get(navigationCount);
            final EdmBindingTarget target = entitySet.getRelatedBindingTarget(uriResourceNavigation.getProperty().getName());
            if (target instanceof EdmEntitySet) {
                entitySet = (EdmEntitySet) target;
            } else {
                throw new ODataApplicationException("Singletons not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                        Locale.ROOT);
            }
        }

        return entitySet;
    }

    private InputStream createResponseContent(EntityCollection responseEntityCollection, EdmEntitySet responseEdmEntitySet, String rawBaseUri) throws SerializerException {
        // annotate the entities
        for (Entity e : responseEntityCollection.getEntities()) {
            entityAnnotator.annotateEntity(e, responseEdmEntitySet.getEntityType(), rawBaseUri);
        }

        // 3rd: create a serializer based on the requested format (json)
        ODataSerializer serializer = odata.createSerializer(ET_COLLECTION_PROCESSOR_CONTENT_TYPE);

        // and serialize the content: transform from the EntitySet object to InputStream
        EdmEntityType edmEntityType = responseEdmEntitySet.getEntityType();
        ContextURL contextUrl = ContextURL.with().entitySet(responseEdmEntitySet).build();

        final String id = rawBaseUri + "/" + responseEdmEntitySet.getName();
        EntityCollectionSerializerOptions opts
                = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl).build();
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, responseEntityCollection, opts);
        InputStream serializedContent = serializerResult.getContent();

        return serializedContent;
    }

}
