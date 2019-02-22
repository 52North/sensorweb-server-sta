/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometry;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.FixedFormatSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.PrimitiveValueSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.core.serializer.SerializerResultImpl;
import org.apache.olingo.server.core.serializer.utils.CircleStreamBuffer;
import org.n52.sta.service.handler.AbstractPropertyRequestHandler;
import org.n52.sta.service.response.PropertyResponse;
import org.n52.sta.service.serializer.SensorThingsSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsPropertyValueProcessor implements PrimitiveValueProcessor {

    @Autowired
    AbstractPropertyRequestHandler requestHandler;

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private SensorThingsSerializer serializer;
    private FixedFormatSerializer fixedFormatSerializer;

    @Override
    public void readPrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        PropertyResponse propertyValueReponse = requestHandler.handlePropertyRequest(uriInfo);

        // serialize
        Object propertyValue = propertyValueReponse.getProperty().getValue();
//        if (propertyValue != null) {
            //TODO: check for other than primitive types for the property

            if (propertyValueReponse.getEdmPropertyType() instanceof EdmComplexType) {
                createComplexValueResponse(response, propertyValueReponse);
            } else if (propertyValueReponse.getEdmPropertyType() instanceof EdmGeometry) {
                createGeospatialValueResponse(response, propertyValueReponse);
            } else {
                createPrimitiveValueResponse(response, propertyValueReponse);
            }

//        } else {
//            // in case there's no value for the property, we can skip the serialization
//            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
//        }
    }

    private void createComplexValueResponse(ODataResponse response, PropertyResponse complexResponse) throws SerializerException {
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
        PrimitiveValueSerializerOptions options = PrimitiveValueSerializerOptions.with().build();
        InputStream serializedContent = null;
        if (primitiveResponse.getProperty().getValue() != null) {
            serializedContent = fixedFormatSerializer.primitiveValue((EdmPrimitiveType) primitiveResponse.getEdmPropertyType(), primitiveResponse.getProperty().getValue(), options);
        } else {
            if (primitiveResponse.getEdmPropertyType() instanceof EdmDateTimeOffset) {
                // Work around facet constraint not allowing null values
                serializedContent = fixedFormatSerializer.primitiveValue(EdmString.getInstance(), "null", options);
            } else {
                serializedContent = fixedFormatSerializer.primitiveValue((EdmPrimitiveType) primitiveResponse.getEdmPropertyType(), "null", options);
            }
        }
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.TEXT_PLAIN.toContentTypeString());
    }
    
    private void createGeospatialValueResponse(ODataResponse response, PropertyResponse primitiveResponse) throws SerializerException {
        Property property = primitiveResponse.getProperty();
        EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) primitiveResponse.getEdmPropertyType();
        
        ContextURL contextUrl = ContextURL.with().entitySet(primitiveResponse.getResponseEdmEntitySet()).navOrPropertyPath(property.getName()).build();
        PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();
        // serialize
        SerializerResult serializerResult = serializer.geospatialPrimitive(serviceMetadata, edmPropertyType, property, options);
        
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
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
        InputStream serializedContent;
        if (primitiveResponse.getProperty().getValue() == null) {
            serializedContent = new ByteArrayInputStream( ("{\"" + primitiveResponse.getProperty().getName() + "\":null}").getBytes() );
        } else {
            //TODO: check for other than primitive types for the property
            serializedContent = createReponseContent(primitiveResponse.getProperty(),
                                                     (EdmPrimitiveType) primitiveResponse.getEdmPropertyType(),
                                                     primitiveResponse.getResponseEdmEntitySet());
        }

        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
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
        this.serializer = new SensorThingsSerializer(ContentType.JSON_NO_METADATA);
        fixedFormatSerializer = odata.createFixedFormatSerializer();
    }

    private InputStream createReponseContent(Property property, EdmPrimitiveType edmPropertyType, EdmEntitySet responseEdmEntitySet) throws SerializerException {
        ContextURL contextUrl = ContextURL.with().entitySet(responseEdmEntitySet).navOrPropertyPath(property.getName()).build();
        PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();

        // serialize
        SerializerResult serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, property, options);
        InputStream propertyStream = serializerResult.getContent();
        return propertyStream;

    }

}