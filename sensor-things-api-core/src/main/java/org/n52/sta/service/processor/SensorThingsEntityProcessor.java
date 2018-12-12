/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;

import java.io.InputStream;
import java.util.Locale;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.core.deserializer.DeserializerResultImpl;
import org.n52.sta.edm.provider.SensorThingsEdmConstants;
import org.n52.sta.service.deserializer.SensorThingsDeserializer;
import org.n52.sta.service.handler.AbstractEntityRequestHandler;
import org.n52.sta.service.handler.crud.AbstractEntityCrudRequestHandler;
import org.n52.sta.service.handler.crud.EntityCrudRequestHandlerRepository;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.query.QueryOptionsHandler;
import org.n52.sta.service.query.URIQueryOptions;
import org.n52.sta.service.response.EntityResponse;
import org.n52.sta.service.serializer.SensorThingsSerializer;
import org.n52.sta.utils.EntityAnnotator;
import org.n52.sta.utils.EntityQueryParams;
import org.n52.sta.utils.UriResourceNavigationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingsEntityProcessor implements EntityProcessor {

    @Autowired
    private AbstractEntityRequestHandler requestHandler;
    
    @Autowired
    private EntityCrudRequestHandlerRepository crudRequestHandlerReportitory;

    @Autowired
    private QueryOptionsHandler queryOptionsHandler;

    @Autowired
    private EntityAnnotator entityAnnotator;
    
    @Autowired
    private UriResourceNavigationResolver navigationResolver;


    private OData odata;
    private ServiceMetadata serviceMetadata;
    private ODataSerializer serializer;
    private SensorThingsDeserializer deserializer;

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        QueryOptions queryOptions = new URIQueryOptions(uriInfo, request.getRawBaseUri());
        EntityResponse entityResponse = requestHandler.handleEntityRequest(uriInfo.getUriResourceParts(), queryOptions);

        InputStream serializedContent = createResponseContent(serviceMetadata, entityResponse, queryOptions);

        // configure the response object: set the body, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        QueryOptions queryOptions = new URIQueryOptions(uriInfo, request.getRawBaseUri());
        EntityResponse entityResponse = new EntityResponse();
        DeserializerResult deserializeRequestBody = deserializeRequestBody(request, uriInfo);
        if (deserializeRequestBody.getEntity() != null) {
            entityResponse = getCrudEntityHanlder(uriInfo)
                    .handleCreateEntityRequest(deserializeRequestBody.getEntity(), uriInfo.getUriResourceParts());

            entityAnnotator.annotateEntity(entityResponse.getEntity(), entityResponse.getEntitySet().getEntityType(),
                    queryOptions.getBaseURI(), queryOptions.getSelectOption());
            InputStream serializedContent = createResponseContent(serviceMetadata, entityResponse, queryOptions);
            // configure the response object: set the body, headers and status code
            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON.toContentTypeString());
            response.setHeader(HttpHeader.LOCATION, entityResponse.getEntity().getSelfLink().getHref());
        } else {
            response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
        }
//        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        if (HttpMethod.PUT.equals(request.getMethod())) {
            throw new ODataApplicationException("Http PUT is not yet supported!",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.getDefault());
        }
        QueryOptions queryOptions = new URIQueryOptions(uriInfo, request.getRawBaseUri());
        EntityResponse entityResponse = new EntityResponse();
        DeserializerResult deserializeRequestBody = deserializeRequestBody(request, uriInfo);
        if (deserializeRequestBody.getEntity() != null) {
            entityResponse = getCrudEntityHanlder(uriInfo).handleUpdateEntityRequest(
                    deserializeRequestBody.getEntity(), request.getMethod(), uriInfo.getUriResourceParts());

            entityAnnotator.annotateEntity(
                    entityResponse.getEntity(),
                    entityResponse.getEntitySet().getEntityType(),
                    queryOptions.getBaseURI(),
                    queryOptions.getSelectOption());
            InputStream serializedContent = createResponseContent(serviceMetadata, entityResponse, queryOptions);
            // configure the response object: set the body, headers and status code
            response.setContent(serializedContent);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.APPLICATION_JSON.toContentTypeString());
        } else {
            response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
        }
//        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {
        QueryOptions queryOptions = new URIQueryOptions(uriInfo, request.getRawBaseUri());
        EntityResponse entityResponse = new EntityResponse();
        entityResponse = getCrudEntityHanlder(uriInfo)
                .handleDeleteEntityRequest(uriInfo.getUriResourceParts());
        entityAnnotator.annotateEntity(entityResponse.getEntity(),
                entityResponse.getEntitySet().getEntityType(),
                queryOptions.getBaseURI(),
                queryOptions.getSelectOption());
