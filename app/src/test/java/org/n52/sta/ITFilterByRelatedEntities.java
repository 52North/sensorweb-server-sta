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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test filtering by related Entities. For now only checks that no error is returned.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ITFilterByRelatedEntities extends ConformanceTests implements TestUtil {

    ITFilterByRelatedEntities(@Value("${server.rootUrl}") String rootUrl) throws Exception {
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
    public void testFilterOnSingleNavigationDoesNotThrowError() throws Exception {
        getCollection(EntityType.THING, "$filter=Datastreams/id eq '52N'");
        getCollection(EntityType.THING, "$filter=Locations/id eq '52N'");
        getCollection(EntityType.THING, "$filter=HistoricalLocations/id eq '52N'");
        getCollection(EntityType.LOCATION, "$filter=Things/id eq '52N'");
        getCollection(EntityType.LOCATION, "$filter=HistoricalLocations/id eq '52N'");
        getCollection(EntityType.HISTORICAL_LOCATION, "$filter=Thing/id eq '52N'");
        getCollection(EntityType.HISTORICAL_LOCATION, "$filter=Locations/id eq '52N'");
        getCollection(EntityType.DATASTREAM, "$filter=Observations/id eq '52N'");
        getCollection(EntityType.DATASTREAM, "$filter=ObservedProperty/id eq '52N'");
        getCollection(EntityType.DATASTREAM, "$filter=Thing/id eq '52N'");
        getCollection(EntityType.DATASTREAM, "$filter=Sensor/id eq '52N'");
        getCollection(EntityType.SENSOR, "$filter=Datastreams/id eq '52N'");
        getCollection(EntityType.OBSERVATION, "$filter=Datastream/id eq '52N'");
        getCollection(EntityType.OBSERVATION, "$filter=FeatureOfInterest/id eq '52N'");
        getCollection(EntityType.OBSERVED_PROPERTY, "$filter=Datastreams/id eq '52N'");
        getCollection(EntityType.FEATURE_OF_INTEREST, "$filter=Observations/id eq '52N'");
    }

    @Test
    public void testFilterOnDoubleNavigationDoesNotThrowError() throws Exception {

        getCollection(EntityType.THING, "$filter=Datastreams/Observations/id eq '52N'");
        getCollection(EntityType.THING, "$filter=Datastreams/ObservedProperty/id eq '52N'");
        getCollection(EntityType.THING, "$filter=Datastreams/Thing/id eq '52N'");
        getCollection(EntityType.THING, "$filter=Datastreams/Sensor/id eq '52N'");
        getCollection(EntityType.THING, "$filter=Locations/Things/id eq '52N'");
        getCollection(EntityType.THING, "$filter=Locations/HistoricalLocations/id eq '52N'");
        getCollection(EntityType.THING, "$filter=HistoricalLocations/Thing/id eq '52N'");
        getCollection(EntityType.THING, "$filter=HistoricalLocations/Locations/id eq '52N'");

        getCollection(EntityType.LOCATION, "$filter=Things/Datastreams/id eq '52N'");
        getCollection(EntityType.LOCATION, "$filter=Things/Locations/id eq '52N'");
        getCollection(EntityType.LOCATION, "$filter=HistoricalLocations/Thing/id eq '52N'");
        getCollection(EntityType.LOCATION, "$filter=HistoricalLocations/Locations/id eq '52N'");

        getCollection(EntityType.HISTORICAL_LOCATION, "$filter=Thing/Datastreams/id eq '52N'");
        getCollection(EntityType.HISTORICAL_LOCATION, "$filter=Thing/Locations/id eq '52N'");
        getCollection(EntityType.HISTORICAL_LOCATION, "$filter=Thing/HistoricalLocations/id eq " +
                "'52N'");
        getCollection(EntityType.HISTORICAL_LOCATION, "$filter=Locations/Things/id eq '52N'");
        getCollection(EntityType.HISTORICAL_LOCATION,
                      "$filter=Locations/HistoricalLocations/id eq '52N'");

        getCollection(EntityType.DATASTREAM, "$filter=Observations/Datastream/id eq '52N'");
        getCollection(EntityType.DATASTREAM, "$filter=Observations/FeatureOfInterest/id eq '52N'");
        getCollection(EntityType.DATASTREAM, "$filter=ObservedProperty/Datastreams/id eq '52N'");
        getCollection(EntityType.DATASTREAM, "$filter=Thing/Datastreams/id eq '52N'");
        getCollection(EntityType.DATASTREAM, "$filter=Thing/Locations/id eq '52N'");
        getCollection(EntityType.DATASTREAM, "$filter=Thing/HistoricalLocations/id eq '52N'");
        getCollection(EntityType.DATASTREAM, "$filter=Sensor/Datastreams/id eq '52N'");

        getCollection(EntityType.SENSOR, "$filter=Datastreams/Sensor/id eq '52N'");
        getCollection(EntityType.SENSOR, "$filter=Datastreams/Thing/id eq '52N'");
        getCollection(EntityType.SENSOR, "$filter=Datastreams/ObservedProperty/id eq '52N'");
        getCollection(EntityType.SENSOR, "$filter=Datastreams/Observations/id eq '52N'");

        getCollection(EntityType.OBSERVATION, "$filter=Datastream/Sensor/id eq '52N'");
        getCollection(EntityType.OBSERVATION, "$filter=Datastream/Thing/id eq '52N'");
        getCollection(EntityType.OBSERVATION, "$filter=Datastream/ObservedProperty/id eq '52N'");
        getCollection(EntityType.OBSERVATION, "$filter=Datastream/Observations/id eq '52N'");
        getCollection(EntityType.OBSERVATION, "$filter=FeatureOfInterest/Observations/id eq '52N'");

        getCollection(EntityType.OBSERVED_PROPERTY, "$filter=Datastreams/Sensor/id eq '52N'");
        getCollection(EntityType.OBSERVED_PROPERTY, "$filter=Datastreams/Thing/id eq '52N'");
        getCollection(EntityType.OBSERVED_PROPERTY,
                      "$filter=Datastreams/ObservedProperty/id eq '52N'");
        getCollection(EntityType.OBSERVED_PROPERTY, "$filter=Datastreams/Observations/id eq '52N'");

        getCollection(EntityType.FEATURE_OF_INTEREST, "$filter=Observations/Datastream/id eq '52N'");
        getCollection(EntityType.FEATURE_OF_INTEREST,
                      "$filter=Observations/FeatureOfInterest/id eq '52N'");
    }
}
