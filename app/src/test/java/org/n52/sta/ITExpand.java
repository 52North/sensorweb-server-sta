/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;

/**
 * Test expanding nested Entities.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class ITExpand extends ConformanceTests implements TestUtil {

    ITExpand(@Value("${server.rootUrl}") String rootUrl) throws Exception {
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
    public void testSingleExpandonCollection() throws Exception {
        checkSingleExpandOnCollection(EntityType.THING, DATASTREAMS);
        checkSingleExpandOnCollection(EntityType.THING, LOCATIONS);
        checkSingleExpandOnCollection(EntityType.THING, HISTORICALLOCATIONS);

        checkSingleExpandOnCollection(EntityType.LOCATION, THINGS);
        checkSingleExpandOnCollection(EntityType.LOCATION, HISTORICALLOCATIONS);

        checkSingleExpandOnCollection(EntityType.HISTORICAL_LOCATION, THING);
        checkSingleExpandOnCollection(EntityType.HISTORICAL_LOCATION, LOCATIONS);

        checkSingleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS);
        checkSingleExpandOnCollection(EntityType.DATASTREAM, OBSERVEDPROPERTY);
        checkSingleExpandOnCollection(EntityType.DATASTREAM, THING);
        checkSingleExpandOnCollection(EntityType.DATASTREAM, SENSOR);

        checkSingleExpandOnCollection(EntityType.SENSOR, DATASTREAMS);

        checkSingleExpandOnCollection(EntityType.OBSERVATION, DATASTREAM);
        checkSingleExpandOnCollection(EntityType.OBSERVATION, FEATUREOFINTEREST);

        checkSingleExpandOnCollection(EntityType.OBSERVED_PROPERTY, DATASTREAMS);

        checkSingleExpandOnCollection(EntityType.FEATURE_OF_INTEREST, OBSERVATIONS);
    }

    private void checkSingleExpandOnCollection(EntityType type, String expanded) throws Exception {
        JsonNode response = getCollection(type, "$expand=" + expanded);
        Assertions.assertTrue(response.has(value));
        for (JsonNode item : response.get(value)) {
            if (item.get(expanded).isArray()) {
                for (JsonNode node : item.get(expanded)) {
                    Assertions.assertTrue(node.has(idKey));
                }
            } else {
                Assertions.assertTrue(item.get(expanded).has(idKey));
            }
        }
    }

    @Test
    @Order(2)
    public void testMultipleExpandOnCollection() throws Exception {
        checkMultipleExpandOnCollection(EntityType.THING, DATASTREAMS, LOCATIONS);
        checkMultipleExpandOnCollection(EntityType.THING, DATASTREAMS, HISTORICALLOCATIONS);
        checkMultipleExpandOnCollection(EntityType.THING, LOCATIONS, HISTORICALLOCATIONS);
        checkMultipleExpandOnCollection(EntityType.THING, DATASTREAMS, LOCATIONS, HISTORICALLOCATIONS);

        checkMultipleExpandOnCollection(EntityType.LOCATION, HISTORICALLOCATIONS, THINGS);

        checkMultipleExpandOnCollection(EntityType.HISTORICAL_LOCATION, THING, LOCATIONS);

        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, THING, SENSOR);

        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, THING);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, THING);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, THING);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, THING, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, THING, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, THING, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVEDPROPERTY, THING, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVEDPROPERTY, THING, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVEDPROPERTY, THING, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, THING);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVEDPROPERTY, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVEDPROPERTY, THING);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, OBSERVEDPROPERTY, OBSERVATIONS);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, THING, SENSOR);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, THING, OBSERVATIONS);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, THING, OBSERVEDPROPERTY);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, SENSOR, OBSERVATIONS);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, SENSOR, THING);
        checkMultipleExpandOnCollection(EntityType.DATASTREAM, SENSOR, OBSERVEDPROPERTY);

        checkMultipleExpandOnCollection(EntityType.OBSERVATION, DATASTREAM, FEATUREOFINTEREST);
    }

    private void checkMultipleExpandOnCollection(EntityType type, String... expanded) throws Exception {
        for (int length = expanded.length; length > 1; length--) {
            StringBuilder expand = new StringBuilder();
            expand.append(expanded[0]);
            for (int i = 1; i < length; i++) {
                expand.append(",");
                expand.append(expanded[i]);
            }
            JsonNode response = getCollection(type, "$expand=" + expand.toString());
            Assertions.assertTrue(response.has(value));
            for (JsonNode item : response.get(value)) {
                for (int i = 0; i < length; i++) {
                    if (item.get(expanded[i]).isArray()) {
                        for (JsonNode jsonNode : item.get(expanded[i])) {
                            Assertions.assertTrue(jsonNode.has(idKey));
                        }
                    } else {
                        Assertions.assertTrue(item.get(expanded[i]).has(idKey));
                    }
                }
            }
        }
    }

    @Test
    @Order(3)
    public void testSingleExpandOnEntity() throws Exception {
        checkSingleExpandOnEntity(EntityType.THING, DATASTREAMS);
        checkSingleExpandOnEntity(EntityType.THING, LOCATIONS);
        checkSingleExpandOnEntity(EntityType.THING, HISTORICALLOCATIONS);

        checkSingleExpandOnEntity(EntityType.LOCATION, THINGS);
        checkSingleExpandOnEntity(EntityType.LOCATION, HISTORICALLOCATIONS);

        checkSingleExpandOnEntity(EntityType.HISTORICAL_LOCATION, THING);
        checkSingleExpandOnEntity(EntityType.HISTORICAL_LOCATION, LOCATIONS);

        checkSingleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS);
        checkSingleExpandOnEntity(EntityType.DATASTREAM, OBSERVEDPROPERTY);
        checkSingleExpandOnEntity(EntityType.DATASTREAM, THING);
        checkSingleExpandOnEntity(EntityType.DATASTREAM, SENSOR);

        checkSingleExpandOnEntity(EntityType.SENSOR, DATASTREAMS);

        checkSingleExpandOnEntity(EntityType.OBSERVATION, DATASTREAM);
        checkSingleExpandOnEntity(EntityType.OBSERVATION, FEATUREOFINTEREST);

        checkSingleExpandOnEntity(EntityType.OBSERVED_PROPERTY, DATASTREAMS);

        checkSingleExpandOnEntity(EntityType.FEATURE_OF_INTEREST, OBSERVATIONS);
    }

    private void checkSingleExpandOnEntity(EntityType type, String expanded) throws Exception {
        String id = getCollection(type).get(value).get(0).get(idKey).asText();
        JsonNode response = getEntity(type, id, "$expand=" + expanded);
        Assertions.assertTrue(response.has(idKey));
        Assertions.assertTrue(response.has(expanded));
        if (response.get(expanded).isArray()) {
            for (JsonNode node : response.get(expanded)) {
                Assertions.assertTrue(node.has(idKey));
            }
        } else {
            Assertions.assertTrue(response.get(expanded).has(idKey));
        }
    }

    @Test
    @Order(4)
    public void testMultipleExpandOnEntity() throws Exception {
        checkMultipleExpandOnEntity(EntityType.THING, DATASTREAMS, LOCATIONS);
        checkMultipleExpandOnEntity(EntityType.THING, DATASTREAMS, HISTORICALLOCATIONS);
        checkMultipleExpandOnEntity(EntityType.THING, LOCATIONS, HISTORICALLOCATIONS);
        checkMultipleExpandOnEntity(EntityType.THING, DATASTREAMS, LOCATIONS, HISTORICALLOCATIONS);

        checkMultipleExpandOnEntity(EntityType.LOCATION, HISTORICALLOCATIONS, THINGS);

        checkMultipleExpandOnEntity(EntityType.HISTORICAL_LOCATION, THING, LOCATIONS);

        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, THING, SENSOR);

        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, THING);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, THING);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, THING);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, THING, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, THING, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, THING, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVEDPROPERTY, THING, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVEDPROPERTY, THING, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVEDPROPERTY, THING, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, OBSERVEDPROPERTY);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, THING);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVEDPROPERTY, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVEDPROPERTY, THING);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, OBSERVEDPROPERTY, OBSERVATIONS);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, THING, SENSOR);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, THING, OBSERVATIONS);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, THING, OBSERVEDPROPERTY);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, SENSOR, OBSERVATIONS);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, SENSOR, THING);
        checkMultipleExpandOnEntity(EntityType.DATASTREAM, SENSOR, OBSERVEDPROPERTY);

        checkMultipleExpandOnEntity(EntityType.OBSERVATION, DATASTREAM, FEATUREOFINTEREST);
    }

    private void checkMultipleExpandOnEntity(EntityType type, String... expanded) throws Exception {
        for (int length = expanded.length; length > 1; length--) {
            StringBuilder expand = new StringBuilder();
            expand.append(expanded[0]);
            for (int i = 1; i < length; i++) {
                expand.append(",");
                expand.append(expanded[i]);
            }
            String id = getCollection(type).get(value).get(0).get(idKey).asText();
            JsonNode response = getEntity(type, id, "$expand=" + expand.toString());
            for (int i = 0; i < length; i++) {
                if (response.get(expanded[i]).isArray()) {
                    for (JsonNode jsonNode : response.get(expanded[i])) {
                        Assertions.assertTrue(jsonNode.has(idKey));
                    }
                } else {
                    Assertions.assertTrue(response.get(expanded[i]).has(idKey));
                }
            }
        }
    }

    @Test
    @Order(5)
    public void testNestedExpandOnCollection() throws Exception {
        checkNestedExpandOnCollection(EntityType.THING, DATASTREAMS, OBSERVATIONS);
        checkNestedExpandOnCollection(EntityType.THING, DATASTREAMS, OBSERVEDPROPERTY);
        checkNestedExpandOnCollection(EntityType.THING, DATASTREAMS, THING);
        checkNestedExpandOnCollection(EntityType.THING, DATASTREAMS, SENSOR);
        checkNestedExpandOnCollection(EntityType.THING, LOCATIONS, THINGS);
        checkNestedExpandOnCollection(EntityType.THING, LOCATIONS, HISTORICALLOCATIONS);
        checkNestedExpandOnCollection(EntityType.THING, HISTORICALLOCATIONS, THING);
        checkNestedExpandOnCollection(EntityType.THING, HISTORICALLOCATIONS, LOCATIONS);

        checkNestedExpandOnCollection(EntityType.LOCATION, THINGS, DATASTREAMS);
        checkNestedExpandOnCollection(EntityType.LOCATION, THINGS, LOCATIONS);
        checkNestedExpandOnCollection(EntityType.LOCATION, HISTORICALLOCATIONS, THING);
        checkNestedExpandOnCollection(EntityType.LOCATION, HISTORICALLOCATIONS, LOCATIONS);

        checkNestedExpandOnCollection(EntityType.HISTORICAL_LOCATION, THING, DATASTREAMS);
        checkNestedExpandOnCollection(EntityType.HISTORICAL_LOCATION, THING, LOCATIONS);
        checkNestedExpandOnCollection(EntityType.HISTORICAL_LOCATION, THING, HISTORICALLOCATIONS);

        checkNestedExpandOnCollection(EntityType.HISTORICAL_LOCATION, THINGS);
        checkNestedExpandOnCollection(EntityType.HISTORICAL_LOCATION, HISTORICALLOCATIONS);

        checkNestedExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, DATASTREAM);
        checkNestedExpandOnCollection(EntityType.DATASTREAM, OBSERVATIONS, FEATUREOFINTEREST);
        checkNestedExpandOnCollection(EntityType.DATASTREAM, OBSERVEDPROPERTY, DATASTREAMS);
        checkNestedExpandOnCollection(EntityType.DATASTREAM, THING, DATASTREAMS);
        checkNestedExpandOnCollection(EntityType.DATASTREAM, THING, LOCATIONS);
        checkNestedExpandOnCollection(EntityType.DATASTREAM, THING, HISTORICALLOCATIONS);
        checkNestedExpandOnCollection(EntityType.DATASTREAM, SENSOR, DATASTREAMS);

        checkNestedExpandOnCollection(EntityType.SENSOR, DATASTREAMS, SENSOR);
        checkNestedExpandOnCollection(EntityType.SENSOR, DATASTREAMS, THING);
        checkNestedExpandOnCollection(EntityType.SENSOR, DATASTREAMS, OBSERVEDPROPERTY);
        checkNestedExpandOnCollection(EntityType.SENSOR, DATASTREAMS, OBSERVATIONS);

        checkNestedExpandOnCollection(EntityType.OBSERVATION, DATASTREAM, SENSOR);
        checkNestedExpandOnCollection(EntityType.OBSERVATION, DATASTREAM, THING);
        checkNestedExpandOnCollection(EntityType.OBSERVATION, DATASTREAM, OBSERVEDPROPERTY);
        checkNestedExpandOnCollection(EntityType.OBSERVATION, DATASTREAM, OBSERVATIONS);
        checkNestedExpandOnCollection(EntityType.OBSERVATION, FEATUREOFINTEREST, OBSERVATIONS);

        checkNestedExpandOnCollection(EntityType.OBSERVED_PROPERTY, DATASTREAMS, SENSOR);
        checkNestedExpandOnCollection(EntityType.OBSERVED_PROPERTY, DATASTREAMS, THING);
        checkNestedExpandOnCollection(EntityType.OBSERVED_PROPERTY, DATASTREAMS, OBSERVEDPROPERTY);
        checkNestedExpandOnCollection(EntityType.OBSERVED_PROPERTY, DATASTREAMS, OBSERVATIONS);

        checkNestedExpandOnCollection(EntityType.FEATURE_OF_INTEREST, OBSERVATIONS, DATASTREAM);
        checkNestedExpandOnCollection(EntityType.FEATURE_OF_INTEREST, OBSERVATIONS, FEATUREOFINTEREST);
    }

    private void checkNestedExpandOnCollection(EntityType type, String... expanded) throws Exception {
        checkNestedExpandOnCollectionNestedExpand(type, expanded);
        checkNestedExpandOnCollectionWithSlash(type, expanded);
    }

    private void checkNestedExpandOnCollectionNestedExpand(EntityType type, String... expanded) throws Exception {
        for (int length = expanded.length; length > 1; length--) {
            StringBuilder expand = new StringBuilder();
            expand.append(expanded[0]);
            for (int i = 1; i < length; i++) {
                expand.append("($expand=");
                expand.append(expanded[i]);
                expand.append(")");
            }
            String id = getCollection(type).get(value).get(0).get(idKey).asText();
            JsonNode response = getEntity(type, id, "$expand=" + expand.toString());
            checkNested(response, expanded);
        }
    }

    private void checkNestedExpandOnCollectionWithSlash(EntityType type, String... expanded) throws Exception {
        for (int length = expanded.length; length > 1; length--) {
            StringBuilder expand = new StringBuilder();
            expand.append(expanded[0]);
            for (int i = 1; i < length; i++) {
                expand.append("/");
                expand.append(expanded[i]);
            }
            String id = getCollection(type).get(value).get(0).get(idKey).asText();
            JsonNode response = getEntity(type, id, "$expand=" + expand.toString());
            checkNested(response, expanded);
        }
    }

    private void checkNested(JsonNode obj, String... expand) {
        if (expand.length > 0) {
            if (obj.get(expand[0]).isArray()) {
                for (JsonNode jsonNode : obj.get(expand[0])) {
                    Assertions.assertTrue(jsonNode.has(idKey));
                    checkNested(jsonNode, Arrays.copyOfRange(expand, 1, expand.length));
                }
            } else {
                Assertions.assertTrue(obj.get(expand[0]).has(idKey));
                checkNested(obj.get(expand[0]), Arrays.copyOfRange(expand, 1, expand.length));
            }
        }
    }

    @Test
    @Order(6)
    public void testNestedExpandOnEntity() throws Exception {
        checkNestedExpandOnEntity(EntityType.THING, DATASTREAMS, OBSERVATIONS);
        checkNestedExpandOnEntity(EntityType.THING, DATASTREAMS, OBSERVEDPROPERTY);
        checkNestedExpandOnEntity(EntityType.THING, DATASTREAMS, THING);
        checkNestedExpandOnEntity(EntityType.THING, DATASTREAMS, SENSOR);
        checkNestedExpandOnEntity(EntityType.THING, LOCATIONS, THINGS);
        checkNestedExpandOnEntity(EntityType.THING, LOCATIONS, HISTORICALLOCATIONS);
        checkNestedExpandOnEntity(EntityType.THING, HISTORICALLOCATIONS, THING);
        checkNestedExpandOnEntity(EntityType.THING, HISTORICALLOCATIONS, LOCATIONS);

        checkNestedExpandOnEntity(EntityType.LOCATION, THINGS, DATASTREAMS);
        checkNestedExpandOnEntity(EntityType.LOCATION, THINGS, LOCATIONS);
        checkNestedExpandOnEntity(EntityType.LOCATION, HISTORICALLOCATIONS, THING);
        checkNestedExpandOnEntity(EntityType.LOCATION, HISTORICALLOCATIONS, LOCATIONS);

        checkNestedExpandOnEntity(EntityType.HISTORICAL_LOCATION, THING, DATASTREAMS);
        checkNestedExpandOnEntity(EntityType.HISTORICAL_LOCATION, THING, LOCATIONS);
        checkNestedExpandOnEntity(EntityType.HISTORICAL_LOCATION, THING, HISTORICALLOCATIONS);

        checkNestedExpandOnEntity(EntityType.HISTORICAL_LOCATION, THINGS);
        checkNestedExpandOnEntity(EntityType.HISTORICAL_LOCATION, HISTORICALLOCATIONS);

        checkNestedExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, DATASTREAM);
        checkNestedExpandOnEntity(EntityType.DATASTREAM, OBSERVATIONS, FEATUREOFINTEREST);
        checkNestedExpandOnEntity(EntityType.DATASTREAM, OBSERVEDPROPERTY, DATASTREAMS);
        checkNestedExpandOnEntity(EntityType.DATASTREAM, THING, DATASTREAMS);
        checkNestedExpandOnEntity(EntityType.DATASTREAM, THING, LOCATIONS);
        checkNestedExpandOnEntity(EntityType.DATASTREAM, THING, HISTORICALLOCATIONS);
        checkNestedExpandOnEntity(EntityType.DATASTREAM, SENSOR, DATASTREAMS);

        checkNestedExpandOnEntity(EntityType.SENSOR, DATASTREAMS, SENSOR);
        checkNestedExpandOnEntity(EntityType.SENSOR, DATASTREAMS, THING);
        checkNestedExpandOnEntity(EntityType.SENSOR, DATASTREAMS, OBSERVEDPROPERTY);
        checkNestedExpandOnEntity(EntityType.SENSOR, DATASTREAMS, OBSERVATIONS);

        checkNestedExpandOnEntity(EntityType.OBSERVATION, DATASTREAM, SENSOR);
        checkNestedExpandOnEntity(EntityType.OBSERVATION, DATASTREAM, THING);
        checkNestedExpandOnEntity(EntityType.OBSERVATION, DATASTREAM, OBSERVEDPROPERTY);
        checkNestedExpandOnEntity(EntityType.OBSERVATION, DATASTREAM, OBSERVATIONS);
        checkNestedExpandOnEntity(EntityType.OBSERVATION, FEATUREOFINTEREST, OBSERVATIONS);

        checkNestedExpandOnEntity(EntityType.OBSERVED_PROPERTY, DATASTREAMS, SENSOR);
        checkNestedExpandOnEntity(EntityType.OBSERVED_PROPERTY, DATASTREAMS, THING);
        checkNestedExpandOnEntity(EntityType.OBSERVED_PROPERTY, DATASTREAMS, OBSERVEDPROPERTY);
        checkNestedExpandOnEntity(EntityType.OBSERVED_PROPERTY, DATASTREAMS, OBSERVATIONS);

        checkNestedExpandOnEntity(EntityType.FEATURE_OF_INTEREST, OBSERVATIONS, DATASTREAM);
        checkNestedExpandOnEntity(EntityType.FEATURE_OF_INTEREST, OBSERVATIONS, FEATUREOFINTEREST);
    }

    private void checkNestedExpandOnEntity(EntityType type, String... expanded) throws Exception {
        checkNestedExpandOnEntityNestedExpand(type, expanded);
        checkNestedExpandOnEntityWithSlash(type, expanded);
    }

    private void checkNestedExpandOnEntityNestedExpand(EntityType type, String[] expanded) throws Exception {
        for (int length = expanded.length; length > 1; length--) {
            StringBuilder expand = new StringBuilder();
            expand.append(expanded[0]);
            for (int i = 1; i < length; i++) {
                expand.append("($expand=");
                expand.append(expanded[i]);
                expand.append(")");
            }
            String id = getCollection(type).get(value).get(0).get(idKey).asText();
            JsonNode response = getEntity(type, id, "$expand=" + expand.toString());
            checkNested(response, expanded);
        }
    }

    private void checkNestedExpandOnEntityWithSlash(EntityType type, String... expanded) throws Exception {
        for (int length = expanded.length; length > 1; length--) {
            StringBuilder expand = new StringBuilder();
            expand.append(expanded[0]);
            for (int i = 1; i < length; i++) {
                expand.append("/");
                expand.append(expanded[i]);
            }
            String id = getCollection(type).get(value).get(0).get(idKey).asText();
            JsonNode response = getEntity(type, id, "$expand=" + expand.toString());
            checkNested(response, expanded);
        }
    }

}
