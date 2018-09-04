/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.InputStream;
import java.util.Locale;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.n52.sta.service.handler.AbstractPropertyRequestHandler;
import org.n52.sta.service.handler.PropertyRequestHandlerImpl;
import org.n52.sta.service.response.PropertyResponse;
import org.n52.sta.utils.EntityAnnotator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsPropertyProcessor implements PrimitiveProcessor {

    @Autowired
    AbstractPropertyRequestHandler requestHandler;

    @Autowired
    EntityAnnotator entityAnnotator;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

        PropertyResponse primitiveResponse = requestHandler.handlePrimitiveRequest(uriInfo);

        // serialize
        Object value = primitiveResponse.getProperty().getValue();
        if (value != null) {
            //TODO: check for other than primitive types for the property
            InputStream serializedContent = createReponseContent(primitiveResponse.getProperty(),
                    (EdmPrimitiveType) primitiveResponse.getEdmPropertyType(),
                    primitiveResponse.getResponseEdmEntitySet());

            //TODO: check for other than primitive types for the property
            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
        } else {
            // in case there's no value for the property, we can skip the serialization
            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        }
    }

    @Override
    public void updatePrimitive(ODataRequest odr, ODataResponse odr1, UriInfo ui, ContentType ct, ContentType ct1) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void deletePrimitive(ODataRequest odr, ODataResponse odr1, UriInfo ui) throws ODataApplicationException, ODataLibraryException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    private InputStream createReponseContent(Property property, EdmPrimitiveType edmPropertyType, EdmEntitySet responseEdmEntitySet) throws SerializerException {

        ODataSerializer serializer = odata.createSerializer(ContentType.JSON_FULL_METADATA);

        ContextURL contextUrl = ContextURL.with().entitySet(responseEdmEntitySet).navOrPropertyPath(property.getName()).build();
        PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();
        // 3.2. serialize
        SerializerResult serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, property, options);
        InputStream propertyStream = serializerResult.getContent();
        return propertyStream;

    }

}
