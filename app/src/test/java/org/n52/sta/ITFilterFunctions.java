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

import java.io.IOException;

/**
 * Test filtering using filter functions.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(OrderAnnotation.class)
public class ITFilterFunctions extends ConformanceTests implements TestUtil {

    public ITFilterFunctions(@Value("${server.rootUrl}") String rootUrl) {
        super(rootUrl);
    }

    private void createMeasurementHarness() throws IOException {
        // Create required test harness
        // Requires POST with deep insert to work.
        postEntity(EntityType.THING, "{ \"description\": \"Thing description 1\", \"name\": \"thing name 1\", " +
            "\"properties\": { " +
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
            "\"Tempreture sensor\" }, \"Observations\":[ { \"phenomenonTime\": \"2015-03-05T04:05:06Z\", " +
            "\"result\": 5.8 }, { \"phenomenonTime\": \"2015-03-06T01:02:03Z\", \"result\": 6 } ] } ] }");
    }

    @Test
    @Order(1)
    public void testSubstringOf() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.THING, "$filter=substringof('ing', name)");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.THING, "$filter=substringof('invalid', name)");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(2)
    public void testEndsWith() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.THING, "$filter=endswith(name, '1')");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.THING, "$filter=endswith(name, 'i')");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(3)
    public void testStartsWith() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.THING, "$filter=startswith(name, 'thing')");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.THING, "$filter=startswith(name, 'i')");
        assertEmptyResponse(collection);
    }

    //@Test
//    @Order(4)
    public void testLength() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.THING, "$filter=length(description) eq 13");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.THING, "$filter=length(description) ne 13");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(5)
    public void testIndexOf() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.THING, "$filter=indexof(description, 'description') eq 7");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.THING, "$filter=indexof(description, 'description') ne 7");
        assertEmptyResponse(collection);
    }

    //@Test
//    @Order(6)
    public void testSubstring() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.THING, "$filter=substring(description, 1) eq 'hing description 1'");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.THING, "$filter=substring(description, 1) ne 'hing description 1'");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(7)
    public void testToLower() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.THING, "$filter=tolower(description) eq 'thing description 1'");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.THING, "$filter=tolower(description) ne 'thing description 1'");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(8)
    public void testToUpper() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.THING, "$filter=toupper(description) eq 'THING DESCRIPTION 1'");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.THING, "$filter=toupper(description) ne 'THING DESCRIPTION 1'");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(9)
    public void testTrim() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.THING, "$filter=trim(description) eq 'Thing description 1'");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.THING, "$filter=trim(description) ne 'Thing description 1'");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(10)
    public void testConcat() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.THING, "$filter=concat(name, name) eq 'thing name 1thing name 1'");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.THING, "$filter=concat(name, name) ne 'thing name 1thing name 1'");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(11)
    public void testYear() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=year(phenomenonTime) eq 2015");
        assertResponseCount(collection, 4);

        collection = getCollection(EntityType.OBSERVATION, "$filter=year(phenomenonTime) ne 2015");
        assertEmptyResponse(collection);
    }

    //@Test
//    @Order(12)
    public void testMonth() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=month(phenomenonTime) eq 03");
        assertResponseCount(collection, 4);

        collection = getCollection(EntityType.OBSERVATION, "$filter=month(phenomenonTime) ne 03");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(13)
    public void testDay() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=day(phenomenonTime) eq 05");
        assertResponseCount(collection, 1);
        collection = getCollection(EntityType.OBSERVATION, "$filter=day(phenomenonTime) eq 06");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.OBSERVATION, "$filter=day(phenomenonTime) eq 09");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(14)
    public void testHour() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=hour(phenomenonTime) eq 1");
        assertResponseCount(collection, 1);
        collection = getCollection(EntityType.OBSERVATION, "$filter=hour(phenomenonTime) eq 4");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.OBSERVATION, "$filter=hour(phenomenonTime) eq 3");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(15)
    public void testMinute() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=minute(phenomenonTime) eq 2");
        assertResponseCount(collection, 1);
        collection = getCollection(EntityType.OBSERVATION, "$filter=minute(phenomenonTime) eq 5");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.OBSERVATION, "$filter=minute(phenomenonTime) eq 8");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(16)
    public void testSecond() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=second(phenomenonTime) eq 3");
        assertResponseCount(collection, 1);
        collection = getCollection(EntityType.OBSERVATION, "$filter=second(phenomenonTime) eq 6");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.OBSERVATION, "$filter=second(phenomenonTime) eq 8");
        assertEmptyResponse(collection);
    }

    //@Test
//    @Order(17)
    public void testFractionalSeconds() throws IOException {
        Assertions.fail("TODO: implement!");
    }

    //@Test
//    @Order(18)
    public void testDate() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=date(phenomenonTime) eq date(phenomenonTime)");
        assertResponseCount(collection, 2);

        collection = getCollection(EntityType.OBSERVATION, "$filter=date(phenomenonTime) eq date(resultTime)");
        assertEmptyResponse(collection);
        collection = getCollection(EntityType.OBSERVATION, "$filter=date(phenomenonTime) ne date(phenomenonTime)");
        assertEmptyResponse(collection);
    }

    //@Test
//    @Order(19)
    public void testTime() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=time(phenomenonTime) eq time(phenomenonTime)");
        assertResponseCount(collection, 2);
        collection = getCollection(EntityType.OBSERVATION, "$filter=time(phenomenonTime) le date(phenomenonTime)");
        assertResponseCount(collection, 2);

        collection = getCollection(EntityType.OBSERVATION, "$filter=time(phenomenonTime) ne date(phenomenonTime)");
        assertEmptyResponse(collection);
    }

    //@Test
//    @Order(20)
    public void testTotalOffsetMinutes() throws IOException {
        Assertions.fail("TODO: implement!");
    }

    //@Test
//    @Order(21)
    public void testNow() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=phenomenonTime lt now()");
        assertResponseCount(collection, 4);

        collection = getCollection(EntityType.OBSERVATION, "$filter=phenomenonTime gt now()");
        assertEmptyResponse(collection);
    }

    @Test
    @Order(22)
    public void testMinDateTime() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=phenomenonTime gt mindatetime()");
        assertResponseCount(collection, 4);

        collection = getCollection(EntityType.OBSERVATION, "$filter=phenomenonTime eq mindatetime()");
        assertEmptyResponse(collection);
    }

    //@Test
//    @Order(23)
    public void testMaxDateTime() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        collection = getCollection(EntityType.OBSERVATION, "$filter=phenomenonTime lt maxdatetime()");
        assertResponseCount(collection, 4);

        collection = getCollection(EntityType.OBSERVATION, "$filter=phenomenonTime eq maxdatetime()");
        assertEmptyResponse(collection);
    }

    //@Test
//    @Order(24)
    public void testRound() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        // 5.8 and 6 both round to 6
        collection = getCollection(EntityType.OBSERVATION, "$filter=round(result) eq 6");
        assertResponseCount(collection, 2);

        collection = getCollection(EntityType.OBSERVATION, "$filter=round(result) eq 5");
        assertEmptyResponse(collection);
    }

    //@Test
//    @Order(25)
    public void testFloor() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        // 5.8 floors down to 5. 6 floors to 6
        collection = getCollection(EntityType.OBSERVATION, "$filter=floor(result) eq 5");
        assertResponseCount(collection, 1);

        collection = getCollection(EntityType.OBSERVATION, "$filter=floor(result) eq 4");
        assertEmptyResponse(collection);
    }

    //@Test
//    @Order(26)
    public void testCeiling() throws IOException {
        createMeasurementHarness();
        JsonNode collection;
        // 5.8 ceilings up to 6. 6 ceilings to 6
        collection = getCollection(EntityType.OBSERVATION, "$filter=ceiling(result) eq 6");
        assertResponseCount(collection, 2);

        collection = getCollection(EntityType.OBSERVATION, "$filter=ceiling(result) eq 5.8");
        assertEmptyResponse(collection);
    }
}
