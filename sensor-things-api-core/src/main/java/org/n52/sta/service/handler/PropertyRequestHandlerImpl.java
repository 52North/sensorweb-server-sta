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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.UriResourceValue;
import org.n52.sta.service.request.SensorThingsRequest;
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
public class PropertyRequestHandlerImpl extends AbstractPropertyRequestHandler<SensorThingsRequest, PropertyResponse> {

    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    @Override
    public PropertyResponse handlePropertyRequest(SensorThingsRequest request) throws ODataApplicationException {
        PropertyResponse response = null;

        List<UriResource> resourcePaths = request.getResourcePaths();

        // handle request depending on the number of UriResource paths
        // e.g. the case: sta/Things(id)/Locations(id)/name
        if (resourcePaths.get(1) instanceof UriResourceNavigation) {
            response = resolvePropertyForNavigation(resourcePaths);

            // e.g the case: sta/Things(id)/description
        } else {
            response = resolveSimplePropertyRequest(resourcePaths);

        }
        return response;
    }

    private PropertyResponse resolveSimplePropertyRequest(List<UriResource> resourcePaths) throws ODataApplicationException {
        // determine the response EntitySet
        UriResourceEntitySet uriResourceEntitySet = navigationResolver.resolveRootUriResource(resourcePaths.get(0));
        Entity targetEntity = navigationResolver.resolveSimpleEntityRequest(uriResourceEntitySet);
        List<UriResource> propertyResourcePaths = resourcePaths.subList(1, resourcePaths.size());

        PropertyResponse response = resolveProperty(targetEntity, propertyResourcePaths, uriResourceEntitySet.getEntitySet());

        return response;
    }

    private PropertyResponse resolvePropertyForNavigation(List<UriResource> resourcePaths) throws ODataApplicationException {
        int i = 0;
        UriResource lastEntitySegment = resourcePaths.get(i);
        // note that the last value for i at the end of the loop is the index
        // fot the UriResourceProperty element
        while (resourcePaths.get(++i) instanceof UriResourceNavigation) {
            lastEntitySegment = resourcePaths.get(i);
        }
        // determine the target query parameters and fetch Entity for it
        EntityQueryParams queryParams = navigationResolver.resolveUriResourceNavigationPaths(resourcePaths.subList(0, i));
        Entity targetEntity = navigationResolver.resolveComplexEntityRequest(lastEntitySegment, queryParams);

        PropertyResponse response = null;

        response = resolveProperty(targetEntity, resourcePaths.subList(i, resourcePaths.size()), queryParams.getTargetEntitySet());
        return response;
    }

    private PropertyResponse resolveProperty(Entity targetEntity, List<UriResource> resourcePaths, EdmEntitySet targetEntitySet) throws ODataApplicationException {
        int i = 0;
        EdmProperty edmProperty = ((UriResourceProperty) resourcePaths.get(i)).getProperty();

        // retrieve the property data from the entity
        Property property = targetEntity.getProperty(edmProperty.getName());

        while (resourcePaths.get(i) instanceof UriResourceComplexProperty
                && i != resourcePaths.size() - 1
                && !(resourcePaths.get(++i) instanceof UriResourceValue)) {
            ComplexValue complexValue = (ComplexValue) property.getValue();
            edmProperty = ((UriResourceProperty) resourcePaths.get(i)).getProperty();
            final String edmPropertyName = edmProperty.getName();
            Optional<Property> optProperty = complexValue.getValue().stream()
                    .filter(p -> p.getName().equals(edmPropertyName))
                    .findFirst();
            if (!optProperty.isPresent()) {
                throw new ODataApplicationException("Property not found",
                        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
            }
            property = optProperty.get();
        }

        // set Entity response information
        PropertyResponse response = new PropertyResponse();
        response.setEdmPropertyType(edmProperty.getType());
        response.setProperty(property);
        response.setResponseEdmEntitySet(targetEntitySet);

        return response;
    }
}
