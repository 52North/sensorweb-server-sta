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
package org.n52.sta.service.handler;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;

import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.n52.sta.service.query.QueryOptionsHandler;
import org.n52.sta.service.request.SensorThingsRequest;
import org.n52.sta.service.response.EntityResponse;
import org.n52.sta.utils.EntityAnnotator;
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
public class EntityRequestHandlerImpl extends AbstractEntityRequestHandler<SensorThingsRequest, EntityResponse> {

    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    @Autowired
    private QueryOptionsHandler queryOptionsHandler;

    @Autowired
    EntityAnnotator entityAnnotator;

    @Override
    public EntityResponse handleEntityRequest(SensorThingsRequest request) throws ODataApplicationException {
        EntityResponse response = null;

        // handle request depending on the number of UriResource paths
        // e.g the case: sta/Things(id)
        if (request.getResourcePaths().size() == 1) {
            response = createResponseForEntity(request.getResourcePaths());

            // e.g. the case: sta/Things(id)/Locations(id)
        } else {
            response = createResponseForNavigation(request.getResourcePaths());
        }

        if (request.getQueryOptions().hasExpandOption()) {
            entityAnnotator.annotateEntity(
                    response.getEntity(),
                    response.getEntitySet().getEntityType(),
                    request.getQueryOptions().getBaseURI(),
                    request.getQueryOptions().getSelectOption());
            queryOptionsHandler.handleExpandOption(
                    response.getEntity(),
                    request.getQueryOptions().getExpandOption(),
                    Long.parseLong(response.getEntity().getProperty(PROP_ID).getValue().toString()),
                    response.getEntitySet().getEntityType(),
                    request.getQueryOptions().getBaseURI());
        } else {
            entityAnnotator.annotateEntity(
                    response.getEntity(),
                    response.getEntitySet().getEntityType(),
                    request.getQueryOptions().getBaseURI(),
                    request.getQueryOptions().getSelectOption());
        }
        return response;
    }

    private EntityResponse createResponseForEntity(List<UriResource> resourcePaths) throws ODataApplicationException {

        UriResourceEntitySet uriResourceEntitySet = navigationResolver.resolveRootUriResource(resourcePaths.get(0));
        Entity responseEntity = navigationResolver.resolveSimpleEntityRequest(uriResourceEntitySet);

        // set Entity response information
        EntityResponse response = new EntityResponse();
        response.setEntitySet(uriResourceEntitySet.getEntitySet());
        response.setEntity(responseEntity);

        return response;
    }

    private EntityResponse createResponseForNavigation(List<UriResource> resourcePaths) throws ODataApplicationException {
        // determine the target query parameters and fetch Entity for it
        EntityQueryParams requestParams = navigationResolver.resolveUriResourceNavigationPaths(resourcePaths);
        UriResource lastSegment = resourcePaths.get(resourcePaths.size() - 1);
        Entity responseEntity = navigationResolver.resolveComplexEntityRequest(lastSegment, requestParams);

        // set EntityCollection response information
        EntityResponse response = new EntityResponse();
        response.setEntitySet(requestParams.getTargetEntitySet());
        response.setEntity(responseEntity);
        return response;
    }

    private UriResourceEntitySet getUriResourceEntitySet(List<UriResource> resourcePaths) throws ODataApplicationException {
        return navigationResolver.resolveRootUriResource(resourcePaths.get(0));
    }

}
