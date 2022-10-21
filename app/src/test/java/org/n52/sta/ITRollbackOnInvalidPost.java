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

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;

/**
 * Implements Conformance Tests according to Section A.1 in OGC SensorThings API Part 1: Sensing (15-078r6)
 * Adapted from the official Test Suite <a href="https://github.com/opengeospatial/ets-sta10/">ets-sta10</a>
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
public class ITRollbackOnInvalidPost extends ConformanceTests {

    private final String invalidSensor = "{\"description\":\"This is the Lobby of the 52N HQ\",\"name\":\"52N HQ " +
        "Lobby\",\"Locations\":[{\"name\":\"Location of 52N HQ\",\"description\":\"Somewhere in the " +
        "Loddenheide\",\"encodingType\":\"application/vnd.geo+json\",\"location\":{\"type\":\"Feature\"," +
        "\"geometry\":{\"type\":\"Point\",\"coordinates\":[52,7]}}}],\"Datastreams\":[{\"name\":\"Air " +
        "Temperature\",\"description\":\"This Datastreams measures Air Temperature\"," +
        "\"unitOfMeasurement\":{\"name\":\"degree Celsius\",\"symbol\":\"°C\"," +
        "\"definition\":\"http://unitsofmeasure.org/ucum.html#para-30\"},\"observationType\":\"http://www.opengis" +
        ".net/def/observationType/OGC-OM/2.0/OM_Measurement\",\"observedArea\":{\"type\":\"Polygon\"," +
        "\"coordinates\":[[[100,0]]]},\"Observations\":[{\"result\":\"28\"," +
        "\"phenomenonTime\":\"2019-03-10T17:46:09Z\"},{\"result\":\"31\"," +
        "\"phenomenonTime\":\"2019-03-10T17:50:00Z\"}],\"phenomenonTime\":\"2009-01-11T16:22:25" +
        ".00Z/2011-08-21T08:32:10.00Z\",\"ObservedProperty\":{\"@iot.id\":\"AirTemp\",\"name\":\"Air " +
        "Temperature\",\"definition\":\"http://sweet.jpl.nasa.gov/ontology/property.owl#AirTemperature\"," +
        "\"description\":\"The air temperature is the temperature of the air.\"},\"Sensor\":{\"@iot" +
        ".id\":\"DS18B2022\",\"encodingType\":\"application/pdf\",\"metadata\":\"http://datasheets.maxim-ic" +
        ".com/en/ds/DS18B20.pdf\"}}]}";

    private final String invalidObsProp = "{\"description\":\"This is the Lobby of the 52N HQ\",\"name\":\"52N HQ " +
        "Lobby\",\"Locations\":[{\"name\":\"Location of 52N HQ\",\"description\":\"Somewhere in the " +
        "Loddenheide\",\"encodingType\":\"application/vnd.geo+json\",\"location\":{\"type\":\"Feature\"," +
        "\"geometry\":{\"type\":\"Point\",\"coordinates\":[52,7]}}}],\"Datastreams\":[{\"name\":\"Air " +
        "Temperature\",\"description\":\"This Datastreams measures Air Temperature\"," +
        "\"unitOfMeasurement\":{\"name\":\"degree Celsius\",\"symbol\":\"°C\"," +
        "\"definition\":\"http://unitsofmeasure.org/ucum.html#para-30\"},\"observationType\":\"http://www.opengis" +
        ".net/def/observationType/OGC-OM/2.0/OM_Measurement\",\"observedArea\":{\"type\":\"Polygon\"," +
        "\"coordinates\":[[[100,0]]]},\"Observations\":[{\"result\":\"28\"," +
        "\"phenomenonTime\":\"2019-03-10T17:46:09Z\"},{\"result\":\"31\"," +
        "\"phenomenonTime\":\"2019-03-10T17:50:00Z\"}],\"phenomenonTime\":\"2009-01-11T16:22:25" +
        ".00Z/2011-08-21T08:32:10.00Z\",\"ObservedProperty\":{\"@iot.id\":\"AirTemp\",\"name\":\"Air " +
        "Temperature\",\"description\":\"The air temperature is the temperature of the air.\"},\"Sensor\":{\"@iot" +
        ".id\":\"DS18B2022\",\"name\":\"sensor 1\",\"description\":\"sensor 1\"," +
        "\"encodingType\":\"application/pdf\",\"metadata\":\"http://datasheets.maxim-ic.com/en/ds/DS18B20" +
        ".pdf\"}}]}";

    private final String invalidObs = "{\"description\":\"This is the Lobby of the 52N HQ\",\"name\":\"52N HQ " +
        "Lobby\",\"Locations\":[{\"name\":\"Location of 52N HQ\",\"description\":\"Somewhere in the " +
        "Loddenheide\",\"encodingType\":\"application/vnd.geo+json\",\"location\":{\"type\":\"Feature\"," +
        "\"geometry\":{\"type\":\"Point\",\"coordinates\":[52,7]}}}],\"Datastreams\":[{\"name\":\"Air " +
        "Temperature\",\"description\":\"This Datastreams measures Air Temperature\"," +
        "\"unitOfMeasurement\":{\"name\":\"degree Celsius\",\"symbol\":\"°C\"," +
        "\"definition\":\"http://unitsofmeasure.org/ucum.html#para-30\"},\"observationType\":\"http://www.opengis" +
        ".net/def/observationType/OGC-OM/2.0/OM_Measurement\",\"observedArea\":{\"type\":\"Polygon\"," +
        "\"coordinates\":[[[100,0]]]},\"Observations\":[{},{\"result\":\"31\"," +
        "\"phenomenonTime\":\"2019-03-10T17:50:00Z\"}],\"phenomenonTime\":\"2009-01-11T16:22:25" +
        ".00Z/2011-08-21T08:32:10.00Z\",\"ObservedProperty\":{\"@iot.id\":\"AirTemp\",\"name\":\"Air " +
        "Temperature\",\"definition\":\"http://sweet.jpl.nasa.gov/ontology/property.owl#AirTemperature\"," +
        "\"description\":\"The air temperature is the temperature of the air.\"},\"Sensor\":{\"@iot" +
        ".id\":\"DS18B2022\",\"name\":\"sensor 1\",\"description\":\"sensor 1\"," +
        "\"encodingType\":\"application/pdf\",\"metadata\":\"http://datasheets.maxim-ic.com/en/ds/DS18B20" +
        ".pdf\"}}]}";

    private final String invalidDatastream = "{\"description\":\"This is the Lobby of the 52N HQ\",\"name\":\"52N HQ " +
        "Lobby\",\"Locations\":[{\"name\":\"Location of 52N HQ\",\"description\":\"Somewhere in the " +
        "Loddenheide\",\"encodingType\":\"application/vnd.geo+json\",\"location\":{\"type\":\"Feature\"," +
        "\"geometry\":{\"type\":\"Point\",\"coordinates\":[52,7]}}}],\"Datastreams\":[{\"name\":\"Air " +
        "Temperature\",\"description\":\"This Datastreams measures Air Temperature\"," +
        "\"observationType\":\"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\"," +
        "\"observedArea\":{\"type\":\"Polygon\",\"coordinates\":[[[100,0]]]}," +
        "\"Observations\":[{\"result\":\"31\",\"phenomenonTime\":\"2019-03-10T17:50:00Z\"}]," +
        "\"phenomenonTime\":\"2009-01-11T16:22:25.00Z/2011-08-21T08:32:10.00Z\",\"ObservedProperty\":{\"@iot" +
        ".id\":\"AirTemp\",\"name\":\"Air Temperature\",\"definition\":\"http://sweet.jpl.nasa" +
        ".gov/ontology/property.owl#AirTemperature\",\"description\":\"The air temperature is the temperature of " +
        "the air.\"},\"Sensor\":{\"@iot.id\":\"DS18B2022\",\"name\":\"sensor 1\",\"description\":\"sensor 1\"," +
        "\"encodingType\":\"application/pdf\",\"metadata\":\"http://datasheets.maxim-ic.com/en/ds/DS18B20" +
        ".pdf\"}}]}";

    private final String invalidLocation = "{\"description\":\"This is the Lobby of the 52N HQ\",\"name\":\"52N HQ " +
        "Lobby\",\"Locations\":[{\"name\":\"Location of 52N HQ\",\"description\":\"Somewhere in the " +
        "Loddenheide\",\"location\":{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[52," +
        "7]}}}],\"Datastreams\":[{\"name\":\"Air Temperature\",\"description\":\"This Datastreams measures Air " +
        "Temperature\",\"unitOfMeasurement\":{\"name\":\"degree Celsius\",\"symbol\":\"°C\"," +
        "\"definition\":\"http://unitsofmeasure.org/ucum.html#para-30\"},\"observationType\":\"http://www.opengis" +
        ".net/def/observationType/OGC-OM/2.0/OM_Measurement\",\"observedArea\":{\"type\":\"Polygon\"," +
        "\"coordinates\":[[[100,0]]]},\"Observations\":[{\"result\":\"31\"," +
        "\"phenomenonTime\":\"2019-03-10T17:50:00Z\"}],\"phenomenonTime\":\"2009-01-11T16:22:25" +
        ".00Z/2011-08-21T08:32:10.00Z\",\"ObservedProperty\":{\"@iot.id\":\"AirTemp\",\"name\":\"Air " +
        "Temperature\",\"definition\":\"http://sweet.jpl.nasa.gov/ontology/property.owl#AirTemperature\"," +
        "\"description\":\"The air temperature is the temperature of the air.\"},\"Sensor\":{\"@iot" +
        ".id\":\"DS18B2022\",\"name\":\"sensor 1\",\"description\":\"sensor 1\"," +
        "\"encodingType\":\"application/pdf\",\"metadata\":\"http://datasheets.maxim-ic.com/en/ds/DS18B20" +
        ".pdf\"}}]}";

    ITRollbackOnInvalidPost(@Value("${server.rootUrl}") String rootUrl) {
        super(rootUrl);
    }

    @Test
    @Order(1)
    public void checkNoEntityCreatedOnInvalidThingPost() throws Exception {
        postInvalidEntity(EntityType.THING, invalidSensor);
        checkNotExisting(Arrays.asList(EntityType.values()));

        postInvalidEntity(EntityType.THING, invalidObsProp);
        checkNotExisting(Arrays.asList(EntityType.values()));

        postInvalidEntity(EntityType.THING, invalidObs);
        checkNotExisting(Arrays.asList(EntityType.values()));

        postInvalidEntity(EntityType.THING, invalidDatastream);
        checkNotExisting(Arrays.asList(EntityType.values()));

        postInvalidEntity(EntityType.THING, invalidLocation);
        checkNotExisting(Arrays.asList(EntityType.values()));
    }
}
