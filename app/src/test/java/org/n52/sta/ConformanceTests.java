/*
 * Copyright (C) 2018-2022 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.util.UriUtils;
import org.testcontainers.junit.jupiter.Container;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
abstract class ConformanceTests implements TestUtil {

    @Container
    public static STAPostgresContainer POSTGIS_DB = STAPostgresContainer.instance();
    private static final Logger logger = LoggerFactory.getLogger(ConformanceTests.class);
    protected final String rootUrl;
    protected final ObjectMapper mapper = new ObjectMapper();

    ConformanceTests(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    JsonNode postEntity(EntityType type, String body) throws IOException {
        HttpPost request = new HttpPost(rootUrl + type.getVal());
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
        Assertions.assertEquals(200, response.getStatusLine().getStatusCode());

        return result;
    }

    protected JsonNode postInvalidEntity(EntityType type, String body) throws IOException {
        HttpPost request = new HttpPost(rootUrl + type.getVal());
        request.setEntity(new StringEntity(body));
        request.setHeader("Content-Type", "application/json");

        if (logger.isTraceEnabled()) {
            System.out.printf("POSTed to URL: %s\n", request.getURI());
            System.out.println(body);
        }

        HttpResponse response = HttpClientBuilder.create().build().execute(request);
        Assertions.assertTrue(
            response.getStatusLine().getStatusCode() == 400 || response.getStatusLine().getStatusCode() == 409,
            "Entity " + rootUrl + type.getVal() + " should not have been created but response Code  is: " +
                response.getStatusLine().getStatusCode()
        );

        JsonNode result = mapper.readTree(response.getEntity().getContent());
        if (logger.isTraceEnabled()) {
            System.out.printf("RETURNED by Server: %s\n", request.getURI());
            System.out.println(result.toPrettyString());
        }
        return result;
    }

    protected JsonNode patchEntity(EntityType type, String body, String id) throws IOException {
        HttpPatch request = new HttpPatch(rootUrl
                                              + type.getVal()
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
            String.format("Error: PATCH does not work properly for URI: %s. Error %s", request.getURI(), result.toPrettyString())
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
    protected void patchInvalidEntity(EntityType type, String body, String id) throws IOException {
        HttpPatch request = new HttpPatch(rootUrl
                                              + type.getVal()
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
    protected void deleteNonexistentEntity(EntityType type) throws IOException {
        HttpDelete request = new HttpDelete(rootUrl
                                                + type.getVal()
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

    protected void deleteEverythings() throws IOException, URISyntaxException {
        for (EntityType type : EntityType.values()) {
            for (JsonNode elem : getCollection(type).get("value")) {
                deleteEntity(type, elem.get(idKey).asText(), false);
            }
        }
    }

    protected void deleteEntity(EntityType type, String id, boolean canError) throws IOException {
        HttpDelete request = new HttpDelete(rootUrl
                                                + type.getVal()
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
                "DELETE does not work properly for " + request.getURI()
            );
        }
    }

    protected JsonNode getEntity(EntityType type, String id, String queryOption) throws IOException {
        String query = UriUtils.encode(queryOption, Charset.defaultCharset());
        return getEntity(type.getVal() + "(" + id + ")" + "?" + query);
    }

    protected JsonNode getEntityProperty(EntityType type, String id, String property) throws IOException {
        return getEntity(type.getVal() + "(" + id + ")" + "/" + property);
    }

    protected JsonNode getEntity(EntityType type, String id) throws IOException {
        return getEntity(type.getVal() + "(" + id + ")");
    }

    protected JsonNode getEntity(String path) throws IOException {
        logger.trace("Requesting:" + rootUrl + path);
        HttpGet request = new HttpGet(rootUrl + path);
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assertions.assertEquals(jsonMimeType, mimeType, "Wrong MIME Type returned on path: " + path);

        Assertions.assertEquals(200,
                                response.getStatusLine().getStatusCode(),
                                "ERROR: Did not receive 200 OK for path: " + path
                                    + " Instead received Status Code: " + response.getStatusLine().getStatusCode());
        return mapper.readTree(response.getEntity().getContent());
    }

    protected String getEntityValue(EntityType type, String id, String property) throws IOException {
        String url = type.getVal() + "(" + id + ")" + "/" + property + "/$value";
        HttpGet request = new HttpGet(rootUrl + url);
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assertions.assertEquals("text/plain", mimeType);

        Assertions.assertEquals(200,
                                response.getStatusLine().getStatusCode(),
                                "ERROR: Did not receive 200 OK for path: " + url
                                    + " Instead received Status Code: " + response.getStatusLine().getStatusCode());
        return new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
            .lines().collect(Collectors.joining(""));
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
        HttpGet request = new HttpGet(rootUrl + type.getVal() + "(aaaaaa)");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assertions.assertEquals(jsonMimeType, mimeType);

        Assertions.assertEquals(404,
                                response.getStatusLine().getStatusCode(),
                                "ERROR: Did not receive 404 NOT FOUND for path: "
                                    + type.getVal()
                                    + "(aaaaaa)"
                                    + " Instead received Status Code: " + response.getStatusLine().getStatusCode());
    }

    protected JsonNode getCollection(EntityType type, String filters) throws IOException {
        return getCollection(rootUrl + type.getVal(), filters);
    }

    protected JsonNode getCollection(String url, String filters) throws IOException {
        String query = UriUtils.encode(filters, Charset.defaultCharset());
        logger.debug("GET Collection: " + url + "?" + query);
        HttpGet request = new HttpGet(url + "?" + query);
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Check Response MIME Type
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
        Assertions.assertEquals(jsonMimeType, mimeType);

        Assertions.assertEquals(200,
                                response.getStatusLine().getStatusCode(),
                                "ERROR: Did not receive 200 OK for path: " + url
                                    + " Instead received Status Code: " + response.getStatusLine().getStatusCode());
        return mapper.readTree(response.getEntity().getContent());
    }

    protected JsonNode getCollection(EntityType type) throws IOException {
        return getCollection(type, "");
    }

    /**
     * This helper method is checking the mandatory properties of the response
     * for a specific entity
     *
     * @param mandatoryProperties List of mandatory properties
     * @param response            The response of the GET request to be checked
     */
    protected void checkEntityProperties(Set<String> mandatoryProperties, JsonNode response) {
        if (response.has(value)) {
            response.get(value).forEach(v -> checkObjectProperties(mandatoryProperties, v));
        } else {
            checkObjectProperties(mandatoryProperties, response);
        }
    }

    private void checkObjectProperties(Set<String> mandatoryProperties, JsonNode response) {
        for (String property : mandatoryProperties) {
            if (property.equals("id")) {
                property = idKey;
            }
            Assertions.assertTrue(response.has(property),
                                  "Entity: "
                                      + response.toPrettyString()
                                      + "does not have mandatory property:"
                                      + property);
            Assertions.assertNotNull(response.get(property),
                                     "Entity: "
                                         + response.toPrettyString()
                                         + "does not have mandatory property:"
                                         + property);
        }
    }

    /**
     * Check the database is empty of certain entity types
     *
     * @param entityTypes List of entity types
     */
    protected void checkNotExisting(Iterable<EntityType> entityTypes) throws Exception {
        for (EntityType entityType : entityTypes) {
            JsonNode response = getEntity(entityType.getVal());
            Assertions.assertTrue(
                response.get("value").isEmpty(),
                "Entity with type: " + entityType.name() + " is created although it shouldn't"
            );
        }
    }

    protected void checkExisting(Set<EntityType> entityTypes) throws Exception {
        for (EntityType entityType : entityTypes) {
            JsonNode response = getEntity(entityType.getVal());
            Assertions.assertTrue(
                !response.get("value").isEmpty(),
                "No Entity with type: " + entityType.name() + " is present"
            );
        }
    }

}
