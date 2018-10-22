/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.InputStream;

import org.apache.olingo.commons.api.data.ContextURL;
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
import org.n52.sta.service.query.QueryOptionsHandler;
import org.n52.sta.service.query.URIQueryOptions;
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

    @Autowired
    AbstractEntityCollectionRequestHandler requestHandler;

    @Autowired
    QueryOptionsHandler queryOptionsHandler;

    @Autowired
    EntityAnnotator entityAnnotator;

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private ODataSerializer serializer;

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
            ContentType contentType) throws ODataApplicationException, ODataLibraryException {
        QueryOptions queryOptions = new URIQueryOptions(uriInfo, request.getRawBaseUri());

        EntityCollectionResponse entityCollectionResponse = requestHandler.handleEntityCollectionRequest(
                uriInfo.getUriResourceParts(), queryOptions);

        InputStream serializedContent = createResponseContent(serviceMetadata, entityCollectionResponse, queryOptions);

        // configure the response object: set the body, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
        this.serializer = new SensorThingsSerializer(ContentType.JSON_NO_METADATA);
        this.queryOptionsHandler.setUriHelper(odata.createUriHelper());

    }

    public InputStream createResponseContent(ServiceMetadata serviceMetadata, EntityCollectionResponse response, QueryOptions queryOptions) throws SerializerException {
        EdmEntityType edmEntityType = response.getEntitySet().getEntityType();

        ContextURL.Builder contextUrlBuilder = ContextURL.with().entitySet(response.getEntitySet());
        contextUrlBuilder.selectList(queryOptionsHandler.getSelectListFromSelectOption(
                edmEntityType, queryOptions.getExpandOption(), queryOptions.getSelectOption()));
        ContextURL contextUrl = contextUrlBuilder.build();

        final String id = queryOptions.getBaseURI() + "/" + response.getEntitySet().getName();
        EntityCollectionSerializerOptions opts
                = EntityCollectionSerializerOptions
                        .with()
                        .id(id)
                        .contextURL(contextUrl)
                        .select(queryOptions.getSelectOption())
                        .expand(queryOptions.getExpandOption())
                        .count(queryOptions.getCountOption())
                        .build();
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, response.getEntityCollection(), opts);
        InputStream serializedContent = serializerResult.getContent();

        return serializedContent;
    }
}
