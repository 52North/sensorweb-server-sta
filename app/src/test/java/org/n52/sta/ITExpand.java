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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Test expanding nested Entities.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ITExpand extends ConformanceTests implements TestUtil {

    ITExpand(@Value("${server.rootUrl}") String rootUrl) throws IOException {
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
    public void testSingleExpandonCollection() throws IOException {
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

    private void checkSingleExpandOnCollection(EntityType type, String expanded) throws IOException {
        JsonNode response = getCollection(type, URLEncoder.encode("$expand=" + expanded));
        Assertions.assertTrue(response.has(countKey));
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
    public void testMultipleExpandOnCollection() throws IOException {
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

    private void checkMultipleExpandOnCollection(EntityType type, String... expanded) throws IOException {
        for (int length = expanded.length; length > 1; length--) {
            StringBuilder expand = new StringBuilder();
            expand.append(expanded[0]);
            for (int i = 1; i < length; i++) {
                expand.append(",");
                expand.append(expanded[i]);
            }
            JsonNode response = getCollection(type, URLEncoder.encode("$expand=" + expand.toString()));
            Assertions.assertTrue(response.has(countKey));
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
    public void testSingleExpandOnEntity() throws IOException {
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

    private void checkSingleExpandOnEntity(EntityType type, String expanded) throws IOException {
        String id = getCollection(type).get(value).get(0).get(idKey).asText();
        JsonNode response = getEntity(type, id, URLEncoder.encode("$expand=" + expanded));
        Assertions.assertTrue(response.has(idKey));
        Assertions.assertTrue(response.has(expanded));
        System.out.println(expanded);
        System.out.println(response.toPrettyString());
        if (response.get(expanded).isArray()) {
            for (JsonNode node : response.get(expanded)) {
                Assertions.assertTrue(node.has(idKey));
            }
        } else {
            Assertions.assertTrue(response.get(expanded).has(idKey));
        }
    }

    @Test
    public void testMultipleExpandOnEntity() throws IOException {
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

    private void checkMultipleExpandOnEntity(EntityType type, String... expanded) throws IOException {
        for (int length = expanded.length; length > 1; length--) {
            StringBuilder expand = new StringBuilder();
            expand.append(expanded[0]);
            for (int i = 1; i < length; i++) {
                expand.append(",");
                expand.append(expanded[i]);
            }
            String id = getCollection(type).get(value).get(0).get(idKey).asText();
            JsonNode response = getEntity(type, id, URLEncoder.encode("$expand=" + expanded));
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

}
