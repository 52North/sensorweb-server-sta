/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsErrorProcessor implements ErrorProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void processError(ODataRequest request, ODataResponse response, ODataServerError error, ContentType contentType) {
        try {
            // create a serializer based on json format
            ODataSerializer serializer = odata.createSerializer(ContentType.JSON_FULL_METADATA);

            SerializerResult serializerResult = serializer.error(error);
            InputStream serializedContent = serializerResult.getContent();

            // Finally: configure the response object: set the body, headers and status code
            response.setContent(serializedContent);
            response.setStatusCode(error.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
        } catch (SerializerException ex) {
            Logger.getLogger(SensorThingsErrorProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void init(OData odata, ServiceMetadata metadata) {
        this.odata = odata;
        this.serviceMetadata = metadata;
    }

}
