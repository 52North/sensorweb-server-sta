/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;
import org.n52.sta.mqtt.core.MQTTSubscription;
import org.n52.sta.service.handler.AbstractEntityCollectionRequestHandler;
import org.n52.sta.service.handler.AbstractEntityRequestHandler;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.query.URIQueryOptions;
import org.n52.sta.service.response.EntityCollectionResponse;
import org.n52.sta.service.response.EntityResponse;
import org.n52.sta.utils.EntityQueryParams;
import org.n52.sta.utils.UriResourceNavigationResolver;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class MqttEntityCollectionSubscriptionHandler {

//    @Autowired
//    private UriResourceNavigationResolver navigationResolver;
//
//    public MQTTSubscription handleEntityCollectionRequest(String topic, List<UriResource> resourcePaths, QueryOptions queryOptions) throws ODataApplicationException {
//        MQTTSubscription response = null;
//
//        // handle request depending on the number of UriResource paths
//        // e.g the case: sta/Things
//        if (resourcePaths.size() == 1) {
//            response = createResponseForEntitySet(topic, resourcePaths, queryOptions);
//
//            // e.g. the case: sta/Things(id)/Locations
//        } else {
//            response = createResponseForNavigation(resourcePaths, queryOptions);
//        }
//        return response;
//    }
//
//    private MQTTSubscription createResponseForEntitySet(String topic, List<UriResource> resourcePaths, QueryOptions queryOptions) throws ODataApplicationException {
//        List<SelectItem> fields = new ArrayList();
//        Set<String> watchedProperties = new HashSet();
//        String olingoEntityType = null;
//        boolean isCollection = true;
//        Long entityId = null;
//
//        if (queryOptions.hasSelectOption()) {
//            fields = queryOptions.getSelectOption().getSelectItems();
//        }
//
//        // determine the response EntitySet
//        UriResourceEntitySet uriResourceEntitySet = navigationResolver.resolveRootUriResource(resourcePaths.get(0));
//        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();
//        MQTTSubscription subscription = new MQTTSubscription(topic, fields, olingoEntityType, watchedProperties, isCollection, entityId, queryOptions, responseEntitySet, responseEntitySet.getEntityType());
//        return subscription;
//    }
//
//    private MQTTSubscription createResponseForNavigation(String topic, List<UriResource> resourcePaths, QueryOptions queryOptions) throws ODataApplicationException {
//        // determine the target query parameters and fetch EntityCollection for it
//        EntityQueryParams queryParams = navigationResolver.resolveUriResourceNavigationPaths(resourcePaths);
//
//    }

}
