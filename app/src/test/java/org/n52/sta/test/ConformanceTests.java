/*
 * Copyright 2015-2020 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.n52.sta.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
abstract class ConformanceTests implements TestUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConformanceTests.class);

    protected final String rootUrl;
    protected final ObjectMapper mapper = new ObjectMapper();
    protected final HashMap<EntityType, String> endpoints;

    ConformanceTests(String rootUrl) {
        this.rootUrl = rootUrl;
        HashMap<EntityType, String> map = new HashMap<>();
        map.put(EntityType.THING, "Things");
        map.put(EntityType.LOCATION, "Locations");
        map.put(EntityType.HISTORICAL_LOCATION, "HistoricalLocations");
        map.put(EntityType.DATASTREAM, "Datastreams");
        map.put(EntityType.SENSOR, "Sensors");
        map.put(EntityType.FEATURE_OF_INTEREST, "FeaturesOfInterest");
        map.put(EntityType.OBSERVATION, "Observations");
        map.put(EntityType.OBSERVED_PROPERTY, "ObservedProperties");
        this.endpoints = map;
    }

    JsonNode postEntity(Conformance2.EntityType type, String body) throws IOException {
        HttpPost request = new HttpPost(rootUrl + endpoints.get(type));
        request.setEntity(new StringEntity(body));
        request.setHeader("Content-Type", "application/json");

        if (logger.isTraceEnabled()) {
            System.out.printf("POSTed to URL: %s\n", request.getURI());
            System.out.println(body);
        }

        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assertions.assertEquals(
                jsonMimeType,
                mimeType,
                "Response has invalid MIME Type"
        );

        JsonNode result = mapper.readTree(response.getEntity().getContent());
        if (logger.isTraceEnabled()) {
            System.out.printf("RETURNED by Server: %s\n", request.getURI());
            System.out.println(result.toPrettyString());
        }
        return result;
    }

    protected JsonNode postInvalidEntity(Conformance2.EntityType type, String body) throws IOException {
        HttpPost request = new HttpPost(rootUrl + endpoints.get(type));
        request.setEntity(new StringEntity(body));
        request.setHeader("Content-Type", "application/json");

        if (logger.isTraceEnabled()) {
            System.out.printf("POSTed to URL: %s\n", request.getURI());
            System.out.println(body);
        }

        HttpResponse response = HttpClientBuilder.create().build().execute(request);
        Assertions.assertTrue(
                response.getStatusLine().getStatusCode() == 400 || response.getStatusLine().getStatusCode() == 409,
                "Entity " + rootUrl + endpoints.get(type) + " should not have been created but response Code  is: " +
                        response.getStatusLine().getStatusCode()
        );

        JsonNode result = mapper.readTree(response.getEntity().getContent());
        if (logger.isTraceEnabled()) {
            System.out.printf("RETURNED by Server: %s\n", request.getURI());
            System.out.println(result.toPrettyString());
        }
        return result;
    }

    protected JsonNode patchEntity(Conformance2.EntityType type, String body, String id) throws IOException {
        HttpPatch request = new HttpPatch(rootUrl
                                                  + endpoints.get(type)
                                                  + "("
                                                  + id
                                                  + ")");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(body));

        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        if (logger.isTraceEnabled()) {
            System.out.printf("PATCHed to URL: %s\n", request.getURI());
            System.out.println(body);
        }

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assertions.assertEquals(jsonMimeType, mimeType);

        JsonNode result = mapper.readTree(response.getEntity().getContent());
        if (logger.isTraceEnabled()) {
            System.out.printf("RETURNED by Server: %s\n", request.getURI());
            System.out.println(result.toPrettyString());
        }
        int statusCode = response.getStatusLine().getStatusCode();
        Assertions.assertEquals(
                200,
                statusCode,
                "Error: PATCH does not work properly for " + request.getURI()
        );
        return result;
    }

    /**
     * This method created the URL string for the entity with specific id and
     * then PATCH invalid entity with urlParameters to that URL and confirms
     * that the response is correct based on specification.
     *
     * @param type Entity type in from EntityType enum
     * @param body The PATCH body (invalid)
     * @param id   The id of requested entity
     */
    protected void patchInvalidEntity(Conformance2.EntityType type, String body, String id) throws IOException {
        HttpPatch request = new HttpPatch(rootUrl
                                                  + endpoints.get(type)
                                                  + "("
                                                  + id
                                                  + ")");
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(body));

        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        if (logger.isTraceEnabled()) {
            System.out.printf("PATCHed to URL: %s\n", request.getURI());
            System.out.println(body);
        }

        int statusCode = response.getStatusLine().getStatusCode();

        if (logger.isTraceEnabled()) {
            System.out.printf("RETURNED by Server: %s\n", request.getURI());
            System.out.println("Statuscode:" + statusCode);
        }
        Assertions.assertEquals(
                400,
                statusCode,
                "Error: Patching related entities inline must be illegal for " + request.getURI()
        );
    }

    /**
     * This method create the URL string for a nonexistent entity and send the
     * DELETE request to that URL and confirm that the response is correct based
     * on specification.
     *
     * @param type Entity type in from EntityType enum
     */
    protected void deleteNonexistentEntity(Conformance2.EntityType type) throws IOException {
        HttpDelete request = new HttpDelete(rootUrl
                                                    + endpoints.get(type)
                                                    + "("
                                                    + "aaaaaa"
                                                    + ")");
        request.setHeader("Content-Type", "application/json");

        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        if (logger.isTraceEnabled()) {
            System.out.printf("DELETEd to URL: %s\n", request.getURI());
        }

        int statusCode = response.getStatusLine().getStatusCode();

        if (logger.isTraceEnabled()) {
            System.out.printf("RETURNED by Server: %s\n", request.getURI());
            System.out.println("Statuscode:" + statusCode);
        }
        Assertions.assertEquals(
                404,
                statusCode,
                "DELETE does not work properly for nonexistent " + request.getURI()
        );
    }

    protected void deleteEverythings() throws IOException {
        for (Conformance2.EntityType type : Conformance2.EntityType.values()) {
            for (JsonNode elem : getCollection(type).get("value")) {
                deleteEntity(type, elem.get(idKey).asText(), false);
            }
        }
    }

    protected void deleteEntity(Conformance2.EntityType type, String id, boolean canError) throws IOException {
        HttpDelete request = new HttpDelete(rootUrl
                                                    + endpoints.get(type)
                                                    + "("
                                                    + id
                                                    + ")");
        request.setHeader("Content-Type", "application/json");

        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        if (logger.isTraceEnabled()) {
            System.out.printf("DELETEd to URL: %s\n", request.getURI());
        }

        int statusCode = response.getStatusLine().getStatusCode();

        if (logger.isTraceEnabled()) {
            System.out.printf("RETURNED by Server: %s\n", request.getURI());
            System.out.println("Statuscode:" + statusCode);
        }
        if (!canError) {
            Assertions.assertEquals(
                    200,
                    statusCode,
                    "DELETE does not work properly for nonexistent " + request.getURI()
            );
        }
    }

    protected JsonNode getEntity(Conformance2.EntityType type, String id) throws IOException {
        return getEntity(endpoints.get(type) + "(" + id + ")");
    }

    protected JsonNode getEntity(String path) throws IOException {
        HttpGet request = new HttpGet(rootUrl + path);
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assertions.assertEquals(jsonMimeType, mimeType);

        Assertions.assertEquals(200,
                                response.getStatusLine().getStatusCode(),
                                "ERROR: Did not receive 200 OK for path: " + path
                                        + " Instead received Status Code: " + response.getStatusLine().getStatusCode());
        return mapper.readTree(response.getEntity().getContent());
    }

    protected JsonNode getRootResponse() throws IOException {
        HttpGet request = new HttpGet(rootUrl);
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assertions.assertEquals(jsonMimeType, mimeType);

        Assertions.assertEquals(200,
                                response.getStatusLine().getStatusCode(),
                                "ERROR: Did not receive 200 OK for path: " + rootUrl
                                        + " Instead received Status Code: " + response.getStatusLine().getStatusCode());
        return mapper.readTree(response.getEntity().getContent());
    }

    protected void getNonExistentEntity(EntityType type) throws IOException {
        HttpGet request = new HttpGet(rootUrl
                                              + endpoints.get(type)
                                              + "(aaaaaa)");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assertions.assertEquals(jsonMimeType, mimeType);

        Assertions.assertEquals(404,
                                response.getStatusLine().getStatusCode(),
                                "ERROR: Did not receive 404 NOT FOUND for path: "
                                        + endpoints.get(type)
                                        + "(aaaaaa)"
                                        + " Instead received Status Code: " + response.getStatusLine().getStatusCode());
    }

    protected JsonNode getCollection(Conformance2.EntityType type) throws IOException {
        HttpGet request = new HttpGet(rootUrl + endpoints.get(type));
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assertions.assertEquals(jsonMimeType, mimeType);

        Assertions.assertEquals(200,
                                response.getStatusLine().getStatusCode(),
                                "ERROR: Did not receive 200 OK for path: " + rootUrl + endpoints.get(type)
                                        + " Instead received Status Code: " + response.getStatusLine().getStatusCode());
        return mapper.readTree(response.getEntity().getContent());
    }

}
