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
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n52.shetland.ogc.gml.time.TimeInstant;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
     * Checks all Mqtt Subscriptions dealing with Things
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkThings() throws MqttException, IOException {
        init();
        /* Thing */
        EntityType type = EntityType.THING;
        String source = demoThing;
        Map<String, String> patchMap = new HashMap<>();
        patchMap.put("description", "{\"description\":\"This is a PATCHED Description\"}");
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");

        testCollectionSubscriptionOnNewEntityCreation(type, source);
        deleteCollection(type);
        testCollectionSubscriptionOnExistingEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testPropertySubscriptionOnEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnNewEntityCreation(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnEntityPatch(patchMap, type, source);
    }

    /**
     * Checks all Mqtt Subscriptions dealing with Locations
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkLocations() throws MqttException, IOException {
        init();
        /* Location */
        EntityType type = EntityType.LOCATION;
        String source = demoLocation;
        Map<String, String> patchMap = new HashMap<>();
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        patchMap.put("description", "{\"description\":\"This is a PATCHED Description\"}");
        patchMap.put("location", "{\"location\":{ \"type\": \"Point\", \"coordinates\": [-114.05, 50] }}}");

        testCollectionSubscriptionOnNewEntityCreation(type, source);
        deleteCollection(type);
        testCollectionSubscriptionOnExistingEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testPropertySubscriptionOnEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnNewEntityCreation(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnEntityPatch(patchMap, type, source);
    }

    /**
     * Checks all Mqtt Subscriptions dealing with Sensors
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkSensors() throws MqttException, IOException {
        init();
        /* Sensor */
        Map<String, String> patchMap = new HashMap<>();
        EntityType type = EntityType.SENSOR;
        String source = demoSensor;
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        patchMap.put("description", "{\"description\":\"This is a PATCHED Description\"}");
        patchMap.put("encodingType", "{\"encodingType\":\"http://www.opengis.net/doc/IS/SensorML/2.0\"}");
        patchMap.put("metadata", "{\"metadata\":\"This is a PATCHED metadata\"}");

        testCollectionSubscriptionOnNewEntityCreation(type, source);
        deleteCollection(type);
        testCollectionSubscriptionOnExistingEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testPropertySubscriptionOnEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnNewEntityCreation(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnEntityPatch(patchMap, type, source);
    }


    /**
     * Checks all Mqtt Subscriptions dealing with Sensors
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkObservedProperties() throws MqttException, IOException {
        init();
        /* ObservedProperty */
        Map<String, String> patchMap = new HashMap<>();
        EntityType type = EntityType.OBSERVED_PROPERTY;
        String source = demoObsProp;
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        patchMap.put("description", "{\"description\":\"This is a PATCHED description\"}");
        patchMap.put("definition", "{\"definition\":\"This is a PATCHED definition\"}");

        testCollectionSubscriptionOnNewEntityCreation(type, source);
        deleteCollection(type);
        testCollectionSubscriptionOnExistingEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testPropertySubscriptionOnEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnNewEntityCreation(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnEntityPatch(patchMap, type, source);
    }

    /**
     * Checks all Mqtt Subscriptions dealing with FeaturesOfInterest
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkFOI() throws MqttException, IOException {
        init();
        /* FeatureOfInterest */
        Map<String, String> patchMap = new HashMap<>();
        EntityType type = EntityType.FEATURE_OF_INTEREST;
        String source = demoFOI;
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        patchMap.put("description", "{\"description\":\"This is a PATCHED description\"}");
        // There is currently only a single encodingType specified
        //patchMap.put("encodingType", "{\"encodingType\":\"This is a PATCHED encodingType\"}");
        patchMap.put("feature", "{\"feature\":{ \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }}");

        testCollectionSubscriptionOnNewEntityCreation(type, source);
        deleteCollection(type);
        testCollectionSubscriptionOnExistingEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testPropertySubscriptionOnEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnNewEntityCreation(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnEntityPatch(patchMap, type, source);
    }

    /**
     * Checks all Mqtt Subscriptions dealing with Datastreams
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkDatastreams() throws MqttException, IOException {
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
        String source = "{\n"
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

        Map<String, String> patchMap = new HashMap<>();
        patchMap.put("name", "{\"name\":\"This is a PATCHED Name\"}");
        patchMap.put("description", "{\"description\":\"This is a PATCHED description\"}");

        testCollectionSubscriptionOnNewEntityCreation(type, source);
        deleteCollection(type);
        testCollectionSubscriptionOnExistingEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testPropertySubscriptionOnEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnNewEntityCreation(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnEntityPatch(patchMap, type, source);
    }

    /**
     * Checks all Mqtt Subscriptions dealing with Observations
     *
     * @throws MqttException when an error occurred
     * @throws IOException   when an error occurred
     */
    @Test
    public void checkObservations() throws MqttException, IOException {
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
        String source = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
                + "  \"validTime\": \"2015-03-01T00:40:00.000Z\",\n"
                + "  \"result\": 8,\n"
                + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "},\n"
                + "  \"FeatureOfInterest\": {\"@iot.id\": " + escape(foiId) + "}  \n"
                + "}";

        Map<String, String> patchMap = new HashMap<>();
        patchMap.put("result", "{\"result\":\"52.0\"}");
        patchMap.put("phenomenonTime", "{\"phenomenonTime\":\"2052-07-01T00:40:00.000Z\"}");
        patchMap.put("resultTime", "{\"resultTime\":\"2053-07-01T00:40:00.000Z\"}");
        patchMap.put("validTime", "{\"validTime\":\"2053-07-01T00:40:00.000Z\"}");
        patchMap.put("parameters", "{\"parameters\":[{\"name\":\"PATCHED Parameters\", \"value\":\"test\"}]}}");

        testCollectionSubscriptionOnNewEntityCreation(type, source);
        deleteCollection(type);
        testCollectionSubscriptionOnExistingEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testPropertySubscriptionOnEntityPatch(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnNewEntityCreation(patchMap, type, source);
        deleteCollection(type);
        testSelectSubscriptionOnEntityPatch(patchMap, type, source);
    }

    private void deleteCollection(EntityType type) throws IOException {
        for (JsonNode elem : getCollection(type).get("value")) {
            deleteEntity(type, elem.get(idKey).asText(), false);
        }
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
            + "  \"definition\": \"http://dbpedia.org/page/Dew_point" + System.currentTimeMillis() +"\",\n"
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

    /**
     * Subscribe to an entity set with MQTT Subscribe.
     * Then create a new entity of the subscribed entity set.
     * Check if a complete JSON representation of the newly created entity through MQTT is received.
     *
     * @param type   Type of the Entity
     * @param source Entity to be created
     * @throws MqttException if an error occurred
     * @throws IOException   if an error occurred
     */
    private void testCollectionSubscriptionOnNewEntityCreation(EntityType type, String source)
            throws MqttException, IOException {
        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);
        mqttClient.subscribe(endpoints.get(type));

        JsonNode entity = postEntity(type, source);
        MqttMessage message = listener.next();
        Assertions.assertNotNull(message);
        compareJsonNodes(entity, mapper.readTree(message.toString()));
        mqttClient.unsubscribe(endpoints.get(type));
    }

    /**
     * Subscribe to an entity set with MQTT Subscribe.
     * Then update an existing entity of the subscribed entity set.
     * Check if a complete JSON representation of the updated entity through MQTT is received.
     *
     * @param patchMap Map of Patches to be applied
     * @param type     Type of the Entity
     * @param source   Entity to be created
     * @throws MqttException if an error occurred
     * @throws IOException   if an error occurred
     */
    private void testCollectionSubscriptionOnExistingEntityPatch(Map<String, String> patchMap,
                                                                 EntityType type,
                                                                 String source) throws MqttException, IOException {
        MessageListener listener = new MessageListener();
        JsonNode entity = postEntity(type, source);
        mqttClient.setCallback(listener);
        mqttClient.subscribe(endpoints.get(type));

        for (String key : patchMap.keySet()) {
            JsonNode updatedEntity = patchEntity(type, patchMap.get(key), entity.get(idKey).asText());

            MqttMessage message = listener.next();
            Assertions.assertNotNull(message);
            compareJsonNodes(updatedEntity, mapper.readTree(message.toString()));
        }
        mqttClient.unsubscribe(endpoints.get(type));
    }

    /**
     * Subscribe to an entity’s property with MQTT Subscribe.
     * Then update the property with PATCH.
     * Check if the JSON object of the updated property is received.
     *
     * @param patchMap Map of Patches to be applied
     * @param type     Type of the Entity
     * @param source   Entity to be created
     * @throws MqttException if an error occurred
     * @throws IOException   if an error occurred
     */
    private void testPropertySubscriptionOnEntityPatch(Map<String, String> patchMap,
                                                       EntityType type,
                                                       String source)
            throws MqttException, IOException {
        MessageListener listener = new MessageListener();
        JsonNode entity = postEntity(type, source);
        mqttClient.setCallback(listener);
        String entityKey = entity.get(idKey).asText();

        for (String key : patchMap.keySet()) {
            String topic = endpoints.get(type) + "(" + entityKey + ")/" + key;
            mqttClient.subscribe(topic);

            JsonNode updatedEntity = patchEntity(type, patchMap.get(key), entityKey);
            MqttMessage message = listener.next();
            Assertions.assertNotNull(message);
            Assertions.assertEquals(updatedEntity.get(key), mapper.readTree(message.toString()).get(key));
            mqttClient.unsubscribe(topic);
        }
    }

    /**
     * Subscribe to multiple properties of an entity set with MQTT Subscribe.
     * Then create a new entity of the entity set.
     * Check if a JSON object of the subscribed properties is received.
     *
     * @param patchMap Map of Patches to be applied
     * @param type     Type of the Entity
     * @param source   Entity to be created
     * @throws MqttException if an error occurred
     * @throws IOException   if an error occurred
     */
    private void testSelectSubscriptionOnNewEntityCreation(Map<String, String> patchMap, EntityType type, String source)
            throws IOException, MqttException {
        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);

        for (String key : patchMap.keySet()) {
            String topic = endpoints.get(type) + "?$select=" + key;
            mqttClient.subscribe(topic);
        }
        JsonNode entity = postEntity(type, source);

        for (String ignored : patchMap.keySet()) {
            JsonNode mqtt = mapper.readTree(listener.next().toString());
            Iterator<String> fieldNameIt = mqtt.fieldNames();
            Assertions.assertTrue(fieldNameIt.hasNext());
            String name = fieldNameIt.next();
            Assertions.assertFalse(fieldNameIt.hasNext());
            Assertions.assertEquals(entity.get(name).asText(), mqtt.get(name).asText());
        }

        for (String key : patchMap.keySet()) {
            String topic = endpoints.get(type) + "?$select=" + key;
            mqttClient.unsubscribe(topic);
        }
    }

    /**
     * Subscribe to multiple properties of an entity set with MQTT Subscribe.
     * Then update an existing entity of the entity set with PATCH.
     * Check if a JSON object of the subscribed properties is received.
     *
     * @param patchMap Map of Patches to be applied
     * @param type     Type of the Entity
     * @param source   Entity to be created
     * @throws MqttException if an error occurred
     * @throws IOException   if an error occurred
     */
    private void testSelectSubscriptionOnEntityPatch(Map<String, String> patchMap, EntityType type, String source)
            throws MqttException, IOException {
        MessageListener listener = new MessageListener();
        mqttClient.setCallback(listener);
        JsonNode entity = postEntity(type, source);

        for (String key : patchMap.keySet()) {
            String topic = endpoints.get(type) + "?$select=" + key;
            mqttClient.subscribe(topic);
        }

        Set<String> alreadyPatched = new HashSet<>();
        for (String patchKey : patchMap.keySet()) {
            patchEntity(type, patchMap.get(patchKey), entity.get(idKey).asText());
            alreadyPatched.add(patchKey);
            for (int i = 0; i < patchMap.keySet().size(); i++) {
                JsonNode mqtt = mapper.readTree(listener.next().toString());
                Iterator<String> fieldNameIt = mqtt.fieldNames();
                Assertions.assertTrue(fieldNameIt.hasNext());
                String name = fieldNameIt.next();
                Assertions.assertFalse(fieldNameIt.hasNext());
                if (alreadyPatched.contains(name)) {
                    if (name.equals("result")) {
                        Assertions.assertEquals(Double.valueOf(mapper.readTree(patchMap.get(name)).get(name).asText()),
                                                Double.valueOf(mqtt.get(name).asText()));
                    } else if (name.contains("Time")) {
                        Assertions.assertEquals(new TimeInstant(DateTime.parse(mapper.readTree(patchMap.get(name)).get(name).asText())),
                                                new TimeInstant(DateTime.parse(mqtt.get(name).asText())));
                    } else {
                        Assertions.assertEquals(mapper.readTree(patchMap.get(name)).get(name).asText(),
                                                mqtt.get(name).asText());
                    }
                } else {
                    Assertions.assertEquals(entity.get(name).asText(), mqtt.get(name).asText());
                }
            }
        }

        for (String key : patchMap.keySet()) {
            String topic = endpoints.get(type) + "?$select=" + key;
            mqttClient.unsubscribe(topic);
        }
    }

    static class MessageListener implements MqttCallback {

        final ArrayList<MqttMessage> messages;

        MessageListener() {
            messages = new ArrayList<>();
        }

        public void messageArrived(String topic, MqttMessage message) throws Exception {
            LOGGER.info("Received: " + new String(message.getPayload()) + "'");

            synchronized (messages) {
                messages.add(message);
                messages.notifyAll();
            }
        }

        MqttMessage next() {
            synchronized (messages) {
                if (messages.size() == 0) {
                    try {
                        messages.wait(10000);
                    } catch (InterruptedException ignored) {
                    }
                }

                if (messages.size() == 0) {
                    throw new IllegalStateException("No Mqtt Message received!");
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
