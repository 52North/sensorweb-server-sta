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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Implements Conformance Tests according to Section A.3 in OGC SensorThings API Part 1: Sensing (15-078r6)
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * @see <a href="http://docs.opengeospatial.org/is/15-078r6/15-078r6.html#54">
 * OGC SensorThings API Part 1: Sensing (15-078r6)</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class ITConformance3 extends ConformanceTests implements TestUtil {

    private static final Logger logger = LoggerFactory.getLogger(ITConformance3.class);

    public ITConformance3(@Value("${server.rootUrl}") String rootUrl) {
        super(rootUrl);
    }

    @Test
    @Order(1)
    public void createEntities() throws Exception {
        /* Thing */
        String urlParameters = "{"
            + "\"name\":\"Test Thing\","
            + "\"description\":\"This is a Test Thing From TestNG\""
            + "}";
        JsonNode entity = postEntity(EntityType.THING, urlParameters);
        String thingId = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);

        /* Location */
        urlParameters = "{\n"
            + "  \"name\": \"bow river\",\n"
            + "  \"description\": \"bow river\",\n"
            + "  \"encodingType\": \"application/vnd.geo+json\",\n"
            + "  \"location\": { \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }\n"
            + "}";
        JsonNode locationEntity = postEntity(EntityType.LOCATION, urlParameters);
        String locationId = locationEntity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), locationEntity);

        /* Sensor */
        urlParameters = "{\n"
            + "  \"name\": \"Fuguro Barometer\",\n"
            + "  \"description\": \"Fuguro Barometer\",\n"
            + "  \"encodingType\": \"application/pdf\",\n"
            + "  \"metadata\": \"Barometer\"\n"
            + "}";
        entity = postEntity(EntityType.SENSOR, urlParameters);
        String sensorId = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);

        /* ObservedProperty */
        urlParameters = "{\n"
            + "  \"name\": \"DewPoint Temperature\",\n"
            + "  \"definition\": \"http://dbpedia.org/page/Dew_point\",\n"
            +
            "  \"description\": \"The dewpoint temperature is the temperature to which the air must be cooled, at" +
            " constant pressure, for dew to form. As the grass and other objects near the ground cool to the " +
            "dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.\"\n"
            + "}";
        entity = postEntity(EntityType.OBSERVED_PROPERTY, urlParameters);
        String obsPropId = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);

        /* FeatureOfInterest */
        urlParameters = "{\n"
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
        entity = postEntity(EntityType.FEATURE_OF_INTEREST, urlParameters);
        String foiId = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);

        /* Datastream */
        urlParameters = "{\n"
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
        entity = postEntity(EntityType.DATASTREAM, urlParameters);
        String datastreamId = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);

        /* Observation */
        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
            + "  \"result\": 8,\n"
            + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "},\n"
            + "  \"FeatureOfInterest\": {\"@iot.id\": " + escape(foiId) + "}  \n"
            + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        String obsId1 = entity.get(idKey).asText();
        compareJsonNodes(mapper.readTree(urlParameters), entity);

        //POST Observation without FOI (Automatic creation of FOI)
        //Add location to the Thing
        urlParameters = "{\"Locations\":[{\"@iot.id\":" + escape(locationId) + "}]}";
        patchEntity(EntityType.THING, urlParameters, thingId);

        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-03-01T00:00:00.000Z\",\n"
            + "  \"resultTime\": \"2015-03-01T01:00:00.000Z\",\n"
            + "  \"result\": 100,\n"
            + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
            + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        compareJsonNodesTime(
            "resultTime",
            mapper.readTree("{\"resultTime\":\"2015-03-01T01:00:00.000Z\"}").get("resultTime"),
            entity.get("resultTime")
        );
        String obsId2 = entity.get(idKey).asText();
        String automatedFOIId = checkAutomaticInsertionOfFOI(obsId2, locationEntity, null);

        //POST another Observation to make sure it is linked to the previously created FOI
        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-05-01T00:00:00.000Z\",\n"
            + "  \"result\": 105,\n"
            + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
            + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        compareJsonNodesTime(
            "resultTime",
            mapper.readTree("{\"resultTime\":\"null\"}").get("resultTime"),
            entity.get("resultTime")
        );
        String obsId3 = entity.get(idKey).asText();
        checkAutomaticInsertionOfFOI(obsId2, locationEntity, automatedFOIId);

        // Move the Thing to a new location, create a new observation
        // without FOI, check if a new FOI is created from this new location.
        /* Second Location */
        urlParameters = "{\n"
            + "  \"name\": \"spear river\",\n"
            + "  \"description\": \"spear river\",\n"
            + "  \"encodingType\": \"application/vnd.geo+json\",\n"
            + "  \"location\": { \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }\n"
            + "}";
        entity = postEntity(EntityType.LOCATION, urlParameters);
        String location2Id = entity.get(idKey).asText();
        JsonNode location2Entity = entity;

        //Add second location to the Thing
        urlParameters = "{\"Locations\":[{\"@iot.id\":" + escape(location2Id) + "}]}";
        patchEntity(EntityType.THING, urlParameters, thingId);

        // Create a new Observation for Thing1 with no FoI.
        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-03-01T01:00:00.000Z\",\n"
            + "  \"resultTime\": \"2015-03-01T02:00:00.000Z\",\n"
            + "  \"result\": 200,\n"
            + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
            + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        String obsId4 = entity.get(idKey).asText();
        String automatedFOI2Id = checkAutomaticInsertionOfFOI(obsId4, location2Entity, null);
        Assertions.assertNotEquals(
            automatedFOIId,
            automatedFOI2Id,
            "A new FoI should have been created, since the Thing moved."
        );

        // Create a new Thing with the same Location, create a new
        // observation without FOI, check if the same FOI is used.
        /* Thing2 */
        urlParameters = "{"
            + "\"name\":\"Test Thing 2\","
            + "\"description\":\"This is a second Test Thing From TestNG\","
            + "\"Locations\":[{\"@iot.id\": " + escape(locationId) + "}]"
            + "}";
        entity = postEntity(EntityType.THING, urlParameters);
        String thing2Id = entity.get(idKey).asText();

        /* Datastream2 */
        urlParameters = "{\n"
            + "  \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Celsius\",\n"
            + "    \"symbol\": \"degC\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
            + "  },\n"
            + "  \"name\": \"test datastream 2.\",\n"
            + "  \"description\": \"test datastream 2.\",\n"
            + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "  \"Thing\": { \"@iot.id\": " + escape(thing2Id) + " },\n"
            + "  \"ObservedProperty\":{ \"@iot.id\":" + escape(obsPropId) + "},\n"
            + "  \"Sensor\": { \"@iot.id\": " + escape(sensorId) + " }\n"
            + "}";
        entity = postEntity(EntityType.DATASTREAM, urlParameters);
        String datastream2Id = entity.get(idKey).asText();

        /* Post new Observation without FoI */
        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-03-01T03:00:00.000Z\",\n"
            + "  \"resultTime\": \"2015-03-01T04:00:00.000Z\",\n"
            + "  \"result\": 300,\n"
            + "  \"Datastream\":{\"@iot.id\": " + escape(datastream2Id) + "}\n"
            + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        String obsId5 = entity.get(idKey).asText();
        String automatedFOI3Id = checkAutomaticInsertionOfFOI(obsId5, locationEntity, null);
        Assertions.assertEquals(
            automatedFOIId,
            automatedFOI3Id,
            "The generated FoI should be the same as the first generated FoI, since Thing2 has the same Location."
        );

        /* HistoricalLocation */
        urlParameters = "{\n"
            + "  \"time\": \"2015-03-01T00:40:00.000Z\",\n"
            + "  \"Thing\":{\"@iot.id\": " + escape(thingId) + "},\n"
            + "  \"Locations\": [{\"@iot.id\": " + escape(locationId) + "}]  \n"
            + "}";
        entity = postEntity(EntityType.HISTORICAL_LOCATION, urlParameters);
        String histLocId = entity.get(idKey).asText();
    }

    /**
     * This method is testing create or POST in the form of Deep Insert. It
     * makes sure the response is 201. Also using GET requests, it makes sure
     * the entity and all its related entities are created and added to the
     * service.
     */
    @Test
    @Order(2)
    public void createEntitiesWithDeepInsert() throws Exception {
        /* Thing */
        String urlParameters = "{\n"
            + "  \"name\": \"Office Building\",\n"
            + "  \"description\": \"Office Building\",\n"
            + "  \"properties\": {\n"
            + "    \"reference\": \"Third Floor\"\n"
            + "  },\n"
            + "  \"Locations\": [\n"
            + "    {\n"
            + "      \"name\": \"West Roof\",\n"
            + "      \"description\": \"West Roof\",\n"
            + "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117.05, 51.05] },\n"
            + "      \"encodingType\": \"application/vnd.geo+json\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"Datastreams\": [\n"
            + "    {\n"
            + "      \"unitOfMeasurement\": {\n"
            + "        \"name\": \"Lumen\",\n"
            + "        \"symbol\": \"lm\",\n"
            + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
            + "      },\n"
            + "      \"name\": \"Light exposure.\",\n"
            + "      \"description\": \"Light exposure.\",\n"
            +
            "      \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "      \"ObservedProperty\": {\n"
            + "        \"name\": \"Luminous Flux\",\n"
            +
            "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
            +
            "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of " +
            "light.\"\n"
            + "      },\n"
            + "      \"Sensor\": {        \n"
            + "        \"name\": \"Acme Fluxomatic 1000\",\n"
            + "        \"description\": \"Acme Fluxomatic 1000\",\n"
            + "        \"encodingType\": \"application/pdf\",\n"
            + "        \"metadata\": \"Light flux sensor\"\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}";
        JsonNode entity = postEntity(EntityType.THING, urlParameters);
        String thingId = entity.get(idKey).asText();
        //Check Datastream
        JsonNode deepInsertedObj = mapper.readTree("{\n"
                                                       + "      \"unitOfMeasurement\": {\n"
                                                       + "        \"name\": \"Lumen\",\n"
                                                       + "        \"symbol\": \"lm\",\n"
                                                       +
                                                       "        \"definition\": \"http://www.qudt.org/qudt/owl/1" +
                                                       ".0.0/unit/Instances.html#Lumen\"\n"
                                                       + "      },\n"
                                                       + "      \"name\": \"Light exposure.\",\n"
                                                       + "      \"description\": \"Light exposure.\",\n"
                                                       +
                                                       "      \"observationType\": \"http://www.opengis" +
                                                       ".net/def/observationType/OGC-OM/2.0/OM_Measurement\"\n"
                                                       + "    }\n");
        String datastreamId = checkRelatedEntity(EntityType.THING, thingId, EntityType.DATASTREAM, deepInsertedObj);
        //Check Location
        deepInsertedObj = mapper.readTree("{\n"
                                              + "      \"name\": \"West Roof\",\n"
                                              + "      \"description\": \"West Roof\",\n"
                                              +
                                              "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117" +
                                              ".05, 51.05] },\n"
                                              + "      \"encodingType\": \"application/vnd.geo+json\"\n"
                                              + "    }\n");
        checkRelatedEntity(EntityType.THING, thingId, EntityType.LOCATION, deepInsertedObj);
        //Check Sensor
        deepInsertedObj = mapper.readTree("{\n"
                                              + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                                              + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                                              + "        \"encodingType\": \"application/pdf\",\n"
                                              + "        \"metadata\": \"Light flux sensor\"\n"
                                              + "      }\n");
        checkRelatedEntity(EntityType.DATASTREAM, datastreamId, EntityType.SENSOR, deepInsertedObj);
        //Check ObservedProperty
        deepInsertedObj = mapper.readTree("{\n"
                                              + "        \"name\": \"Luminous Flux\",\n"
                                              +
                                              "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0" +
                                              ".0/quantity/Instances.html#LuminousFlux\",\n"
                                              +
                                              "        \"description\": \"Luminous Flux or Luminous Power is the " +
                                              "measure of the perceived power of light.\"\n"
                                              + "      },\n");
        checkRelatedEntity(EntityType.DATASTREAM, datastreamId, EntityType.OBSERVED_PROPERTY, deepInsertedObj);

        /* Datastream */
        urlParameters = "{\n"
            + "  \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Celsius\",\n"
            + "    \"symbol\": \"degC\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
            + "  },\n"
            + "  \"name\": \"test datastream.\",\n"
            + "  \"description\": \"test datastream.\",\n"
            + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
            + "   \"ObservedProperty\": {\n"
            + "        \"name\": \"More Luminous Flux\",\n"
            +
            "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances" +
            ".html#LuminousFluxWithMorePower\",\n"
            +
            "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of " +
            "light. This has even more power than regular flux.\"\n"
            + "   },\n"
            + "   \"Sensor\": {        \n"
            + "        \"name\": \"Acme Fluxomatic 1000\",\n"
            + "        \"description\": \"Acme Fluxomatic 1000\",\n"
            + "        \"encodingType\": \"application/pdf\",\n"
            + "        \"metadata\": \"Light flux sensor\"\n"
            + "   },\n"
            + "      \"Observations\": [\n"
            + "        {\n"
            + "          \"phenomenonTime\": \"2015-03-01T00:10:00Z\",\n"
            + "          \"result\": 10\n"
            + "        }\n"
            + "      ]"
            + "}";
        entity = postEntity(EntityType.DATASTREAM, urlParameters);
        datastreamId = entity.get(idKey).asText();
        //Check Sensor
        deepInsertedObj = mapper.readTree("{\n"
                                              + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                                              + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                                              + "        \"encodingType\": \"application/pdf\",\n"
                                              + "        \"metadata\": \"Light flux sensor\"\n"
                                              + "      }\n");
        checkRelatedEntity(EntityType.DATASTREAM, datastreamId, EntityType.SENSOR, deepInsertedObj);
        //Check ObservedProperty
        deepInsertedObj = mapper.readTree("{\n"
                                              + "\"name\": \"More Luminous Flux\",\n"
                                              +
                                              "\"definition\": \"http://www.qudt.org/qudt/owl/1.0" +
                                              ".0/quantity/Instances.html#LuminousFluxWithMorePower\",\n"
                                              +
                                              "\"description\": \"Luminous Flux or Luminous Power is the measure " +
                                              "of the perceived power of light. This has even more power than " +
                                              "regular flux.\"\n"
                                              + "}\n");
        checkRelatedEntity(EntityType.DATASTREAM, datastreamId, EntityType.OBSERVED_PROPERTY, deepInsertedObj);
        //Check Observation
        deepInsertedObj = mapper.readTree("{\n"
                                              + "          \"phenomenonTime\": \"2015-03-01T00:10:00.000Z\",\n"
                                              + "          \"result\": 10\n"
                                              + "        }\n");
        checkRelatedEntity(EntityType.DATASTREAM, datastreamId, EntityType.OBSERVATION, deepInsertedObj);

        /* Observation */
        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
            + "  \"result\": 100,\n"
            + "  \"FeatureOfInterest\": {\n"
            + "  \t\"name\": \"A weather station.\",\n"
            + "  \t\"description\": \"A weather station.\",\n"
            + "  \t\"encodingType\": \"application/vnd.geo+json\",\n"
            + "    \"feature\": {\n"
            + "      \"type\": \"Point\",\n"
            + "      \"coordinates\": [\n"
            + "        -114.05,\n"
            + "        51.05\n"
            + "      ]\n"
            + "    }\n"
            + "  },\n"
            + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
            + "}";
        entity = postEntity(EntityType.OBSERVATION, urlParameters);
        String obsId1 = entity.get(idKey).asText();
        //Check FeaturOfInterest
        deepInsertedObj = mapper.readTree("{\n"
                                              + "  \"name\": \"A weather station.\",\n"
                                              + "  \"description\": \"A weather station.\",\n"
                                              + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                                              + "    \"feature\": {\n"
                                              + "      \"type\": \"Point\",\n"
                                              + "      \"coordinates\": [\n"
                                              + "        -114.05,\n"
                                              + "        51.05\n"
                                              + "      ]\n"
                                              + "    }\n"
                                              + "  }\n");
        checkRelatedEntity(EntityType.OBSERVATION, obsId1, EntityType.FEATURE_OF_INTEREST, deepInsertedObj);
    }

    @Test
    @Order(3)
    public void createInvalidEntitiesWithDeepInsert() throws Exception {
        String urlParameters = "{\n"
            + "  \"name\": \"Office Building\",\n"
            + "  \"description\": \"Office Building\",\n"
            + "  \"properties\": {\n"
            + "    \"reference\": \"Third Floor\"\n"
            + "  },\n"
            + "  \"Locations\": [\n"
            + "    {\n"
            + "      \"name\": \"West Roof\",\n"
            + "      \"description\": \"West Roof\",\n"
            + "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117.05, 51.05] },\n"
            + "      \"encodingType\": \"application/vnd.geo+json\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"Datastreams\": [\n"
            + "    {\n"
            + "      \"unitOfMeasurement\": {\n"
            + "        \"name\": \"Lumen\",\n"
            + "        \"symbol\": \"lm\",\n"
            + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
            + "      },\n"
            + "      \"name\": \"Light exposure.\",\n"
            + "      \"description\": \"Light exposure.\",\n"
            +
            "      \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "      \"ObservedProperty\": {\n"
            + "        \"name\": \"Luminous Flux\",\n"
            +
            "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances" +
            ".html#LuminousFluxWithEvenMorePower\",\n"
            +
            "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of " +
            "light.\"\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}";
        postInvalidEntity(EntityType.THING, urlParameters);
        Set<EntityType> entityTypesToCheck = new HashSet<>();
        entityTypesToCheck.add(EntityType.THING);
        entityTypesToCheck.add(EntityType.LOCATION);
        entityTypesToCheck.add(EntityType.HISTORICAL_LOCATION);
        entityTypesToCheck.add(EntityType.DATASTREAM);
        entityTypesToCheck.add(EntityType.OBSERVED_PROPERTY);
        checkNotExisting(entityTypesToCheck);

        /* Datastream */
        urlParameters = "{"
            + "\"name\": \"Office Building\","
            + "\"description\": \"Office Building\""
            + "}";
        String thingId = postEntity(EntityType.THING, urlParameters).get(idKey).asText();

        urlParameters = "{\n"
            + "  \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Celsius\",\n"
            + "    \"symbol\": \"degC\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
            + "  },\n"
            + "  \"name\": \"test datastream.\",\n"
            + "  \"description\": \"test datastream.\",\n"
            + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
            + "   \"ObservedProperty\": {\n"
            + "        \"name\": \"Luminous Flux\",\n"
            +
            "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
            +
            "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of " +
            "light.\"\n"
            + "   },\n"
            + "      \"Observations\": [\n"
            + "        {\n"
            + "          \"phenomenonTime\": \"2015-03-01T00:10:00Z\",\n"
            + "          \"result\": 10\n"
            + "        }\n"
            + "      ]"
            + "}";
        postInvalidEntity(EntityType.DATASTREAM, urlParameters);

        urlParameters = "{\n"
            + "  \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Celsius\",\n"
            + "    \"symbol\": \"degC\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
            + "  },\n"
            + "  \"name\": \"test datastream.\",\n"
            + "  \"description\": \"test datastream.\",\n"
            + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
            + "   \"Sensor\": {        \n"
            + "        \"name\": \"Acme Fluxomatic 1000\",\n"
            + "        \"description\": \"Acme Fluxomatic 1000\",\n"
            + "        \"encodingType\": \"application/pdf\",\n"
            + "        \"metadata\": \"Light flux sensor\"\n"
            + "   },\n"
            + "      \"Observations\": [\n"
            + "        {\n"
            + "          \"phenomenonTime\": \"2015-03-01T00:10:00Z\",\n"
            + "          \"result\": 10\n"
            + "        }\n"
            + "      ]"
            + "}";
        postInvalidEntity(EntityType.DATASTREAM, urlParameters);

        urlParameters = "{\n"
            + "  \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Celsius\",\n"
            + "    \"symbol\": \"degC\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
            + "  },\n"
            + "  \"name\": \"test datastream.\",\n"
            + "  \"description\": \"test datastream.\",\n"
            + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "   \"ObservedProperty\": {\n"
            + "        \"name\": \"Luminous Flux\",\n"
            +
            "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
            +
            "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of " +
            "light.\"\n"
            + "   },\n"
            + "   \"Sensor\": {        \n"
            + "        \"name\": \"Acme Fluxomatic 1000\",\n"
            + "        \"description\": \"Acme Fluxomatic 1000\",\n"
            + "        \"encodingType\": \"application/pdf\",\n"
            + "        \"metadata\": \"Light flux sensor\"\n"
            + "   },\n"
            + "      \"Observations\": [\n"
            + "        {\n"
            + "          \"phenomenonTime\": \"2015-03-01T00:10:00Z\",\n"
            + "          \"result\": 10\n"
            + "        }\n"
            + "      ]"
            + "}";
        postInvalidEntity(EntityType.DATASTREAM, urlParameters);

        entityTypesToCheck.clear();
        entityTypesToCheck.add(EntityType.DATASTREAM);
        entityTypesToCheck.add(EntityType.SENSOR);
        entityTypesToCheck.add(EntityType.OBSERVATION);
        entityTypesToCheck.add(EntityType.FEATURE_OF_INTEREST);
        entityTypesToCheck.add(EntityType.OBSERVED_PROPERTY);
        checkNotExisting(entityTypesToCheck);

        /* Observation */
        urlParameters = "{\n"
            + "  \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Celsius\",\n"
            + "    \"symbol\": \"degC\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
            + "  },\n"
            + "  \"name\": \"test datastream.\",\n"
            + "  \"description\": \"test datastream.\",\n"
            + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
            + "   \"ObservedProperty\": {\n"
            + "        \"name\": \"Luminous Flux\",\n"
            +
            "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
            +
            "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of " +
            "light.\"\n"
            + "   },\n"
            + "   \"Sensor\": {        \n"
            + "        \"name\": \"Acme Fluxomatic 1000\",\n"
            + "        \"description\": \"Acme Fluxomatic 1000\",\n"
            + "        \"encodingType\": \"application/pdf\",\n"
            + "        \"metadata\": \"Light flux sensor\"\n"
            + "   }\n"
            + "}";
        String datastreamId = postEntity(EntityType.DATASTREAM, urlParameters).get(idKey).asText();

        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
            + "  \"result\": 100,\n"
            + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
            + "}";
        postInvalidEntity(EntityType.OBSERVATION, urlParameters);

        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
            + "  \"result\": 100,\n"
            + "  \"FeatureOfInterest\": {\n"
            + "  \t\"name\": \"A weather station.\",\n"
            + "  \t\"description\": \"A weather station.\",\n"
            + "    \"feature\": {\n"
            + "      \"type\": \"Point\",\n"
            + "      \"coordinates\": [\n"
            + "        -114.05,\n"
            + "        51.05\n"
            + "      ]\n"
            + "    }\n"
            + "  },\n"
            + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
            + "}";
        postInvalidEntity(EntityType.OBSERVATION, urlParameters);

        entityTypesToCheck.clear();
        entityTypesToCheck.add(EntityType.OBSERVATION);
        entityTypesToCheck.add(EntityType.FEATURE_OF_INTEREST);
        checkNotExisting(entityTypesToCheck);
    }

    @Test()
    @Order(4)
    public void createInvalidEntities() throws Exception {
        // Create necessary structures
        /* Thing */
        String urlParameters = "{"
            + "\"name\":\"Test Thing\","
            + "\"description\":\"This is a Test Thing From TestNG\""
            + "}";
        JsonNode entity = postEntity(EntityType.THING, urlParameters);
        String thingId = entity.get(idKey).asText();

        /* Sensor */
        urlParameters = "{\n"
            + "  \"name\": \"Fuguro Barometer\",\n"
            + "  \"description\": \"Fuguro Barometer\",\n"
            + "  \"encodingType\": \"application/pdf\",\n"
            + "  \"metadata\": \"Barometer\"\n"
            + "}";
        entity = postEntity(EntityType.SENSOR, urlParameters);
        String sensorId = entity.get(idKey).asText();

        /* ObservedProperty */
        urlParameters = "{\n"
            + "  \"name\": \"DewPoint Temperature\",\n"
            + "  \"definition\": \"http://dbpedia.org/page/Dew_point\",\n"
            +
            "  \"description\": \"The dewpoint temperature is the temperature to which the air must be cooled, at" +
            " constant pressure, for dew to form. As the grass and other objects near the ground cool to the " +
            "dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.\"\n"
            + "}";
        entity = postEntity(EntityType.OBSERVED_PROPERTY, urlParameters);
        String obsPropId = entity.get(idKey).asText();

        /* Datastream */
        // Without Sensor
        urlParameters = "{\n"
            + "  \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Celsius\",\n"
            + "    \"symbol\": \"degC\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
            + "  },\n"
            + "  \"name\": \"test datastream.\",\n"
            + "  \"description\": \"test datastream.\",\n"
            + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
            + "  \"ObservedProperty\":{ \"@iot.id\":" + escape(obsPropId) + "}\n"
            + "}";
        postInvalidEntity(EntityType.DATASTREAM, urlParameters);

        //Without ObservedProperty
        urlParameters = "{\n"
            + "  \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Celsius\",\n"
            + "    \"symbol\": \"degC\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
            + "  },\n"
            + "  \"name\": \"test datastream.\",\n"
            + "  \"description\": \"test datastream.\",\n"
            + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "  \"Thing\": { \"@iot.id\": " + escape(thingId) + " },\n"
            + "  \"Sensor\": { \"@iot.id\": " + escape(sensorId) + " }\n"
            + "}";
        postInvalidEntity(EntityType.DATASTREAM, urlParameters);
        //Without Things
        urlParameters = "{\n"
            + "  \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Celsius\",\n"
            + "    \"symbol\": \"degC\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
            + "  },\n"
            + "  \"name\": \"test datastream.\",\n"
            + "  \"description\": \"test datastream.\",\n"
            + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "  \"ObservedProperty\":{ \"@iot.id\":" + escape(obsPropId) + "},\n"
            + "  \"Sensor\": { \"@iot.id\": " + escape(sensorId) + " }\n"
            + "}";
        postInvalidEntity(EntityType.DATASTREAM, urlParameters);

        /* Observation */
        //Create Thing and Datastream
        urlParameters = "{"
            + "\"name\":\"This is a Test Thing From TestNG\","
            + "\"description\":\"This is a Test Thing From TestNG\""
            + "}";
        entity = postEntity(EntityType.THING, urlParameters);
        String thingId2 = entity.get(idKey).asText();
        urlParameters = "{\n"
            + "  \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Celsius\",\n"
            + "    \"symbol\": \"degC\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
            + "  },\n"
            + "  \"name\": \"test datastream.\",\n"
            + "  \"description\": \"test datastream.\",\n"
            + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
            + "  \"Thing\": { \"@iot.id\": " + escape(thingId2) + " },\n"
            + "  \"ObservedProperty\":{ \"@iot.id\":" + escape(obsPropId) + "},\n"
            + "  \"Sensor\": { \"@iot.id\": " + escape(sensorId) + " }\n"
            + "}";

        entity = postEntity(EntityType.DATASTREAM, urlParameters);
        String datastreamId = entity.get(idKey).asText();

        /* FeatureOfInterest */
        urlParameters = "{\n"
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
        entity = postEntity(EntityType.FEATURE_OF_INTEREST, urlParameters);
        String foiId = entity.get(idKey).asText();

        //Without Datastream
        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
            + "  \"result\": 8,\n"
            + "  \"FeatureOfInterest\": {\"@iot.id\": " + escape(foiId) + "}  \n"
            + "}";
        postInvalidEntity(EntityType.OBSERVATION, urlParameters);
        //Without FOI and without Thing's Location
        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-03-01T00:00:00.000Z\",\n"
            + "  \"result\": 100,\n"
            + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "}\n"
            + "}";
        postInvalidEntity(EntityType.OBSERVATION, urlParameters);
    }

    /**
     * This method is testing partial update or PATCH. The response should be
     * 200 and only the properties in the PATCH body should be updated, and the
     * rest must be unchanged.
     */
    @Test()
    @Order(5)
    public void patchEntities() throws Exception {
        // Create Entities

        /* Thing */
        String urlParameters = "{"
            + "\"name\":\"Test Thing\","
            + "\"description\":\"This is a Test Thing From TestNG\""
            + "}";
        JsonNode thingEntity = postEntity(EntityType.THING, urlParameters);
        String thingId = thingEntity.get(idKey).asText();

        /* Location */
        urlParameters = "{\n"
            + "  \"name\": \"bow river\",\n"
            + "  \"description\": \"bow river\",\n"
            + "  \"encodingType\": \"application/vnd.geo+json\",\n"
            + "  \"location\": { \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }\n"
            + "}";
        JsonNode locationEntity = postEntity(EntityType.LOCATION, urlParameters);
        String locationId = locationEntity.get(idKey).asText();

        /* Sensor */
        urlParameters = "{\n"
            + "  \"name\": \"Fuguro Barometer\",\n"
            + "  \"description\": \"Fuguro Barometer\",\n"
            + "  \"encodingType\": \"application/pdf\",\n"
            + "  \"metadata\": \"Barometer\"\n"
            + "}";
        JsonNode sensorEntity = postEntity(EntityType.SENSOR, urlParameters);
        String sensorId = sensorEntity.get(idKey).asText();

        /* ObservedProperty */
        urlParameters = "{\n"
            + "  \"name\": \"DewPoint Temperature\",\n"
            + "  \"definition\": \"http://dbpedia.org/page/Dew_point\",\n"
            +
            "  \"description\": \"The dewpoint temperature is the temperature to which the air must be cooled, at" +
            " constant pressure, for dew to form. As the grass and other objects near the ground cool to the " +
            "dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.\"\n"
            + "}";
        JsonNode obsPropEntity = postEntity(EntityType.OBSERVED_PROPERTY, urlParameters);
        String obsPropId = obsPropEntity.get(idKey).asText();

        /* HistoricalLocation */
        urlParameters = "{\n"
            + "  \"time\": \"2015-03-01T00:40:00.000Z\",\n"
            + "  \"Thing\":{\"@iot.id\": " + escape(thingId) + "},\n"
            + "  \"Locations\": [{\"@iot.id\": " + escape(locationId) + "}]  \n"
            + "}";
        JsonNode histLocEntity = postEntity(EntityType.HISTORICAL_LOCATION, urlParameters);
        String histLocId = histLocEntity.get(idKey).asText();

        /* FeatureOfInterest */
        urlParameters = "{\n"
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
        JsonNode foiEntity = postEntity(EntityType.FEATURE_OF_INTEREST, urlParameters);
        String foiId = foiEntity.get(idKey).asText();

        /* Datastream */
        urlParameters = "{\n"
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
        JsonNode datastreamEntity = postEntity(EntityType.DATASTREAM, urlParameters);
        String datastreamId = datastreamEntity.get(idKey).asText();

        /* Observation */
        urlParameters = "{\n"
            + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
            + "  \"result\": 8,\n"
            + "  \"Datastream\":{\"@iot.id\": " + escape(datastreamId) + "},\n"
            + "  \"FeatureOfInterest\": {\"@iot.id\": " + escape(foiId) + "}  \n"
            + "}";
        JsonNode obsEntity = postEntity(EntityType.OBSERVATION, urlParameters);
        String obsId = obsEntity.get(idKey).asText();

        // Patch and check Entities

        /* Thing */
        Map<String, String> diffs = new HashMap<>();
        urlParameters = "{\"description\":\"This is a PATCHED Test Thing From TestNG\"}";
        diffs.put("description", "This is a PATCHED Test Thing From TestNG");
        JsonNode updatedEntity = patchEntity(EntityType.THING, urlParameters, thingId);
        checkPatch(EntityType.THING, thingEntity, updatedEntity, diffs);

        urlParameters = "{\"properties\":{\"testKey\":\"testValue\"}}";
        diffs.put("properties", "{\"testKey\":\"testValue\"}");
        updatedEntity = patchEntity(EntityType.THING, urlParameters, thingId);
        checkPatch(EntityType.THING, thingEntity, updatedEntity, diffs);

        /* Location */
        diffs = new HashMap<>();
        urlParameters = "{\"location\": { \"type\": \"Point\", \"coordinates\": [114.05, -50] }}";
        diffs.put("location", "{ \"type\": \"Point\", \"coordinates\": [114.05, -50] }}");
        updatedEntity = patchEntity(EntityType.LOCATION, urlParameters, locationId);
        checkPatch(EntityType.LOCATION, locationEntity, updatedEntity, diffs);

        urlParameters = "{\"properties\":{\"testKey\":\"testValue\"}}";
        diffs.put("properties", "{\"testKey\":\"testValue\"}");
        updatedEntity = patchEntity(EntityType.LOCATION, urlParameters, locationId);
        checkPatch(EntityType.LOCATION, locationEntity, updatedEntity, diffs);

        /* HistoricalLocation */
        urlParameters = "{\"time\": \"2015-07-01T00:00:00Z\"}";
        diffs = new HashMap<>();
        diffs.put("time", "2015-07-01T00:00:00Z");
        updatedEntity = patchEntity(EntityType.HISTORICAL_LOCATION, urlParameters, histLocId);
        checkPatch(EntityType.HISTORICAL_LOCATION, histLocEntity, updatedEntity, diffs);

        /* Sensor */
        diffs = new HashMap<>();
        urlParameters = "{\"metadata\": \"PATCHED\"}";
        diffs.put("metadata", "PATCHED");
        updatedEntity = patchEntity(EntityType.SENSOR, urlParameters, sensorId);
        checkPatch(EntityType.SENSOR, sensorEntity, updatedEntity, diffs);

        urlParameters = "{\"properties\":{\"testKey\":\"testValue\"}}";
        diffs.put("properties", "{\"testKey\":\"testValue\"}");
        updatedEntity = patchEntity(EntityType.SENSOR, urlParameters, sensorId);
        checkPatch(EntityType.SENSOR, sensorEntity, updatedEntity, diffs);

        /* ObserverdProperty */
        urlParameters = "{\"description\":\"PATCHED\"}";
        diffs = new HashMap<>();
        diffs.put("description", "PATCHED");
        updatedEntity = patchEntity(EntityType.OBSERVED_PROPERTY, urlParameters, obsPropId);
        checkPatch(EntityType.OBSERVED_PROPERTY, obsPropEntity, updatedEntity, diffs);

        urlParameters = "{\"properties\":{\"testKey\":\"testValue\"}}";
        diffs.put("properties", "{\"testKey\":\"testValue\"}");
        updatedEntity = patchEntity(EntityType.OBSERVED_PROPERTY, urlParameters, obsPropId);
        checkPatch(EntityType.OBSERVED_PROPERTY, obsPropEntity, updatedEntity, diffs);

        /* FeatureOfInterest */
        urlParameters = "{\"feature\":{ \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }}";
        diffs = new HashMap<>();
        diffs.put("feature", "{ \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }");
        updatedEntity = patchEntity(EntityType.FEATURE_OF_INTEREST, urlParameters, foiId);
        checkPatch(EntityType.FEATURE_OF_INTEREST, foiEntity, updatedEntity, diffs);

        urlParameters = "{\"properties\":{\"testKey\":\"testValue\"}}";
        diffs.put("properties", "{\"testKey\":\"testValue\"}");
        updatedEntity = patchEntity(EntityType.FEATURE_OF_INTEREST, urlParameters, foiId);
        checkPatch(EntityType.FEATURE_OF_INTEREST, foiEntity, updatedEntity, diffs);

        /* Datastream */
        urlParameters = "{\"description\": \"Patched Description\"}";
        diffs = new HashMap<>();
        diffs.put("description", "Patched Description");
        // We have added an Observation so phenomenonTime has changed since dataStreamEntity was fetched.
        // So we have to refetch again before patching
        datastreamEntity = getEntity(EntityType.DATASTREAM, datastreamEntity.get(idKey).asText());
        updatedEntity = patchEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
        checkPatch(EntityType.DATASTREAM, datastreamEntity, updatedEntity, diffs);

        //Second PATCH for UOM
        JsonNode patchedDatastream = updatedEntity;
        urlParameters = "{ \"unitOfMeasurement\": {\n"
            + "    \"name\": \"Entropy2\",\n"
            + "    \"symbol\": \"S2\",\n"
            + "    \"definition\": \"http://qudt.org/vocab/unit#Entropy2\"\n"
            + "  } }";
        diffs = new HashMap<>();
        diffs.put("unitOfMeasurement",
                  "{\"name\": \"Entropy2\",\"symbol\": \"S2\",\"definition\": \"http://qudt" +
                      ".org/vocab/unit#Entropy2\"}");
        updatedEntity = patchEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
        checkPatch(EntityType.DATASTREAM, patchedDatastream, updatedEntity, diffs);

        urlParameters = "{\"properties\":{\"testKey\":\"testValue\"}}";
        diffs.put("properties", "{\"testKey\":\"testValue\"}");
        updatedEntity = patchEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
        checkPatch(EntityType.DATASTREAM, patchedDatastream, updatedEntity, diffs);

        /* Observation */
        urlParameters = "{\"phenomenonTime\": \"2015-07-01T00:40:00.000Z\"}";
        diffs = new HashMap<>();
        diffs.put("phenomenonTime", "2015-07-01T00:40:00.000Z");
        updatedEntity = patchEntity(EntityType.OBSERVATION, urlParameters, obsId);
        checkPatch(EntityType.OBSERVATION, obsEntity, updatedEntity, diffs);

        urlParameters = "{\"parameters\":{\"testKey\":\"testValue\"}}";
        diffs.put("parameters", "{\"testKey\":\"testValue\"}");
        updatedEntity = patchEntity(EntityType.OBSERVATION, urlParameters, obsId);
        checkPatch(EntityType.OBSERVATION, obsEntity, updatedEntity, diffs);
    }

    /**
     * This method is testing DELETE and its integrity constraint. The response
     * should be 200. After DELETE the GET request to that entity should return
     * 404.
     */
    @Test()
    public void deleteEntities() throws Exception {
        Map<EntityType, String[]> entitiesForDelete = createEntitiesForDelete();
        deleteEntity(EntityType.THING, entitiesForDelete.get(EntityType.THING)[0], false);
        Set<EntityType> entityTypes = new HashSet<>();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        checkExisting(entityTypes);

        //Datastream
        entitiesForDelete = createEntitiesForDelete();
        deleteEntity(EntityType.DATASTREAM, entitiesForDelete.get(EntityType.DATASTREAM)[0], false);
        entityTypes.clear();
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        checkExisting(entityTypes);

        //Location
        entitiesForDelete = createEntitiesForDelete();
        deleteEntity(EntityType.LOCATION, entitiesForDelete.get(EntityType.LOCATION)[0], false);
        entityTypes.clear();
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.OBSERVATION);
        checkExisting(entityTypes);

        //HistoricalLoation
        entitiesForDelete = createEntitiesForDelete();
        deleteEntity(EntityType.HISTORICAL_LOCATION, entitiesForDelete.get(EntityType.HISTORICAL_LOCATION)[0], false);
        entityTypes.clear();
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.OBSERVATION);
        entityTypes.add(EntityType.LOCATION);
        checkExisting(entityTypes);

        //Sensor
        entitiesForDelete = createEntitiesForDelete();
        deleteEntity(EntityType.SENSOR, entitiesForDelete.get(EntityType.SENSOR)[0], false);
        entityTypes.clear();
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        checkExisting(entityTypes);

        //ObservedProperty
        entitiesForDelete = createEntitiesForDelete();
        deleteEntity(EntityType.OBSERVED_PROPERTY, entitiesForDelete.get(EntityType.OBSERVED_PROPERTY)[0], false);
        entityTypes.clear();
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        checkExisting(entityTypes);

        //FeatureOfInterest
        entitiesForDelete = createEntitiesForDelete();
        deleteEntity(EntityType.FEATURE_OF_INTEREST, entitiesForDelete.get(EntityType.FEATURE_OF_INTEREST)[0], false);
        entityTypes.clear();
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        entityTypes.add(EntityType.DATASTREAM);
        checkExisting(entityTypes);

        //Observation
        entitiesForDelete = createEntitiesForDelete();
        deleteEntity(EntityType.OBSERVATION, entitiesForDelete.get(EntityType.OBSERVATION)[0], false);
        entityTypes.clear();
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        entityTypes.add(EntityType.LOCATION);
        checkExisting(entityTypes);
    }

    /**
     * This method is testing invalid partial update or PATCH. The PATCH request
     * is invalid if the body contains related entities as inline content.
     */
    @Test
    public void invalidPatchEntities() throws Exception {
        /**
         * Thing *
         */
        /* Thing */
        String urlParameters = "{"
            + "\"name\":\"Test Thing\","
            + "\"description\":\"This is a Test Thing From TestNG\""
            + "}";
        JsonNode thingEntity = postEntity(EntityType.THING, urlParameters);
        String thingId = thingEntity.get(idKey).asText();

        urlParameters = "{\"Locations\": [\n"
            + "    {\n"
            + "      \"name\": \"West Roof\",\n"
            + "      \"description\": \"West Roof\",\n"
            + "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117.05, 51.05] },\n"
            + "      \"encodingType\": \"application/vnd.geo+json\"\n"
            + "    }\n"
            + "  ]}";
        patchInvalidEntity(EntityType.THING, urlParameters, thingId);
        urlParameters = "{\"Datastreams\": [\n"
            + "    {\n"
            + "      \"unitOfMeasurement\": {\n"
            + "        \"name\": \"Lumen\",\n"
            + "        \"symbol\": \"lm\",\n"
            + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
            + "      }}]}";
        patchInvalidEntity(EntityType.THING, urlParameters, thingId);

        //        /** Location **/
        //        long locationId = locationIds.get(0);
        //        urlParameters = "{\"Things\":[{\"description\":\"Orange\"}]}";
        //        invalidPatchEntity(EntityType.LOCATION, urlParameters, locationId);
        //
        //        /** HistoricalLocation **/
        //        long histLocId = historicalLocationIds.get(0);
        //        urlParameters = "{\"time\": \"2015-07-01T00:00:00.000Z\"}";
        //        invalidPatchEntity(EntityType.HISTORICAL_LOCATION, urlParameters, histLocId);
        //
        /**
         * Sensor *
         */
        urlParameters = "{\n"
            + "  \"name\": \"Fuguro Barometer\",\n"
            + "  \"description\": \"Fuguro Barometer\",\n"
            + "  \"encodingType\": \"application/pdf\",\n"
            + "  \"metadata\": \"Barometer\"\n"
            + "}";
        JsonNode sensorEntity = postEntity(EntityType.SENSOR, urlParameters);
        String sensorId = sensorEntity.get(idKey).asText();

        urlParameters = "{\"Datastreams\": [\n"
            + "    {\n"
            + "      \"unitOfMeasurement\": {\n"
            + "        \"name\": \"Lumen\",\n"
            + "        \"symbol\": \"lm\",\n"
            + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"}\n"
            + "        ,\"Thing\":{\"@iot.id\":" + escape(thingId) + "}"
            + "      }]}";
        patchInvalidEntity(EntityType.SENSOR, urlParameters, sensorId);

        /**
         * ObserverdProperty *
         */
        urlParameters = "{\n"
            + "  \"name\": \"DewPoint Temperature\",\n"
            + "  \"definition\": \"http://dbpedia.org/page/Dew_point\",\n"
            +
            "  \"description\": \"The dewpoint temperature is the temperature to which the air must be cooled, at" +
            " constant pressure, for dew to form. As the grass and other objects near the ground cool to the " +
            "dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.\"\n"
            + "}";
        JsonNode obsPropEntity = postEntity(EntityType.OBSERVED_PROPERTY, urlParameters);
        String obsPropId = obsPropEntity.get(idKey).asText();
        urlParameters = "{\"Datastreams\": [\n"
            + "    {\n"
            + "      \"unitOfMeasurement\": {\n"
            + "        \"name\": \"Lumen\",\n"
            + "        \"symbol\": \"lm\",\n"
            + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"}\n"
            + "        ,\"Thing\":{\"@iot.id\":" + escape(thingId) + "}"
            + "      }]}";
        patchInvalidEntity(EntityType.OBSERVED_PROPERTY, urlParameters, obsPropId);

        //        /** FeatureOfInterest **/
        //        long foiId = foiIds.get(0);
        //        urlParameters = "{\"feature\":{ \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }}";
        //        invalidPatchEntity(EntityType.FEATURE_OF_INTEREST, urlParameters, foiId);
        /**
         * Datastream *
         */
        /* Datastream */
        urlParameters = "{\n"
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
        JsonNode datastreamEntity = postEntity(EntityType.DATASTREAM, urlParameters);
        String datastreamId = datastreamEntity.get(idKey).asText();
        urlParameters = "{\"ObservedProperty\": {\n"
            + "  \t\"name\": \"Count\",\n"
            + "\t\"definition\": \"http://qudt.org/vocab/unit#Dimensionless\",\n"
            + "\t\"name\": \"Count is a dimensionless property.\",\n"
            + "\t\"description\": \"Count is a dimensionless property.\"\n"
            + "  } }";
        patchInvalidEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
        urlParameters = "{\"Sensor\": {\n"
            + "  \t\"name\": \"Acme Traffic 2000\",  \n"
            + "  \t\"description\": \"Acme Traffic 2000\",  \n"
            + "  \t\"encodingType\": \"application/pdf\",\n"
            + "  \t\"metadata\": \"Traffic counting device\"\n"
            + "  }}";
        patchInvalidEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
        urlParameters = "{"
            + "\"Thing\": {"
            + "  \"name\": \"test\","
            + "  \"description\": \"test\""
            + " }"
            + "}";
        patchInvalidEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
        urlParameters = "{\"Observations\": [\n"
            + "    {\n"
            + "      \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
            + "      \"result\": 92122,\n"
            + "      \"resultQuality\": \"High\"\n"
            + "    }\n"
            + "  ]}";
        patchInvalidEntity(EntityType.DATASTREAM, urlParameters, datastreamId);

        //        /** Observation **/
        //        long obsId1 = observationIds.get(0);
        //        urlParameters = "{\"phenomenonTime\": \"2015-07-01T00:40:00.000Z\"}";
        //        invalidPatchEntity(EntityType.OBSERVATION, urlParameters, obsId1);
    }

    /**
     * This method is testing DELETE request for a nonexistent entity. The
     * response should be 404.
     **/
    @Test
    public void deleteNonexistentEntities() throws Exception {
        deleteNonexistentEntity(EntityType.THING);
        deleteNonexistentEntity(EntityType.LOCATION);
        deleteNonexistentEntity(EntityType.HISTORICAL_LOCATION);
        deleteNonexistentEntity(EntityType.SENSOR);
        deleteNonexistentEntity(EntityType.OBSERVED_PROPERTY);
        deleteNonexistentEntity(EntityType.DATASTREAM);
        deleteNonexistentEntity(EntityType.OBSERVATION);
        deleteNonexistentEntity(EntityType.FEATURE_OF_INTEREST);
    }

    /**
     * Create entities as a pre-process for testing DELETE.
     */
    private Map<EntityType, String[]> createEntitiesForDelete() throws Exception {
        deleteEverythings();

        //First Thing
        String urlParameters = "{\n"
            + "    \"name\": \"thing 1\",\n"
            + "    \"description\": \"thing 1\",\n"
            + "    \"properties\": {\n"
            + "        \"reference\": \"first\"\n"
            + "    },\n"
            + "    \"Locations\": [\n"
            + "        {\n"
            + "            \"name\": \"location 1\",\n"
            + "            \"description\": \"location 1\",\n"
            + "            \"location\": {\n"
            + "                \"type\": \"Point\",\n"
            + "                \"coordinates\": [\n"
            + "                    -117.05,\n"
            + "                    51.05\n"
            + "                ]\n"
            + "            },\n"
            + "            \"encodingType\": \"application/vnd.geo+json\"\n"
            + "        }\n"
            + "    ],\n"
            + "    \"Datastreams\": [\n"
            + "        {\n"
            + "            \"unitOfMeasurement\": {\n"
            + "                \"name\": \"Lumen\",\n"
            + "                \"symbol\": \"lm\",\n"
            + "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
            + "            },\n"
            + "            \"name\": \"datastream 1\",\n"
            + "            \"description\": \"datastream 1\",\n"
            +
            "            \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2" +
            ".0/OM_Measurement\",\n"
            + "            \"ObservedProperty\": {\n"
            + "                \"name\": \"Luminous Flux\",\n"
            +
            "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances" +
            ".html#LuminousFlux\",\n"
            + "                \"description\": \"observedProperty 1\"\n"
            + "            },\n"
            + "            \"Sensor\": {\n"
            + "                \"name\": \"sensor 1\",\n"
            + "                \"description\": \"sensor 1\",\n"
            + "                \"encodingType\": \"application/pdf\",\n"
            + "                \"metadata\": \"Light flux sensor\"\n"
            + "            },\n"
            + "            \"Observations\": [{\n"
            + "                  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
            + "                  \"result\": 1 \n"
            + "             }]"
            + "        }\n"
            + "    ]\n"
            + "}";
        postEntity(EntityType.THING, urlParameters);

        HashMap<EntityType, String[]> map = new HashMap<>();
        for (EntityType type : EntityType.values()) {
            List<String> ids = new ArrayList<>();
            getCollection(type).get("value").forEach(elem -> {
                ids.add(elem.get(idKey).asText());
            });
            map.put(type, ids.toArray(new String[] {}));
        }
        return map;
    }

    /**
     * Check the FeatureOfInterest is created automatically correctly if not
     * inserted in Observation
     *
     * @param obsId         The observation id
     * @param locationObj   The Location object that the FOI is supposed to be
     *                      created based on that
     * @param expectedFOIId The id of the FOI linked to the Observation
     * @return The id of FOI
     */
    private String checkAutomaticInsertionOfFOI(String obsId, JsonNode locationObj, String expectedFOIId)
        throws Exception {
        String urlString = "Observations(" + obsId + ")/FeatureOfInterest";
        JsonNode result = getEntity(urlString);
        String id = result.get(idKey).asText();
        if (expectedFOIId != null) {
            Assertions.assertEquals(
                id,
                expectedFOIId,
                "ERROR: the Observation should have linked to FeatureOfInterest with ID: "
                    + expectedFOIId
                    + " , but it is linked for FeatureOfInterest with Id: "
                    + id
                    + "."
            );
        }
        Assertions.assertEquals(
            locationObj.get("location").toString(),
            result.get("feature").toString(),
            "ERROR: Automatic created FeatureOfInterest does not match last Location of that Thing."
        );
        return id;
    }

    /**
     * Check the related entity of a given entity
     *
     * @param sourceType The given entity type
     * @param sourceId   The given entity id
     * @param targetType The relation entity type
     * @param reference  The expected related entity object
     * @return The id of related object
     */
    private String checkRelatedEntity(EntityType sourceType,
                                      String sourceId,
                                      EntityType targetType,
                                      JsonNode reference) throws Exception {

        String url = String.format(getRelatedEntityEndpoint(sourceType, targetType), sourceId);
        JsonNode result = getEntity(url);
        if (result.has("value")) {
            result = result.get("value").get(0);
        }

        if (logger.isTraceEnabled()) {
            System.out.println("checkRelatedEntity:");
            System.out.println("Actual:");
            System.out.println(result.toPrettyString());

            System.out.println("Reference:");
            System.out.println(reference.toPrettyString());
        }

        compareJsonNodes(reference, result);
        return result.get(idKey).asText();
    }

    /**
     * Check the patched entity properties are updates correctly
     *
     * @param entityType Entity type in from EntityType enum
     * @param oldEntity  The old properties of the patched entity
     * @param newEntity  The updated properties of the patched entity
     * @param diffs      The properties that supposed to be updated based on the
     *                   request due to the specification
     */
    private void checkPatch(EntityType entityType, JsonNode oldEntity, JsonNode newEntity, Map<String, String> diffs) {
        // Join diffMap and oldProperties as we might patch on previously omitted fields
        Set<String> fields = new HashSet<>();
        oldEntity.fieldNames().forEachRemaining(fields::add);
        diffs.forEach((k,v) -> fields.add(k));
        fields.forEach(field -> {
            Assertions.assertNotNull(newEntity.get(field), "missing field in actual Entity: " + field);
            if (diffs.containsKey(field)) {
                if (newEntity.get(field).isTextual()) {
                    Assertions.assertEquals(
                        diffs.get(field),
                        newEntity.get(field).asText(),
                        "PATCH was not applied correctly for " + entityType + "'s " + field + "."
                    );
                } else {
                    // Ignore crs field
                    if (newEntity.get(field).isObject() && newEntity.get(field).has("crs")) {
                        ((ObjectNode) newEntity.get(field)).remove("crs");
                    }
                    try {
                        Assertions.assertTrue(
                            newEntity.get(field).equals(mapper.readTree(diffs.get(field))),
                            "PATCH was not applied correctly for " + entityType + "'s " + field + "."
                        );
                    } catch (JsonProcessingException e) {
                        Assertions.assertTrue(false, "Error parsing test value!");
                    }
                }
            } else {
                if (Objects.equals(field, "result")) {
                    Assertions.assertTrue(
                        oldEntity.get(field).asDouble() == (newEntity.get(field).asDouble()),
                        "PATCH was not applied correctly for " + entityType + "'s " + field + ".");
                } else {
                    Assertions.assertNotNull(oldEntity.get(field), "missing field in reference Entity: " + field);
                    Assertions.assertTrue(
                        oldEntity.get(field).equals(newEntity.get(field)),
                        "PATCH was not applied correctly for " + entityType + "'s " + field + ".");
                }
            }
        });
    }
}
