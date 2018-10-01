/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.InputStream;
import java.util.List;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.ReferenceCollectionProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.ReferenceCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.n52.sta.service.handler.AbstractEntityCollectionRequestHandler;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.response.EntityCollectionResponse;
import org.n52.sta.service.serializer.SensorThingsSerializer;
import org.n52.sta.utils.EntityAnnotator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsReferenceCollectionProcessor implements ReferenceCollectionProcessor {

    private static final ContentType ET_REFERENCE_COLLECTION_PROCESSOR_CONTENT_TYPE = ContentType.JSON_NO_METADATA;

    @Autowired
    AbstractEntityCollectionRequestHandler requestHandler;

    @Autowired
    EntityAnnotator entityAnnotator;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readReferenceCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        EntityCollectionResponse entityCollectionResponse =
                requestHandler.handleEntityCollectionRequest(resourcePaths.subList(0, resourcePaths.size() - 1),
                        new QueryOptions(uriInfo, request.getRawBaseUri()));

        InputStream serializedContent = createResponseContent(entityCollectionResponse, uriInfo, request.getRawBaseUri());

        // configure the response object: set the body, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ET_REFERENCE_COLLECTION_PROCESSOR_CONTENT_TYPE.toContentTypeString());
    }

    private InputStream createResponseContent(EntityCollectionResponse response, UriInfo uriInfo, String rawBaseUri) throws SerializerException {
        // annotate the entities
        for (Entity e : response.getEntityCollection().getEntities()) {
            entityAnnotator.annotateEntity(e, response.getEntitySet().getEntityType(), rawBaseUri);
        }

        // create a serializer based on JSON format
        ODataSerializer serializer = new SensorThingsSerializer(ET_REFERENCE_COLLECTION_PROCESSOR_CONTENT_TYPE);

        // serialize the content: transform from the EntitySet object to InputStream
        ContextURL contextUrl = ContextURL.with()
                .entitySet(response.getEntitySet())
                .build();

        ReferenceCollectionSerializerOptions opts
                = ReferenceCollectionSerializerOptions
                        .with()
                        .contextURL(contextUrl)
                        .build();
        SerializerResult serializerResult = serializer.referenceCollection(serviceMetadata, response.getEntitySet(), response.getEntityCollection(), opts);
        InputStream serializedContent = serializerResult.getContent();

        return serializedContent;
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

}
