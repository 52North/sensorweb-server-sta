/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.InputStream;
import java.util.List;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.ReferenceProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.ReferenceSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.n52.sta.service.handler.AbstractEntityRequestHandler;
import org.n52.sta.service.query.handler.AbstractQueryOptionHandler;
import org.n52.sta.service.response.EntityResponse;
import org.n52.sta.service.serializer.SensorThingsSerializer;
import org.n52.sta.utils.EntityAnnotator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsReferenceProcessor implements ReferenceProcessor {

    private static final ContentType ET_REFERENCE_PROCESSOR_CONTENT_TYPE = ContentType.JSON_NO_METADATA;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Autowired
    AbstractEntityRequestHandler requestHandler;

    @Autowired
    AbstractQueryOptionHandler propertySelectionHandler;

    @Autowired
    EntityAnnotator entityAnnotator;

    @Override
    public void readReference(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        EntityResponse entityResponse = requestHandler.handleEntityCollectionRequest(resourcePaths.subList(0, resourcePaths.size() - 1));

        InputStream serializedContent = createResponseContent(entityResponse, request.getRawBaseUri());

        // configure the response object: set the body, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ET_REFERENCE_PROCESSOR_CONTENT_TYPE.toContentTypeString());
    }

    @Override
    public void createReference(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateReference(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteReference(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    private InputStream createResponseContent(EntityResponse response, String rawBaseUri) throws SerializerException {

        // annotate the entity
        entityAnnotator.annotateEntity(response.getEntity(),
                response.getEntitySet().getEntityType(), rawBaseUri);

        ODataSerializer serializer = new SensorThingsSerializer(ET_REFERENCE_PROCESSOR_CONTENT_TYPE);

        // and serialize the content: transform from the EntitySet object to InputStream
        ContextURL contextUrl = ContextURL.with()
                .entitySet(response.getEntitySet())
                .suffix(ContextURL.Suffix.ENTITY)
                .build();

        ReferenceSerializerOptions opts = ReferenceSerializerOptions.with()
                .contextURL(contextUrl)
                .build();

        SerializerResult serializerResult = serializer.reference(serviceMetadata, response.getEntitySet(), response.getEntity(), opts);
        InputStream serializedContent = serializerResult.getContent();

        return serializedContent;
    }
}
