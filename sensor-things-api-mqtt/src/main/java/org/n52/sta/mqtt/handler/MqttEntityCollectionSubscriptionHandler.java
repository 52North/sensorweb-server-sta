/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.handler;

import java.util.List;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.n52.sta.mqtt.core.MqttEntityCollectionSubscription;
import org.n52.sta.mqtt.request.SensorThingsMqttRequest;
import org.n52.sta.service.handler.AbstractEntityCollectionRequestHandler;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.utils.EntityQueryParams;
import org.n52.sta.utils.UriResourceNavigationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class MqttEntityCollectionSubscriptionHandler extends AbstractEntityCollectionRequestHandler<SensorThingsMqttRequest, MqttEntityCollectionSubscription> {

    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    @Override
    public MqttEntityCollectionSubscription handleEntityCollectionRequest(SensorThingsMqttRequest request) throws ODataApplicationException {
        MqttEntityCollectionSubscription subscription = null;

        // handle request depending on the number of UriResource paths
        // e.g the case: sta/Things
        if (request.getResourcePaths().size() == 1) {
            subscription = createResponseForEntitySet(request.getTopic(), request.getResourcePaths(), request.getQueryOptions());

            // e.g. the case: sta/Things(id)/Locations
        } else {
            subscription = createResponseForNavigation(request.getTopic(), request.getResourcePaths(), request.getQueryOptions());
        }
        return subscription;
    }

    private MqttEntityCollectionSubscription createResponseForEntitySet(String topic, List<UriResource> resourcePaths, QueryOptions queryOptions) throws ODataApplicationException {
        // determine the response EntitySet
        UriResourceEntitySet uriResourceEntitySet = navigationResolver.resolveRootUriResource(resourcePaths.get(0));
        EdmEntitySet responseEntitySet = uriResourceEntitySet.getEntitySet();
        MqttEntityCollectionSubscription subscription = new MqttEntityCollectionSubscription(topic, queryOptions, null, null, responseEntitySet, responseEntitySet.getEntityType());
        return subscription;
    }

    private MqttEntityCollectionSubscription createResponseForNavigation(String topic, List<UriResource> resourcePaths, QueryOptions queryOptions) throws ODataApplicationException {
        // determine the target query parameters
        EntityQueryParams queryParams = navigationResolver.resolveUriResourceNavigationPaths(resourcePaths);
        MqttEntityCollectionSubscription subscription = new MqttEntityCollectionSubscription(topic, queryOptions, queryParams.getSourceEntityType(), queryParams.getSourceId(), queryParams.getTargetEntitySet(), queryParams.getTargetEntitySet().getEntityType());
        return subscription;
    }

}
