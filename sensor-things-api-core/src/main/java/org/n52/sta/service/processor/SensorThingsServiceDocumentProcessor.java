/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.InputStream;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.ServiceDocumentProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.n52.sta.service.serializer.SensorThingsSerializer;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsServiceDocumentProcessor implements ServiceDocumentProcessor {

    private static final ContentType ET_SERVICE_DOCUMENT_PROCESSOR_CONTENT_TYPE = ContentType.JSON_FULL_METADATA;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readServiceDocument(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        // create a serializer based on json format
//        ODataSerializer serializer = odata.createSerializer(ET_SERVICE_DOCUMENT_PROCESSOR_CONTENT_TYPE);
        ODataSerializer serializer = new SensorThingsSerializer(contentType);

        SerializerResult serializerResult = serializer.serviceDocument(serviceMetadata, request.getRawBaseUri());
        InputStream serializedContent = serializerResult.getContent();

        // Finally: configure the response object: set the body, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ET_SERVICE_DOCUMENT_PROCESSOR_CONTENT_TYPE.toContentTypeString());
    }

    @Override
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
        this.serviceMetadata = sm;
    }

}