//        InputStream serializedContent = createResponseContent(serviceMetadata, entityResponse, queryOptions);
        // configure the response object: set the body, headers and status code
//        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.JSON_NO_METADATA.toContentTypeString());
        
//        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    @Override
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
        this.serializer = new SensorThingsSerializer(ContentType.JSON_NO_METADATA);
        this.deserializer = new SensorThingsDeserializer(ContentType.JSON_NO_METADATA);
        this.queryOptionsHandler.setUriHelper(odata.createUriHelper());
    }

    private InputStream createResponseContent(ServiceMetadata serviceMetadata, EntityResponse response, QueryOptions queryOptions) throws SerializerException {
        EdmEntityType edmEntityType = response.getEntitySet().getEntityType();

        ContextURL.Builder contextUrlBuilder = ContextURL.with()
                .entitySet(response.getEntitySet())
                .suffix(ContextURL.Suffix.ENTITY);
        contextUrlBuilder.selectList(queryOptionsHandler.getSelectListFromSelectOption(
                edmEntityType, queryOptions.getExpandOption(), queryOptions.getSelectOption()));
        ContextURL contextUrl = contextUrlBuilder.build();

        EntitySerializerOptions opts = EntitySerializerOptions.with()
                .contextURL(contextUrl)
                .select(queryOptions.getSelectOption())
                .expand(QueryOptionsHandler.minimizeExpandOption(queryOptions.getExpandOption()))
                .build();

        SerializerResult serializerResult = serializer.entity(serviceMetadata, response.getEntitySet().getEntityType(), response.getEntity(), opts);
        InputStream serializedContent = serializerResult.getContent();

        return serializedContent;
    }

    private DeserializerResult deserializeRequestBody(ODataRequest request, UriInfo uriInfo)
            throws DeserializerException, ODataApplicationException {
        if (uriInfo.getUriResourceParts().size() > 1) {
            EntityQueryParams navigationPaths = navigationResolver.resolveUriResourceNavigationPaths(uriInfo.getUriResourceParts());
            DeserializerResult target = deserializer.entity(request.getBody(), navigationPaths.getTargetEntitySet().getEntityType());
            return addNavigationLink(target, getSourceEntity(navigationPaths));
        }
        return deserializer.entity(request.getBody(), navigationResolver
                .resolveRootUriResource(uriInfo.getUriResourceParts().get(0)).getEntityType());
    }
    
    private DeserializerResult addNavigationLink(DeserializerResult target, Entity sourceEntity) {
        Link link = new Link();
        link.setTitle(sourceEntity.getType().replaceAll(SensorThingsEdmConstants.NAMESPACE + ".", ""));
        link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
        link.setInlineEntity(sourceEntity);
        target.getEntity().getNavigationLinks().add(link);
        return target;
    }

    private Entity getSourceEntity(EntityQueryParams navigationPaths) {
        Entity entity = new Entity();
        entity.setType(navigationPaths.getSourceEntityType().getFullQualifiedName().getFullQualifiedNameAsString());
        addId(entity, navigationPaths.getSourceId());
        return entity;
    }
    
    private Entity addId(Entity entity, Long id) {
        return entity.addProperty(new Property(null, PROP_ID, ValueType.PRIMITIVE, id));
    }

    private AbstractEntityCrudRequestHandler getCrudEntityHanlder(UriInfo uriInfo) throws ODataApplicationException {
        if (uriInfo.getUriResourceParts().size() > 1) {
            return getUriResourceEntitySet(navigationResolver
                    .resolveUriResourceNavigationPaths(uriInfo.getUriResourceParts()).getTargetEntitySet().getEntityType().getName());
        }
        return getCrudEntityHanlder(navigationResolver.resolveRootUriResource(uriInfo.getUriResourceParts().get(0)));
    }
    
    private AbstractEntityCrudRequestHandler getCrudEntityHanlder(UriResourceEntitySet uriResourceEntitySet) {
        return getUriResourceEntitySet(uriResourceEntitySet.getEntityType().getName());
    }

    private AbstractEntityCrudRequestHandler getUriResourceEntitySet(String type) {
        return crudRequestHandlerReportitory.getEntityCrudRequestHandler(type);
    }
}
