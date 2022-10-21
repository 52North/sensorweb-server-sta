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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

/**
 * Test $orderby Query Option
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class ITOrderBy extends ConformanceTests implements TestUtil {

    final String result = "result";

    public ITOrderBy(@Value("${server.rootUrl}") String rootUrl) throws Exception {
        super(rootUrl);

        // Create required test harness
        // Requires POST with deep insert to work.
        postEntity(EntityType.THING, "{ \"description\": \"thing 1\", \"name\": \"thing name 1\", \"properties\": { " +
            "\"reference\": \"first\" }, \"Locations\": [ { \"description\": \"location 1\", \"name\": \"location" +
            " name 1\", \"location\": { \"type\": \"Point\", \"coordinates\": [ -117.05, 51.05 ] }, " +
            "\"encodingType\": \"application/vnd.geo+json\" } ], \"Datastreams\": [ { \"unitOfMeasurement\": { " +
            "\"name\": \"Lumen\", \"symbol\": \"lm\", \"definition\": \"http://www.qudt.org/qudt/owl/1.0" +
            ".0/unit/Instances.html/Lumen\" }, \"description\": \"datastream 1\", \"name\": \"datastream name " +
            "1\", \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\", " +
            "\"ObservedProperty\": { \"name\": \"Luminous Flux\", \"definition\": \"http://www.qudt" +
            ".org/qudt/owl/1.0.0/quantity/Instances.html/LuminousFlux\", \"description\": \"observedProperty 1\" " +
            "}, \"Sensor\": { \"description\": \"sensor 1\", \"name\": \"sensor name 1\", \"encodingType\": " +
            "\"application/pdf\", \"metadata\": \"Light flux sensor\" }, \"Observations\":[ { \"phenomenonTime\":" +
            " \"2015-03-03T00:00:00Z\", \"result\": 3 }, { \"phenomenonTime\": \"2015-03-04T00:00:00Z\", " +
            "\"result\": 4 } ] }, { \"unitOfMeasurement\": { \"name\": \"Centigrade\", \"symbol\": \"C\", " +
            "\"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen\" }, \"description\":" +
            " \"datastream 2\", \"name\": \"datastream name 2\", \"observationType\": \"http://www.opengis" +
            ".net/def/observationType/OGC-OM/2.0/OM_Measurement\", \"ObservedProperty\": { \"name\": " +
            "\"Tempretaure\", \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances" +
            ".html/Tempreture\", \"description\": \"observedProperty 2\" }, \"Sensor\": { \"description\": " +
            "\"sensor 2\", \"name\": \"sensor name 2\", \"encodingType\": \"application/pdf\", \"metadata\": " +
            "\"Tempreture sensor\" }, \"Observations\":[ { \"phenomenonTime\": \"2015-03-05T00:00:00Z\", " +
            "\"result\": 5 }, { \"phenomenonTime\": \"2015-03-06T00:00:00Z\", \"result\": 6 } ] } ] }");
    }

    @Test
    @Order(1)
    public void testOrderByResultOnObservationCollection() throws IOException {
        // Check directly on Collection
        // default ascending
        JsonNode allObservations = getCollection(EntityType.OBSERVATION, "$orderby=result");
        assertResponseCount(allObservations, 4, 4);
        assertValueIsOrdered(allObservations.get("value"), FilterConstants.SortOrder.ASC);

        // Explicit ascending
        allObservations = getCollection(EntityType.OBSERVATION, "$orderby=result asc");
        assertResponseCount(allObservations, 4, 4);
        assertValueIsOrdered(allObservations.get("value"), FilterConstants.SortOrder.ASC);

        // Descending
        allObservations = getCollection(EntityType.OBSERVATION, "$orderby=result desc");
        assertResponseCount(allObservations, 4, 4);
        assertValueIsOrdered(allObservations.get("value"), FilterConstants.SortOrder.DESC);

        // Check on related Collection
        JsonNode datastreams = getCollection(EntityType.DATASTREAM);
        assertResponseCount(datastreams, 2, 2);
        JsonNode collection;
        collection =
            getCollection(rootUrl
                              + "Datastreams("
                              + datastreams.get(value).get(0).get(idKey).asText()
                              + ")/Observations",
                          "$orderby=result");
        assertResponseCount(collection, 2, 2);
        assertValueIsOrdered(collection.get("value"), FilterConstants.SortOrder.ASC);

        collection = getCollection(rootUrl
                                       + "Datastreams("
                                       + datastreams.get(value).get(1).get(idKey).asText()
                                       + ")/Observations",
                                   "$orderby=result desc");
        assertResponseCount(collection, 2, 2);
        assertValueIsOrdered(collection.get("value"), FilterConstants.SortOrder.DESC);
    }

    private void assertValueIsOrdered(JsonNode value, FilterConstants.SortOrder sortOrder) {
        Long lastValue = (sortOrder.equals(FilterConstants.SortOrder.ASC)) ? Long.MIN_VALUE : Long.MAX_VALUE;
        for (int i = 0; i < value.size(); i++) {
            Long actual = value.get(i).get(result).asLong();
            if (sortOrder.equals(FilterConstants.SortOrder.ASC)) {
                Assertions.assertTrue(actual >= lastValue);
            } else {
                Assertions.assertTrue(actual <= lastValue);
            }
            lastValue = actual;
        }
    }
}
