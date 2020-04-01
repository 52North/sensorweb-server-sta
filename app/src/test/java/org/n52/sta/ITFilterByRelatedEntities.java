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

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Test filtering by related Entities. For now only checks that no error is returned.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ITFilterByRelatedEntities extends ConformanceTests implements TestUtil {

    ITFilterByRelatedEntities(@Value("${server.rootUrl}") String rootUrl) {
        super(rootUrl);
    }

    @Test
    public void testFilterOnSingleNavigationDoesNotThrowError() throws IOException {

        getCollection(EntityType.THING, URLEncoder.encode("$filter=Datastreams/id eq '52N'"));
        getCollection(EntityType.THING, URLEncoder.encode("$filter=Locations/id eq '52N'"));
        getCollection(EntityType.THING, URLEncoder.encode("$filter=HistoricalLocations/id eq '52N'"));
        getCollection(EntityType.LOCATION, URLEncoder.encode("$filter=Things/id eq '52N'"));
        getCollection(EntityType.LOCATION, URLEncoder.encode("$filter=HistoricalLocations/id eq '52N'"));
        getCollection(EntityType.HISTORICAL_LOCATION, URLEncoder.encode("$filter=Thing/id eq '52N'"));
        getCollection(EntityType.HISTORICAL_LOCATION, URLEncoder.encode("$filter=Locations/id eq '52N'"));
        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=Observations/id eq '52N'"));
        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=ObservedProperty/id eq '52N'"));
        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=Thing/id eq '52N'"));
        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=Sensor/id eq '52N'"));
        getCollection(EntityType.SENSOR, URLEncoder.encode("$filter=Datastreams/id eq '52N'"));
        getCollection(EntityType.OBSERVATION, URLEncoder.encode("$filter=Datastream/id eq '52N'"));
        getCollection(EntityType.OBSERVATION, URLEncoder.encode("$filter=FeatureOfInterest/id eq '52N'"));
        getCollection(EntityType.OBSERVED_PROPERTY, URLEncoder.encode("$filter=Datastreams/id eq '52N'"));
        getCollection(EntityType.FEATURE_OF_INTEREST, URLEncoder.encode("$filter=Observations/id eq '52N'"));
    }

    @Test
    public void testFilterOnDoubleNavigationDoesNotThrowError() throws IOException {

        getCollection(EntityType.THING, URLEncoder.encode("$filter=Datastreams/Observations/id eq '52N'"));
        getCollection(EntityType.THING, URLEncoder.encode("$filter=Datastreams/ObservedProperty/id eq '52N'"));
        getCollection(EntityType.THING, URLEncoder.encode("$filter=Datastreams/Thing/id eq '52N'"));
        getCollection(EntityType.THING, URLEncoder.encode("$filter=Datastreams/Sensor/id eq '52N'"));
        getCollection(EntityType.THING, URLEncoder.encode("$filter=Locations/Things/id eq '52N'"));
        getCollection(EntityType.THING, URLEncoder.encode("$filter=Locations/HistoricalLocations/id eq '52N'"));
        getCollection(EntityType.THING, URLEncoder.encode("$filter=HistoricalLocations/Thing/id eq '52N'"));
        getCollection(EntityType.THING, URLEncoder.encode("$filter=HistoricalLocations/Locations/id eq '52N'"));

        getCollection(EntityType.LOCATION, URLEncoder.encode("$filter=Things/Datastreams/id eq '52N'"));
        getCollection(EntityType.LOCATION, URLEncoder.encode("$filter=Things/Locations/id eq '52N'"));
        getCollection(EntityType.LOCATION, URLEncoder.encode("$filter=HistoricalLocations/Thing/id eq '52N'"));
        getCollection(EntityType.LOCATION, URLEncoder.encode("$filter=HistoricalLocations/Locations/id eq '52N'"));

        getCollection(EntityType.HISTORICAL_LOCATION, URLEncoder.encode("$filter=Thing/Datastreams/id eq '52N'"));
        getCollection(EntityType.HISTORICAL_LOCATION, URLEncoder.encode("$filter=Thing/Locations/id eq '52N'"));
        getCollection(EntityType.HISTORICAL_LOCATION, URLEncoder.encode("$filter=Thing/HistoricalLocations/id eq " +
                                                                                "'52N'"));
        getCollection(EntityType.HISTORICAL_LOCATION, URLEncoder.encode("$filter=Locations/Things/id eq '52N'"));
        getCollection(EntityType.HISTORICAL_LOCATION,
                      URLEncoder.encode("$filter=Locations/HistoricalLocations/id eq '52N'"));

        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=Observations/Datastream/id eq '52N'"));
        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=Observations/FeatureOfInterest/id eq '52N'"));
        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=ObservedProperty/Datastreams/id eq '52N'"));
        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=Thing/Datastreams/id eq '52N'"));
        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=Thing/Locations/id eq '52N'"));
        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=Thing/HistoricalLocations/id eq '52N'"));
        getCollection(EntityType.DATASTREAM, URLEncoder.encode("$filter=Sensor/Datastreams/id eq '52N'"));

        getCollection(EntityType.SENSOR, URLEncoder.encode("$filter=Datastreams/Sensor/id eq '52N'"));
        getCollection(EntityType.SENSOR, URLEncoder.encode("$filter=Datastreams/Thing/id eq '52N'"));
        getCollection(EntityType.SENSOR, URLEncoder.encode("$filter=Datastreams/ObservedProperty/id eq '52N'"));
        getCollection(EntityType.SENSOR, URLEncoder.encode("$filter=Datastreams/Observations/id eq '52N'"));

        getCollection(EntityType.OBSERVATION, URLEncoder.encode("$filter=Datastream/Sensor/id eq '52N'"));
        getCollection(EntityType.OBSERVATION, URLEncoder.encode("$filter=Datastream/Thing/id eq '52N'"));
        getCollection(EntityType.OBSERVATION, URLEncoder.encode("$filter=Datastream/ObservedProperty/id eq '52N'"));
        getCollection(EntityType.OBSERVATION, URLEncoder.encode("$filter=Datastream/Observations/id eq '52N'"));
        getCollection(EntityType.OBSERVATION, URLEncoder.encode("$filter=FeatureOfInterest/Observations/id eq '52N'"));

        getCollection(EntityType.OBSERVED_PROPERTY, URLEncoder.encode("$filter=Datastreams/Sensor/id eq '52N'"));
        getCollection(EntityType.OBSERVED_PROPERTY, URLEncoder.encode("$filter=Datastreams/Thing/id eq '52N'"));
        getCollection(EntityType.OBSERVED_PROPERTY,
                      URLEncoder.encode("$filter=Datastreams/ObservedProperty/id eq '52N'"));
        getCollection(EntityType.OBSERVED_PROPERTY, URLEncoder.encode("$filter=Datastreams/Observations/id eq '52N'"));

        getCollection(EntityType.FEATURE_OF_INTEREST, URLEncoder.encode("$filter=Observations/Datastream/id eq '52N'"));
        getCollection(EntityType.FEATURE_OF_INTEREST,
                      URLEncoder.encode("$filter=Observations/FeatureOfInterest/id eq '52N'"));
    }
}
