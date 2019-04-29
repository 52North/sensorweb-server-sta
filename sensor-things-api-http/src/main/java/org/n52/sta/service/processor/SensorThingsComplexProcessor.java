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
package org.n52.sta.service.processor;

import java.io.InputStream;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.n52.sta.service.handler.AbstractPropertyRequestHandler;
import org.n52.sta.service.request.SensorThingsRequest;
import org.n52.sta.service.response.PropertyResponse;
import org.n52.sta.service.serializer.SensorThingsSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsComplexProcessor implements ComplexProcessor {

    @Autowired
    AbstractPropertyRequestHandler<SensorThingsRequest, PropertyResponse> requestHandler;

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private ODataSerializer serializer;

    @Override
    public void readComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        PropertyResponse complexResponse = requestHandler.handlePropertyRequest(new SensorThingsRequest(uriInfo.getUriResourceParts(), null));

        // serialize
        Object value = complexResponse.getProperty().getValue();
        if (value != null) {

            InputStream serializedContent = createReponseContent(complexResponse.getProperty(),
                    (EdmComplexType) complexResponse.getEdmPropertyType(),
                    complexResponse.getResponseEdmEntitySet());

            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
        } else {
            // in case there's no value for the property, we can skip the serialization
            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        }
    }

    private InputStream createReponseContent(Property property, EdmComplexType edmPropertyType, EdmEntitySet responseEdmEntitySet) throws SerializerException {

        ContextURL contextUrl = ContextURL.with().entitySet(responseEdmEntitySet).navOrPropertyPath(property.getName()).build();
        ComplexSerializerOptions options = ComplexSerializerOptions.with()
                .contextURL(contextUrl)
                .build();

        // serialize
        SerializerResult serializerResult = serializer.complex(serviceMetadata, edmPropertyType, property, options);
        InputStream propertyStream = serializerResult.getContent();
        return propertyStream;

    }

    @Override
    public void updateComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
        this.serializer = new SensorThingsSerializer(ContentType.JSON_NO_METADATA);
    }

}
