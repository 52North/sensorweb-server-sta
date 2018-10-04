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
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.FixedFormatSerializer;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.PrimitiveValueSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.n52.sta.service.handler.AbstractPropertyRequestHandler;
import org.n52.sta.service.response.PropertyResponse;
import org.n52.sta.service.serializer.SensorThingsSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsPropertyValueProcessor implements PrimitiveValueProcessor {

    private static final ContentType ET_PROPERTY_PROCESSOR_CONTENT_TYPE = ContentType.JSON_NO_METADATA;

    @Autowired
    AbstractPropertyRequestHandler requestHandler;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readPrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        PropertyResponse propertyValueReponse = requestHandler.handlePropertyRequest(uriInfo);

        // serialize
        Object propertyValue = propertyValueReponse.getProperty().getValue();
        if (propertyValue != null) {
            //TODO: check for other than primitive types for the property
            if (propertyValueReponse.getEdmPropertyType() instanceof EdmComplexType) {
                createComplexValueResponse(response, propertyValueReponse);
            } else {
                createPrimitiveValueResponse(response, propertyValueReponse);
            }

        } else {
            // in case there's no value for the property, we can skip the serialization
            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        }
    }

    private void createComplexValueResponse(ODataResponse response, PropertyResponse complexResponse) throws SerializerException {
        SensorThingsSerializer serializer = new SensorThingsSerializer(ET_PROPERTY_PROCESSOR_CONTENT_TYPE);

        ContextURL contextUrl = ContextURL.with().entitySet(complexResponse.getResponseEdmEntitySet()).navOrPropertyPath(complexResponse.getProperty().getName()).build();
        ComplexSerializerOptions options = ComplexSerializerOptions.with().contextURL(contextUrl).build();

        // serialize
        SerializerResult serializerResult = serializer.complexValue(serviceMetadata, (EdmComplexType) complexResponse.getEdmPropertyType(), complexResponse.getProperty(), options);
        InputStream serializedContent = serializerResult.getContent();

        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
    }

    private void createPrimitiveValueResponse(ODataResponse response, PropertyResponse primitiveResponse) throws SerializerException {
        final FixedFormatSerializer serializer = odata.createFixedFormatSerializer();
        PrimitiveValueSerializerOptions options = PrimitiveValueSerializerOptions.with().build();
        InputStream serializedContent = serializer.primitiveValue((EdmPrimitiveType) primitiveResponse.getEdmPropertyType(), primitiveResponse.getProperty().getValue(), options);
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN.toContentTypeString());

    }

    @Override
    public void updatePrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deletePrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        PropertyResponse primitiveResponse = requestHandler.handlePropertyRequest(uriInfo);

        // serialize
        Object value = primitiveResponse.getProperty().getValue();
        if (value != null) {
            //TODO: check for other than primitive types for the property
            InputStream serializedContent = createReponseContent(primitiveResponse.getProperty(),
                    (EdmPrimitiveType) primitiveResponse.getEdmPropertyType(),
                    primitiveResponse.getResponseEdmEntitySet());

            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
        } else {
            // in case there's no value for the property, we can skip the serialization
            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
        }
    }

    @Override
    public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    private InputStream createReponseContent(Property property, EdmPrimitiveType edmPropertyType, EdmEntitySet responseEdmEntitySet) throws SerializerException {

        ODataSerializer serializer = new SensorThingsSerializer(ET_PROPERTY_PROCESSOR_CONTENT_TYPE);

        ContextURL contextUrl = ContextURL.with().entitySet(responseEdmEntitySet).navOrPropertyPath(property.getName()).build();
        PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();

        // serialize
        SerializerResult serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, property, options);
        InputStream propertyStream = serializerResult.getContent();
        return propertyStream;

    }

}
