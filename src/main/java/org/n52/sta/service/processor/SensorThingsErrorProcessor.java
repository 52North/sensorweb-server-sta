/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.InputStream;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ODataServerError;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.ErrorProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsErrorProcessor implements ErrorProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(SensorThingsErrorProcessor.class);

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void processError(ODataRequest request, ODataResponse response, ODataServerError error, ContentType contentType) {
        try {
            LOG.debug("Error occurred.", error.getException());
            // create a serializer based on json format
            ODataSerializer serializer = odata.createSerializer(ContentType.JSON_FULL_METADATA);

            SerializerResult serializerResult = serializer.error(error);
            InputStream serializedContent = serializerResult.getContent();

            // Finally: configure the response object: set the body, headers and status code
            response.setContent(serializedContent);
            response.setStatusCode(error.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
        } catch (SerializerException ex) {
            LOG.error(ex.getMessage());
            LOG.debug("Error while serializing ODataServerError", ex);
        }
    }

    @Override
    public void init(OData odata, ServiceMetadata metadata) {
        this.odata = odata;
        this.serviceMetadata = metadata;
    }

}
