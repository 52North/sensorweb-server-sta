/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.InputStream;

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
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.n52.sta.service.handler.AbstractEntityCollectionRequestHandler;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.query.handler.AbstractQueryOptionHandler;
import org.n52.sta.service.query.handler.CountOptions;
import org.n52.sta.service.query.handler.PropertySelectionOptions;
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
public class SensorThingsEntityCollectionProcessor implements EntityCollectionProcessor {

    private static final ContentType ET_COLLECTION_PROCESSOR_CONTENT_TYPE = ContentType.JSON_NO_METADATA;

    @Autowired
    AbstractEntityCollectionRequestHandler requestHandler;

    @Autowired
    AbstractQueryOptionHandler propertySelectionHandler;

    @Autowired
    EntityAnnotator entityAnnotator;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        EntityCollectionResponse entityCollectionResponse = requestHandler.handleEntityCollectionRequest(
                uriInfo.getUriResourceParts(), new QueryOptions(uriInfo, request.getRawBaseUri()));

        InputStream serializedContent =
                createResponseContent(entityCollectionResponse, uriInfo, request.getRawBaseUri());

        // configure the response object: set the body, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ET_COLLECTION_PROCESSOR_CONTENT_TYPE.toContentTypeString());
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
        this.propertySelectionHandler.setUriHelper(odata.createUriHelper());

    }

    private InputStream createResponseContent(EntityCollectionResponse response, UriInfo uriInfo, String rawBaseUri) throws SerializerException {
        // annotate the entities
        for (Entity e : response.getEntityCollection().getEntities()) {
            entityAnnotator.annotateEntity(e, response.getEntitySet().getEntityType(), rawBaseUri);
        }

        // create a serializer based on JSON format
//        ODataSerializer serializer = odata.createSerializer(ET_COLLECTION_PROCESSOR_CONTENT_TYPE);
        ODataSerializer serializer = new SensorThingsSerializer(ET_COLLECTION_PROCESSOR_CONTENT_TYPE);

        EdmEntityType edmEntityType = response.getEntitySet().getEntityType();

        //evaluate property selections
        PropertySelectionOptions selectOptions = propertySelectionHandler.evaluatePropertySelectionOptions(uriInfo, edmEntityType);

        //evaluate count options
        CountOptions countOptions = propertySelectionHandler.evaluateCountOptions(uriInfo, response.getEntityCollection());

        // serialize the content: transform from the EntitySet object to InputStream
        ContextURL contextUrl = ContextURL.with()
                .entitySet(response.getEntitySet())
                .selectList(selectOptions.getSelectionList())
                .build();

        final String id = rawBaseUri + "/" + response.getEntitySet().getName();
        EntityCollectionSerializerOptions opts
                = EntityCollectionSerializerOptions
                        .with()
                        .id(id)
                        .contextURL(contextUrl)
                        .select(selectOptions.getSelectOption())
                        .count(countOptions.getCountOption())
                        .build();
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, response.getEntityCollection(), opts);
        InputStream serializedContent = serializerResult.getContent();

        return serializedContent;
    }

}
