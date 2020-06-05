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

/**
 * Test filtering by related Entities. For now only checks that no error is returned.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ITFilterByResult extends ConformanceTests implements TestUtil {

    ITFilterByResult(@Value("${server.rootUrl}") String rootUrl) {
        super(rootUrl);
    }

    @Test
    public void testOMMeasurementResultFiltering() throws IOException {
        createMeasurementHarness();
        JsonNode collection = getCollection(EntityType.OBSERVATION, "$filter=result ge 6");
        assertResponseCount(collection, 1);
    }

    @Test
    public void testOMCategoryResultFiltering() throws IOException {
        createCategoryHarness();
        JsonNode collection = getCollection(EntityType.OBSERVATION, "$filter=result ge 'Embryophyta'");
        assertResponseCount(collection, 1);
    }

    @Test
    public void testNoNumericResultWhenFilteringOMCategory() throws IOException {
        createCategoryHarness();
        createMeasurementHarness();
        JsonNode collection = getCollection(EntityType.OBSERVATION, "$filter=result ge 'Embryophyta'");
        assertResponseCount(collection, 1);

    }

    @Test
    public void testNoStringResultWhenFilteringOMMeasurement() throws IOException {
        createCategoryHarness();
        createMeasurementHarness();
        JsonNode collection = getCollection(EntityType.OBSERVATION, "$filter=result ge 6");
        assertResponseCount(collection, 1);
    }

    private void createMeasurementHarness() throws IOException {
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

    private void createCategoryHarness() throws IOException {
        // Create required test harness
        // Requires POST with deep insert to work.
        postEntity(EntityType.THING, "{\n" +
                "    \"@iot.id\": \"CategoryThing\",\n" +
                "    \"description\": \"This is a Forschungsschiff\",\n" +
                "    \"name\": \"MS Guenther\",\n" +
                "    \"Locations\": [\n" +
                "        {\n" +
                "            \"name\": \"Locationaa of MS Test\",\n" +
                "            \"description\": \"Somewhere in the ocean\",\n" +
                "            \"encodingType\": \"application/vnd.geo+json\",\n" +
                "            \"location\": {\n" +
                "                \"type\": \"Feature\",\n" +
                "                \"geometry\": {\n" +
                "                    \"type\": \"Point\",\n" +
                "                    \"coordinates\": [\n" +
                "                        52,\n" +
                "                        52\n" +
                "                    ]\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"Datastreams\": [\n" +
                "        {\n" +
                "            \"@iot.id\": \"Category\",\n" +
                "            \"name\": \"Air Temperature\",\n" +
                "            \"description\": \"This Datastreams measures Air Temperature\",\n" +
                "            \"unitOfMeasurement\": {\n" +
                "                \"name\": \"degree Celsius\",\n" +
                "                \"symbol\": \"°C\",\n" +
                "                \"definition\": \"http://unitsofmeasure.org/ucum.html#para-30\"\n" +
                "            },\n" +
                "            \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2" +
                ".0/OM_CategoryObservation\",\n" +
                "            \"observedArea\": {\n" +
                "                \"type\": \"Polygon\",\n" +
                "                \"coordinates\": [\n" +
                "                    [\n" +
                "                        [\n" +
                "                            100,\n" +
                "                            0\n" +
                "                        ],\n" +
                "                        [\n" +
                "                            101,\n" +
                "                            0\n" +
                "                        ],\n" +
                "                        [\n" +
                "                            101,\n" +
                "                            1\n" +
                "                        ],\n" +
                "                        [\n" +
                "                            100,\n" +
                "                            1\n" +
                "                        ],\n" +
                "                        [\n" +
                "                            100,\n" +
                "                            0\n" +
                "                        ]\n" +
                "                    ]\n" +
                "                ]\n" +
                "            },\n" +
                "            \"Observations\": [\n" +
                "                {\n" +
                "                    \"result\": \"Narcissus assoanus\",\n" +
                "                    \"phenomenonTime\": \"2019-03-10T17:46:09Z\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"result\": \"Dionaea muscipula\",\n" +
                "                    \"phenomenonTime\": \"2050-03-10T16:2:09Z\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"phenomenonTime\": \"2009-01-11T16:22:25.00Z/2011-08-21T08:32:10.00Z\",\n" +
                "            \"ObservedProperty\": {\n" +
                "                \"@iot.id\": \"AirTemp\",\n" +
                "                \"name\": \"Air Temperature\",\n" +
                "                \"definition\": \"http://sweet.jpl.nasa.gov/ontology/property.owl#AirTemperature\"," +
                "\n" +
                "                \"description\": \"The air temperature is the temperature of the air.\"\n" +
                "            },\n" +
                "            \"Sensor\": {\n" +
                "                \"@iot.id\": \"DS18B2022\",\n" +
                "                \"name\": \"Dallas DS18B2022\",\n" +
                "                \"description\": \"DS18B20 is an air temperature sensor\",\n" +
                "                \"encodingType\": \"application/pdf\",\n" +
                "                \"metadata\": \"http://datasheets.maxim-ic.com/en/ds/DS18B20.pdf\"\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}");
    }
}
