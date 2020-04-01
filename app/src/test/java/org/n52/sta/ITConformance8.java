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
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements Conformance Tests according to Section A.7 in OGC SensorThings API Part 1: Sensing (15-078r6)
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * @see <a href="http://docs.opengeospatial.org/is/15-078r6/15-078r6.html#54">
 * OGC SensorThings API Part 1: Sensing (15-078r6)</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ITConformance8 extends ConformanceTests implements TestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ITConformance8.class);

    private static IMqttClient mqttClient;

    public ITConformance8(@Value("${server.rootUrl}") String rootUrl) {
        super(rootUrl);
    }

    private void connectClient() throws MqttException {
        MqttClient client = new MqttClient("tcp://localhost:1883", "ITConformance8");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.connect(options);
        mqttClient = client;
    }

    @AfterAll static void disconnectClient() throws MqttException {
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
            mqttClient.close();
        }
    }

    void init() throws MqttException {
        connectClient();
    }

    /**
     * Subscribe to an entity set with MQTT Subscribe.
     * Then create a new entity of the subscribed entity set.
     * Check if a complete JSON representation of the newly created entity through MQTT is received.
     * Subscribe to an entity set with MQTT Subscribe.
     * Then update an existing entity of the subscribed entity set.
     * Check if a complete JSON representation of the updated entity through MQTT is received.
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkThingsRootSubscription() throws MqttException, IOException {
        init();
        /* Thing */
        EntityType type = EntityType.THING;
        String thing = demoThing;

        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);
        mqttClient.subscribe(endpoints.get(type));

        JsonNode entity = postEntity(type, thing);
        MqttMessage message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(entity, mapper.readTree(message.toString()));

        /* Thing Patch */
        String patch = "{\"description\":\"This is a PATCHED Test Thing\"}";
        Map<String, String> diffs = new HashMap<>();
        diffs.put("description", "This is a PATCHED Test Thing");
        JsonNode updatedEntity = patchEntity(type, patch, entity.get(idKey).asText());

        message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(updatedEntity, mapper.readTree(message.toString()));
    }

    /**
     * Subscribe to an entity’s property with MQTT Subscribe.
     * Then update the property with PATCH.
     * Check if the JSON object of the updated property is received.
     */
    @Test
    public void checkThingsPropertySubscription() throws MqttException, IOException {
        init();
        /* Thing */
        EntityType type = EntityType.THING;
        Map<String, String> patchMap = new HashMap<>();
        String thing = demoThing;
        patchMap.put("description", "{\"description\":\"This is a PATCHED Description\"}");
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        JsonNode entity = postEntity(type, thing);
        testPatch(patchMap, type, entity.get(idKey).asText());
    }

    /**
     * Subscribe to an entity set with MQTT Subscribe.
     * Then create a new entity of the subscribed entity set.
     * Check if a complete JSON representation of the newly created entity through MQTT is received.
     * Subscribe to an entity set with MQTT Subscribe.
     * Then update an existing entity of the subscribed entity set.
     * Check if a complete JSON representation of the updated entity through MQTT is received.
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkLocationsRootSubscription() throws MqttException, IOException {
        init();
        /* Location */
        EntityType type = EntityType.LOCATION;
        String location = demoLocation;

        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);
        mqttClient.subscribe(endpoints.get(type));

        JsonNode entity = postEntity(type, location);
        MqttMessage message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(entity, mapper.readTree(message.toString()));

        /* Location Patch */
        String patch = "{\"location\": { \"type\": \"Point\", \"coordinates\": [114.05, -50] }}";
        Map<String, String> diffs = new HashMap<>();
        diffs.put("location", "{ \"type\": \"Point\", \"coordinates\": [114.05, -50] }}");
        JsonNode updatedEntity = patchEntity(type, patch, entity.get(idKey).asText());

        message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(updatedEntity, mapper.readTree(message.toString()));
    }

    /**
     * Subscribe to an entity’s property with MQTT Subscribe.
     * Then update the property with PATCH.
     * Check if the JSON object of the updated property is received.
     */
    @Test
    public void checkLocationsPropertySubscription() throws MqttException, IOException {
        init();
        Map<String, String> patchMap = new HashMap<>();
        EntityType type = EntityType.LOCATION;
        String location = demoLocation;
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        patchMap.put("description", "{\"description\":\"This is a PATCHED Description\"}");
        patchMap.put("location", "{\"location\":{ \"type\": \"Point\", \"coordinates\": [-114.05, 50] }}}");
        JsonNode entity = postEntity(type, location);
        testPatch(patchMap, type, entity.get(idKey).asText());
    }

    /**
     * Subscribe to an entity set with MQTT Subscribe.
     * Then create a new entity of the subscribed entity set.
     * Check if a complete JSON representation of the newly created entity through MQTT is received.
     * Subscribe to an entity set with MQTT Subscribe.
     * Then update an existing entity of the subscribed entity set.
     * Check if a complete JSON representation of the updated entity through MQTT is received.
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkSensorRootSubscription() throws MqttException, IOException {
        init();
        /* Sensor */
        EntityType type = EntityType.SENSOR;
        String sensor = demoSensor;

        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);
        mqttClient.subscribe(endpoints.get(type));

        JsonNode entity = postEntity(type, sensor);
        MqttMessage message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(entity, mapper.readTree(message.toString()));

        /* Sensor Patch */
        String patch = "{\"metadata\": \"PATCHED\"}";
        Map<String, String> diffs = new HashMap<>();
        diffs.put("metadata", "PATCHED");
        JsonNode updatedEntity = patchEntity(type, patch, entity.get(idKey).asText());

        message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(updatedEntity, mapper.readTree(message.toString()));
    }

    /**
     * Subscribe to an entity’s property with MQTT Subscribe.
     * Then update the property with PATCH.
     * Check if the JSON object of the updated property is received.
     */
    @Test
    public void checkSensorPropertySubscription() throws MqttException, IOException {
        init();
        Map<String, String> patchMap = new HashMap<>();
        EntityType type = EntityType.SENSOR;
        String sensor = demoSensor;
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        patchMap.put("description", "{\"description\":\"This is a PATCHED Description\"}");
        patchMap.put("encodingType", "{\"encodingType\":\"http://www.opengis.net/doc/IS/SensorML/2.0\"}");
        patchMap.put("metadata", "{\"metadata\":\"This is a PATCHED metadata\"}");
        JsonNode entity = postEntity(type, sensor);
        testPatch(patchMap, type, entity.get(idKey).asText());
    }

    /**
     * Subscribe to an entity set with MQTT Subscribe.
     * Then create a new entity of the subscribed entity set.
     * Check if a complete JSON representation of the newly created entity through MQTT is received.
     * Subscribe to an entity set with MQTT Subscribe.
     * Then update an existing entity of the subscribed entity set.
     * Check if a complete JSON representation of the updated entity through MQTT is received.
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkObservedPropertyRootSubscription() throws MqttException, IOException {
        init();
        /* ObservedProperty */
        EntityType type = EntityType.OBSERVED_PROPERTY;
        String obsProp = demoObsProp;

        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);
        mqttClient.subscribe(endpoints.get(type));

        JsonNode entity = postEntity(type, obsProp);
        MqttMessage message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(entity, mapper.readTree(message.toString()));

        /* ObservedProperty Patch */
        String patch = "{\"description\":\"PATCHED\"}";
        Map<String, String> diffs = new HashMap<>();
        diffs.put("description", "PATCHED");
        JsonNode updatedEntity = patchEntity(type, patch, entity.get(idKey).asText());

        message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(updatedEntity, mapper.readTree(message.toString()));
    }

    /**
     * Subscribe to an entity’s property with MQTT Subscribe.
     * Then update the property with PATCH.
     * Check if the JSON object of the updated property is received.
     */
    @Test
    public void checkObservedPropertyPropertySubscription() throws MqttException, IOException {
        init();
        Map<String, String> patchMap = new HashMap<>();
        EntityType type = EntityType.OBSERVED_PROPERTY;
        String obsProp = demoObsProp;
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        patchMap.put("description", "{\"description\":\"This is a PATCHED description\"}");
        patchMap.put("definition", "{\"definition\":\"This is a PATCHED definition\"}");
        JsonNode entity = postEntity(type, obsProp);
        testPatch(patchMap, type, entity.get(idKey).asText());
    }

    /**
     * Subscribe to an entity set with MQTT Subscribe.
     * Then create a new entity of the subscribed entity set.
     * Check if a complete JSON representation of the newly created entity through MQTT is received.
     * Subscribe to an entity set with MQTT Subscribe.
     * Then update an existing entity of the subscribed entity set.
     * Check if a complete JSON representation of the updated entity through MQTT is received.
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkFOIRootSubscription() throws MqttException, IOException {
        init();
        /* FeatureOfInterest */
        EntityType type = EntityType.FEATURE_OF_INTEREST;
        String foi = demoFOI;

        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);
        mqttClient.subscribe(endpoints.get(type));

        JsonNode entity = postEntity(type, foi);
        MqttMessage message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(entity, mapper.readTree(message.toString()));

        /* ObservedProperty Patch */
        String patch = "{\"feature\":{ \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }}";
        Map<String, String> diffs = new HashMap<>();
        diffs.put("feature", "{ \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }");
        JsonNode updatedEntity = patchEntity(type, patch, entity.get(idKey).asText());

        message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(updatedEntity, mapper.readTree(message.toString()));
    }

    /**
     * Subscribe to an entity’s property with MQTT Subscribe.
     * Then update the property with PATCH.
     * Check if the JSON object of the updated property is received.
     */
    @Test
    public void checkFOIPropertySubscription() throws MqttException, IOException {
        init();
        Map<String, String> patchMap = new HashMap<>();
        EntityType type = EntityType.FEATURE_OF_INTEREST;
        String foi = demoFOI;
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        patchMap.put("description", "{\"description\":\"This is a PATCHED description\"}");
        // There is currently only a single encodingType specified
        //patchMap.put("encodingType", "{\"encodingType\":\"This is a PATCHED encodingType\"}");
        patchMap.put("feature", "{\"feature\":{ \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }}");
        JsonNode entity = postEntity(type, foi);
        testPatch(patchMap, type, entity.get(idKey).asText());
    }

    /**
     * Subscribe to an entity set with MQTT Subscribe.
     * Then create a new entity of the subscribed entity set.
     * Check if a complete JSON representation of the newly created entity through MQTT is received.
     * Subscribe to an entity set with MQTT Subscribe.
     * Then update an existing entity of the subscribed entity set.
     * Check if a complete JSON representation of the updated entity through MQTT is received.
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkDatastreamRootSubscription() throws MqttException, IOException {
        init();
        /* Thing */
        JsonNode entity = postEntity(EntityType.THING, demoThing);
        String thingId = entity.get(idKey).asText();

        /* ObservedProperty */
        entity = postEntity(EntityType.OBSERVED_PROPERTY, demoObsProp);
        String obsPropId = entity.get(idKey).asText();

        /* Sensor */
        entity = postEntity(EntityType.SENSOR, demoSensor);
        String sensorId = entity.get(idKey).asText();

        /* Datastream */
        EntityType type = EntityType.DATASTREAM;
        String datastream = "{\n"
                + "  \"unitOfMeasurement\": {\n"
                + "    \"name\": \"Celsius\",\n"
                + "    \"symbol\": \"degC\",\n"
                + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                + "  },\n"
                + "  \"name\": \"test datastream.\",\n"
                + "  \"description\": \"test datastream.\",\n"
                + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
                + "  \"ObservedProperty\":{ \"@iot.id\":" + escape(obsPropId) + "},\n"
                + "  \"Sensor\": { \"@iot.id\": " + escape(sensorId) + " }\n"
                + "}";

        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);
        mqttClient.subscribe(endpoints.get(type));

        entity = postEntity(type, datastream);
        MqttMessage message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(entity, mapper.readTree(message.toString()));

        /* Datastream Patch */
        String patch = "{\"description\": \"Patched Description\"}";
        Map<String, String> diffs = new HashMap<>();
        diffs.put("description", "Patched Description");
        JsonNode updatedEntity = patchEntity(type, patch, entity.get(idKey).asText());

        message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(updatedEntity, mapper.readTree(message.toString()));
    }

    /**
     * Subscribe to an entity’s property with MQTT Subscribe.
     * Then update the property with PATCH.
     * Check if the JSON object of the updated property is received.
     */
    @Test
    public void checkDatastreamPropertySubscription() throws MqttException, IOException {
        init();
        Map<String, String> patchMap = new HashMap<>();

        /* Thing */
        JsonNode entity = postEntity(EntityType.THING, demoThing);
        String thingId = entity.get(idKey).asText();

        /* ObservedProperty */
        entity = postEntity(EntityType.OBSERVED_PROPERTY, demoObsProp);
        String obsPropId = entity.get(idKey).asText();

        /* Sensor */
        entity = postEntity(EntityType.SENSOR, demoSensor);
        String sensorId = entity.get(idKey).asText();

        EntityType type = EntityType.DATASTREAM;
        String datastream = "{\n"
                + "  \"unitOfMeasurement\": {\n"
                + "    \"name\": \"Celsius\",\n"
                + "    \"symbol\": \"degC\",\n"
                + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                + "  },\n"
                + "  \"name\": \"test datastream.\",\n"
                + "  \"description\": \"test datastream.\",\n"
                + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
                + "  \"ObservedProperty\":{ \"@iot.id\":" + escape(obsPropId) + "},\n"
                + "  \"Sensor\": { \"@iot.id\": " + escape(sensorId) + " }\n"
                + "}";
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        patchMap.put("description", "{\"description\":\"This is a PATCHED description\"}");
        // patchMap.put("observationType", "{\"observationType\":\"This is a PATCHED observationType\"}");
        entity = postEntity(type, datastream);
        testPatch(patchMap, type, entity.get(idKey).asText());
    }

    /**
     * Subscribe to an entity set with MQTT Subscribe.
     * Then create a new entity of the subscribed entity set.
     * Check if a complete JSON representation of the newly created entity through MQTT is received.
     * Subscribe to an entity set with MQTT Subscribe.
     * Then update an existing entity of the subscribed entity set.
     * Check if a complete JSON representation of the updated entity through MQTT is received.
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkObservationRootSubscription() throws MqttException, IOException {
        init();
        /* Thing */
        JsonNode entity = postEntity(EntityType.THING, demoThing);
        String thingId = entity.get(idKey).asText();

        /* ObservedProperty */
        entity = postEntity(EntityType.OBSERVED_PROPERTY, demoObsProp);
        String obsPropId = entity.get(idKey).asText();

        /* Sensor */
        entity = postEntity(EntityType.SENSOR, demoSensor);
        String sensorId = entity.get(idKey).asText();

        /* FeatureOfInterest */
        entity = postEntity(EntityType.FEATURE_OF_INTEREST, demoFOI);
        String foiId = entity.get(idKey).asText();

        /* Datastream */
        String datastream = "{\n"
                + "  \"unitOfMeasurement\": {\n"
                + "    \"name\": \"Celsius\",\n"
                + "    \"symbol\": \"degC\",\n"
                + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                + "  },\n"
                + "  \"name\": \"test datastream.\",\n"
                + "  \"description\": \"test datastream.\",\n"
                + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
                + "  \"ObservedProperty\":{ \"@iot.id\":" + escape(obsPropId) + "},\n"
                + "  \"Sensor\": { \"@iot.id\": " + escape(sensorId) + " }\n"
                + "}";
        entity = postEntity(EntityType.DATASTREAM, datastream);
        String datastreamId = entity.get(idKey).asText();

        /* Observation */
        EntityType type = EntityType.OBSERVATION;
        String observation = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
                + "  \"result\": 8,\n"
                + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "},\n"
                + "  \"FeatureOfInterest\": {\"@iot.id\": " + escape(foiId) + "}  \n"
                + "}";

        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);
        mqttClient.subscribe(endpoints.get(type));

        entity = postEntity(type, observation);
        MqttMessage message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(entity, mapper.readTree(message.toString()));

        /* Observation Patch */
        String patch = "{\"phenomenonTime\": \"2015-07-01T00:40:00.000Z\"}";
        Map<String, String> diffs = new HashMap<>();
        diffs.put("phenomenonTime", "2015-07-01T00:40:00.000Z");
        JsonNode updatedEntity = patchEntity(type, patch, entity.get(idKey).asText());

        message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(updatedEntity, mapper.readTree(message.toString()));
    }

    /**
     * Subscribe to an entity’s property with MQTT Subscribe.
     * Then update the property with PATCH.
     * Check if the JSON object of the updated property is received.
     */
    @Test
    public void checkObservationPropertySubscription() throws MqttException, IOException {
        init();

        /* Thing */
        JsonNode entity = postEntity(EntityType.THING, demoThing);
        String thingId = entity.get(idKey).asText();

        /* ObservedProperty */
        entity = postEntity(EntityType.OBSERVED_PROPERTY, demoObsProp);
        String obsPropId = entity.get(idKey).asText();

        /* Sensor */
        entity = postEntity(EntityType.SENSOR, demoSensor);
        String sensorId = entity.get(idKey).asText();

        /* FeatureOfInterest */
        entity = postEntity(EntityType.FEATURE_OF_INTEREST, demoFOI);
        String foiId = entity.get(idKey).asText();

        String datastream = "{\n"
                + "  \"unitOfMeasurement\": {\n"
                + "    \"name\": \"Celsius\",\n"
                + "    \"symbol\": \"degC\",\n"
                + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                + "  },\n"
                + "  \"name\": \"test datastream.\",\n"
                + "  \"description\": \"test datastream.\",\n"
                + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
                + "  \"ObservedProperty\":{ \"@iot.id\":" + escape(obsPropId) + "},\n"
                + "  \"Sensor\": { \"@iot.id\": " + escape(sensorId) + " }\n"
                + "}";
        entity = postEntity(EntityType.DATASTREAM, datastream);
        String datastreamId = entity.get(idKey).asText();

        /* Observation */
        EntityType type = EntityType.OBSERVATION;
        String observation = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
                + "  \"result\": 8,\n"
                + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "},\n"
                + "  \"FeatureOfInterest\": {\"@iot.id\": " + escape(foiId) + "}  \n"
                + "}";
        entity = postEntity(type, observation);

        Map<String, String> patchMap = new HashMap<>();
//        patchMap.put("result", "{\"result\":\"52\"}");
//        patchMap.put("phenomenonTime", "{\"phenomenonTime\":\"2052-07-01T00:40:00.000Z\"}");
//        patchMap.put("resultTime", "{\"resultTime\":\"2052-07-01T00:40:00.000Z\"}");
//        patchMap.put("validTime", "{\"validTime\":\"2052-07-01T00:40:00.000Z\"}");
        patchMap.put("parameters", "{\"parameters\":[{\"name\":\"PATCHED Parameters\", \"value\":\"test\"}]}}");
        testPatch(patchMap, type, entity.get(idKey).asText());
    }

    private String demoThing = "{"
            + "\"name\":\"Test Thing\","
            + "\"description\":\"This is a Test Thing\""
            + "}";

    private String demoLocation = "{\n"
            + "  \"name\": \"bow river\",\n"
            + "  \"description\": \"bow river\",\n"
            + "  \"encodingType\": \"application/vnd.geo+json\",\n"
            + "  \"location\": { \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }\n"
            + "}";

    private String demoSensor = "{\n"
            + "  \"name\": \"Fuguro Barometer\",\n"
            + "  \"description\": \"Fuguro Barometer\",\n"
            + "  \"encodingType\": \"application/pdf\",\n"
            + "  \"metadata\": \"Barometer\"\n"
            + "}";

    private String demoObsProp = "{\n"
            + "  \"name\": \"DewPoint Temperature\",\n"
            + "  \"definition\": \"http://dbpedia.org/page/Dew_point\",\n"
            + "  \"description\": \"The dewpoint temperature \"\n"
            + "}";

    private String demoFOI = "{\n"
            + "  \"name\": \"A weather station.\",\n"
            + "  \"description\": \"A weather station.\",\n"
            + "  \"encodingType\": \"application/vnd.geo+json\",\n"
            + "  \"feature\": {\n"
            + "    \"type\": \"Point\",\n"
            + "    \"coordinates\": [\n"
            + "      10,\n"
            + "      10\n"
            + "    ]\n"
            + "  }\n"
            + "}";

    private void testPatch(Map<String, String> patchMap, EntityType type, String entityKey)
            throws MqttException, IOException {
        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);

        for (String key : patchMap.keySet()) {
            mqttClient.subscribe(endpoints.get(type) + "(" + entityKey + ")/" + key);

            JsonNode updatedEntity = patchEntity(type, patchMap.get(key), entityKey);
            MqttMessage message = listener.next();
            Assertions.assertNotNull(message);
            Assertions.assertEquals(updatedEntity.get(key), mapper.readTree(message.toString()).get(key));
        }
    }

    class MessageListener implements MqttCallback {

        final ArrayList<MqttMessage> messages;

        public MessageListener() {
            messages = new ArrayList<>();
        }

        public void messageArrived(String topic, MqttMessage message) throws Exception {
            LOGGER.info("Received: " + new String(message.getPayload()) + "'");

            synchronized (messages) {
                messages.add(message);
                messages.notifyAll();
            }
        }

        public MqttMessage next() {
            synchronized (messages) {
                if (messages.size() == 0) {
                    try {
                        messages.wait(10000);
                    } catch (InterruptedException e) {
                    }
                }

                if (messages.size() == 0) {
                    return null;
                }
                return messages.remove(0);
            }
        }

        public void connectionLost(Throwable cause) {
            LOGGER.error(cause.getMessage());
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
        }

    }
}
