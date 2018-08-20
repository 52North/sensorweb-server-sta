/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.service.processor;

import java.io.InputStream;
import java.util.List;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.MetadataProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class SensorThingMetadataProcessor implements MetadataProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void readMetadata(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType ct) throws ODataApplicationException, ODataLibraryException {
        // 1st we have retrieve the requested EntitySet from the uriInfo object (representation of the parsed service URI)
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
//        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); // in our example, the first segment is the EntitySet
//        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
//
//        // 2nd: fetch the data from backend for this requested EntitySetName
//        // it has to be delivered as EntitySet object
////        EntityCollection entitySet = getData(edmEntitySet);
//
//        // 3rd: create a serializer based on the requested format (json)
        ODataSerializer serializer = odata.createSerializer(ct);
//
//        // 4th: Now serialize the content: transform from the EntitySet object to InputStream
//        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
//        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
//
//        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
//        EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl).build();
        SerializerResult serializerResult = serializer.metadataDocument(serviceMetadata);
        InputStream serializedContent = serializerResult.getContent();

        // Finally: configure the response object: set the body, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, ct.toContentTypeString());
    }

    @Override
    public void init(OData odata, ServiceMetadata sm) {
        this.odata = odata;
        this.serviceMetadata = sm;
    }

}
