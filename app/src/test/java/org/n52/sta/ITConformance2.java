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
import org.junit.jupiter.api.Test;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements Conformance Tests according to Section A.2 in OGC SensorThings API Part 1: Sensing (15-078r6)
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * @see <a href="http://docs.opengeospatial.org/is/15-078r6/15-078r6.html#54">
 * OGC SensorThings API Part 1: Sensing (15-078r6)</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ITConformance2 extends ConformanceTests implements TestUtil {

    private static final Logger logger = LoggerFactory.getLogger(ITConformance2.class);

    public ITConformance2(@Value("${server.rootUrl}") String rootUrl) throws IOException {
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

    /**
     * Prior to applying any server-driven pagination:
     * $filter
     * $count
     * $orderby
     * $skip
     * $top
     * After applying any server-driven pagination:
     * $expand
     * $select
     */
    @Test
    public void testQueryOptionOrder() {

    }

    @Test
    public void testSelect() throws Exception {
        for (String collectionName : STAEntityDefinition.ALLCOLLECTIONS) {
            STAEntityDefinition definition = STAEntityDefinition.definitions.get(collectionName);
            String prefix = "$select=";
            String filter;
            Set<String> allProps = new HashSet<>();
            allProps.addAll(definition.getEntityPropsOptional());
            allProps.addAll(definition.getEntityPropsMandatory());

            // Test single select
            for (String prop : allProps) {
                filter = prop;
                JsonNode response = getCollection(EntityType.getByVal(collectionName), prefix + filter);
                checkEntityProperties(Collections.singleton(prop), response);
            }
            // Test combined select
            filter = "";
            for (String prop : allProps) {
                filter += prop + ",";
                JsonNode response = getCollection(EntityType.getByVal(collectionName),
                                                  prefix + filter.substring(0,
                                                                            filter.length() - 1));
                checkEntityProperties(new HashSet<>(Arrays.asList(filter.split(","))), response);
            }
        }
    }

    @Test
    public void testExpand() {

    }

}
