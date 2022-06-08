/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.conformance;

import com.fasterxml.jackson.databind.JsonNode;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Implements Conformance Tests according to Section A.7 in OGC SensorThings API Part 1: Sensing (15-078r6)
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * @see <a href="http://docs.opengeospatial.org/is/15-078r6/15-078r6.html#54"> OGC SensorThings API Part 1:
 *      Sensing (15-078r6)</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ITConformance7 extends ConformanceTests {

    private static IMqttClient mqttClient;

    public ITConformance7(@Value("${server.config.service-root-url}") String rootUrl) {
        super(rootUrl);
    }

    @AfterAll
    static void disconnectClient() throws MqttException {
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
            mqttClient.close();
        }
    }

    private void connectClient() throws MqttException {
        MqttClient client = new MqttClient("tcp://localhost:1883", "ITConformance7");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.connect(options);
        mqttClient = client;
    }

    void init() throws Exception {
        connectClient();
        // Create required test harness
        // Requires POST with deep insert to work.
        postEntity(EntityType.THING,
                   "{\n"
                           +
                           "    \"description\": \"thing 1\",\n"
                           +
                           "    \"name\": \"thing name 1\",\n"
                           +
                           "    \"properties\": {\n"
                           +
                           "        \"reference\": \"first\"\n"
                           +
                           "    },\n"
                           +
                           "    \"Locations\": [\n"
                           +
                           "        {\n"
                           +
                           "            \"description\": \"location 1\",\n"
                           +
                           "            \"name\": \"location name 1\",\n"
                           +
                           "            \"location\": {\n"
                           +
                           "                \"type\": \"Point\",\n"
                           +
                           "                \"coordinates\": [\n"
                           +
                           "                    -117.05,\n"
                           +
                           "                    51.05\n"
                           +
                           "                ]\n"
                           +
                           "            },\n"
                           +
                           "            \"encodingType\": \"application/vnd.geo+json\"\n"
                           +
                           "        }\n"
                           +
                           "    ],\n"
                           +
                           "    \"Datastreams\": [\n"
                           +
                           "        {\n"
                           +
                           "            \"@iot.id\": \"ITConformance7Datastream\",\n"
                           +
                           "            \"unitOfMeasurement\": {\n"
                           +
                           "                \"name\": \"Lumen\",\n"
                           +
                           "                \"symbol\": \"lm\",\n"
                           +
                           "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen\"\n"
                           +
                           "            },\n"
                           +
                           "            \"description\": \"datastream 1\",\n"
                           +
                           "            \"name\": \"datastream name 1\",\n"
                           +
                           "            \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2"
                           +
                           ".0/OM_Measurement\",\n"
                           +
                           "            \"ObservedProperty\": {\n"
                           +
                           "                \"name\": \"Luminous Flux\",\n"
                           +
                           "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances"
                           +
                           ".html/LuminousFlux\",\n"
                           +
                           "                \"description\": \"observedProperty 1\"\n"
                           +
                           "            },\n"
                           +
                           "            \"Sensor\": {\n"
                           +
                           "                \"description\": \"sensor 1\",\n"
                           +
                           "                \"name\": \"sensor name 1\",\n"
                           +
                           "                \"encodingType\": \"application/pdf\",\n"
                           +
                           "                \"metadata\": \"Light flux sensor\"\n"
                           +
                           "            },\n"
                           +
                           "            \"Observations\": [\n"
                           +
                           "                {\n"
                           +
                           "                    \"phenomenonTime\": \"2015-03-03T00:00:00Z\",\n"
                           +
                           "                    \"result\": 3\n"
                           +
                           "                },\n"
                           +
                           "                {\n"
                           +
                           "                    \"phenomenonTime\": \"2015-03-04T00:00:00Z\",\n"
                           +
                           "                    \"result\": 4\n"
                           +
                           "                }\n"
                           +
                           "            ]\n"
                           +
                           "        }\n"
                           +
                           "    ]\n"
                           +
                           "}");

        postEntity(EntityType.FEATURE_OF_INTEREST,
                   "{\n"
                           +
                           "    \"@iot.id\": \"ITConformance7FOI\",\n"
                           +
                           "    \"name\": \"ITConformance7\",\n"
                           +
                           "    \"description\": \"ITConformance7 FOI\",\n"
                           +
                           "    \"encodingType\": \"application/vnd.geo+json\",\n"
                           +
                           "    \"feature\": {\n"
                           +
                           "      \"type\": \"Feature\","
                           +
                           "      \"geometry\": {\n"
                           +
                           "        \"type\": \"LineString\",\n"
                           +
                           "        \"coordinates\": [\n"
                           +
                           "          [0, 0.0], [52, 52]\n"
                           +
                           "        ]\n"
                           +
                           "      }\n"
                           +
                           "    }\n"
                           +
                           "}");
    }

    @Test
    public void postObservationDirect() throws Exception {
        init();
        String observation = "{\n"
                +
                "    \"phenomenonTime\": \"2019-03-10T17:45:09Z\",\n"
                +
                "    \"resultTime\": \"2019-03-10T16:58:09Z\",\n"
                +
                "    \"result\": 0.29,\n"
                +
                "    \"parameters\": {\n"
                +
                "        \"http://www.opengis.net/def/param-name/OGC-OM/2.0/samplingGeometry\": {\n"
                +
                "            \"type\": \"Point\",\n"
                +
                "            \"coordinates\": [\n"
                +
                "                2,8466,\n"
                +
                "                41.585\n"
                +
                "            ]\n"
                +
                "        }\n"
                +
                "    },\n"
                +
                "    \"Datastream\": {\n"
                +
                "        \"@iot.id\": \"ITConformance7Datastream\"\n"
                +
                "    },\n"
                +
                "    \"FeatureOfInterest\": {\n"
                +
                "        \"@iot.id\": \"ITConformance7FOI\"\n"
                +
                "    }\n"
                +
                "}";
        mqttClient.publish(MQTT_TOPIC_PREFIX + "Observations", observation.getBytes(), 1, false);

        JsonNode response = getCollection(EntityType.OBSERVATION);
        Assertions.assertTrue(response.has(value));

        assertResponseCount(
                            response,
                            3,
                            3);
    }

    @Test
    public void postObservationRelatedDatastream() throws Exception {
        init();
        String observation = "{\n"
                +
                "    \"result\": 0.29,\n"
                +
                "    \"phenomenonTime\": \"2019-03-10T17:45:09Z\",\n"
                +
                "    \"resultTime\": \"2019-03-10T16:58:09Z\",\n"
                +
                "    \"FeatureOfInterest\": {\n"
                +
                "        \"@iot.id\": \"ITConformance7FOI\"\n"
                +
                "    }\n"
                +
                "}";
        mqttClient.publish(MQTT_TOPIC_PREFIX + "Datastreams(ITConformance7Datastream)/Observations",
                           observation.getBytes(),
                           1,
                           false);

        JsonNode response = getCollection(EntityType.OBSERVATION);
        Assertions.assertTrue(response.has(value));

        assertResponseCount(
                            response,
                            3,
                            3);
    }

    @Test
    public void postObservationRelatedFOI() throws Exception {
        init();
        String observation = "{\n"
                +
                "    \"result\": 0.29,\n"
                +
                "    \"phenomenonTime\": \"2019-03-10T17:45:09Z\",\n"
                +
                "    \"resultTime\": \"2019-03-10T16:58:09Z\",\n"
                +
                "    \"Datastream\": {\n"
                +
                "        \"@iot.id\": \"ITConformance7Datastream\"\n"
                +
                "    }\n"
                +
                "}";
        mqttClient.publish(MQTT_TOPIC_PREFIX + "FeaturesOfInterest(ITConformance7FOI)/Observations",
                           observation.getBytes(),
                           1,
                           false);

        JsonNode response = getCollection(EntityType.OBSERVATION);
        Assertions.assertTrue(response.has(value));

        assertResponseCount(
                            response,
                            3,
                            3);
    }

}
