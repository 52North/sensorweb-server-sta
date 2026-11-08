/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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

/**
 * Tests deleting entities whose Datastream is backed by multiple Datasets.
 * Posting Observations with different FeaturesOfInterest to the same Datastream
 * turns it into an Aggregation of multiple Datasets. Deleting the associated
 * Thing, Sensor or ObservedProperty must delete the whole Datastream
 * (including all its Datasets and Observations).
 * See https://github.com/52North/sensorweb-server-sta/issues/15
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ITDeleteAggregatedDatastream extends ConformanceTests implements TestUtil {

    ITDeleteAggregatedDatastream(@Value("${server.rootUrl}") String rootUrl) {
        super(rootUrl);
    }

    @Test
    public void deleteThingWithSingleDatastream() throws Exception {
        String thingId = createThing("thing single");
        String datastreamId = createDatastream(thingId,
                                               createSensor(),
                                               createObservedProperty("Temperature"));

        deleteEntity(EntityType.THING, thingId, false);

        assertCollectionEmpty(EntityType.THING);
        assertCollectionEmpty(EntityType.DATASTREAM);
    }

    @Test
    public void deleteThingWithMultipleDatastreams() throws Exception {
        String thingId = createThing("thing multiple");
        String sensorId = createSensor();
        createDatastream(thingId, sensorId, createObservedProperty("Temperature"));
        createDatastream(thingId, sensorId, createObservedProperty("Humidity"));

        deleteEntity(EntityType.THING, thingId, false);

        assertCollectionEmpty(EntityType.THING);
        assertCollectionEmpty(EntityType.DATASTREAM);
    }

    @Test
    public void deleteThingWithAggregatedDatastream() throws Exception {
        String[] ids = createAggregatedScenario();

        deleteEntity(EntityType.THING, ids[0], false);

        assertCollectionEmpty(EntityType.THING);
        assertCollectionEmpty(EntityType.DATASTREAM);
        assertCollectionEmpty(EntityType.OBSERVATION);
    }

    @Test
    public void deleteSensorWithAggregatedDatastream() throws Exception {
        String[] ids = createAggregatedScenario();

        deleteEntity(EntityType.SENSOR, ids[1], false);

        assertCollectionEmpty(EntityType.SENSOR);
        assertCollectionEmpty(EntityType.DATASTREAM);
        assertCollectionEmpty(EntityType.OBSERVATION);
    }

    @Test
    public void deleteObservedPropertyWithAggregatedDatastream() throws Exception {
        String[] ids = createAggregatedScenario();

        deleteEntity(EntityType.OBSERVED_PROPERTY, ids[2], false);

        assertCollectionEmpty(EntityType.OBSERVED_PROPERTY);
        assertCollectionEmpty(EntityType.DATASTREAM);
        assertCollectionEmpty(EntityType.OBSERVATION);
    }

    /**
     * Creates a Thing with a Datastream that consists of two Datasets: posting
     * Observations with different FeaturesOfInterest turns the Datastream into
     * an Aggregation of multiple Datasets.
     *
     * @return ids of Thing, Sensor, ObservedProperty and Datastream (in this order)
     */
    private String[] createAggregatedScenario() throws Exception {
        String thingId = createThing("thing aggregated");
        String sensorId = createSensor();
        String observedPropertyId = createObservedProperty("Temperature");
        String datastreamId = createDatastream(thingId, sensorId, observedPropertyId);

        // First Observation assigns the (so far featureless) Dataset to FOI 1
        createObservation(datastreamId, createFeatureOfInterest("foi 1", 10, 10),
                          "2015-03-01T00:00:00.000Z", 1);
        // Second Observation with a different FOI turns the Datastream into an
        // Aggregation consisting of two Datasets
        createObservation(datastreamId, createFeatureOfInterest("foi 2", 20, 20),
                          "2015-03-01T01:00:00.000Z", 2);

        // Still a single Datastream from the API point of view
        Assertions.assertEquals(1,
                                getCollection(EntityType.DATASTREAM).get("value").size(),
                                "Expected exactly one Datastream before deletion");
        Assertions.assertEquals(2,
                                getCollection(EntityType.OBSERVATION).get("value").size(),
                                "Expected exactly two Observations before deletion");
        return new String[] {thingId, sensorId, observedPropertyId, datastreamId};
    }

    private void assertCollectionEmpty(EntityType type) throws Exception {
        JsonNode collection = getCollection(type);
        Assertions.assertTrue(collection.get("value").isEmpty(),
                              "Expected no remaining entities of type " + type);
    }

    private String createThing(String name) throws Exception {
        String body = "{"
            + "\"name\": \"" + name + "\","
            + "\"description\": \"" + name + "\""
            + "}";
        return postEntity(EntityType.THING, body).get(idKey).asText();
    }

    private String createSensor() throws Exception {
        String body = "{"
            + "\"name\": \"Fuguro Barometer\","
            + "\"description\": \"Fuguro Barometer\","
            + "\"encodingType\": \"application/pdf\","
            + "\"metadata\": \"Barometer\""
            + "}";
        return postEntity(EntityType.SENSOR, body).get(idKey).asText();
    }

    private String createObservedProperty(String name) throws Exception {
        String body = "{"
            + "\"name\": \"" + name + "\","
            + "\"definition\": \"http://dbpedia.org/page/" + name + "\","
            + "\"description\": \"" + name + "\""
            + "}";
        return postEntity(EntityType.OBSERVED_PROPERTY, body).get(idKey).asText();
    }

    private String createDatastream(String thingId, String sensorId, String observedPropertyId) throws Exception {
        String body = "{"
            + "\"unitOfMeasurement\": {"
            + "\"name\": \"Celsius\","
            + "\"symbol\": \"degC\","
            + "\"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\""
            + "},"
            + "\"name\": \"test datastream.\","
            + "\"description\": \"test datastream.\","
            + "\"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\","
            + "\"Thing\": {\"@iot.id\": " + escape(thingId) + "},"
            + "\"ObservedProperty\": {\"@iot.id\": " + escape(observedPropertyId) + "},"
            + "\"Sensor\": {\"@iot.id\": " + escape(sensorId) + "}"
            + "}";
        return postEntity(EntityType.DATASTREAM, body).get(idKey).asText();
    }

    private String createFeatureOfInterest(String name, int longitude, int latitude) throws Exception {
        String body = "{"
            + "\"name\": \"" + name + "\","
            + "\"description\": \"" + name + "\","
            + "\"encodingType\": \"application/vnd.geo+json\","
            + "\"feature\": {\"type\": \"Point\", \"coordinates\": [" + longitude + ", " + latitude + "]}"
            + "}";
        return postEntity(EntityType.FEATURE_OF_INTEREST, body).get(idKey).asText();
    }

    private String createObservation(String datastreamId,
                                     String featureOfInterestId,
                                     String phenomenonTime,
                                     int result) throws Exception {
        String body = "{"
            + "\"phenomenonTime\": \"" + phenomenonTime + "\","
            + "\"result\": " + result + ","
            + "\"Datastream\": {\"@iot.id\": " + escape(datastreamId) + "},"
            + "\"FeatureOfInterest\": {\"@iot.id\": " + escape(featureOfInterestId) + "}"
            + "}";
        return postEntity(EntityType.OBSERVATION, body).get(idKey).asText();
    }
}
