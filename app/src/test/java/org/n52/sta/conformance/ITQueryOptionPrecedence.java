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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test precedence of query options. Prior to applying any server-driven pagination: 1. $filter 2. $count 3.
 * $orderby 4. $skip 5. $top After applying any server-driven pagination: 6. $expand 7. $select
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ITQueryOptionPrecedence extends ConformanceTests implements TestUtil {

    private static final Logger logger = LoggerFactory.getLogger(ITQueryOptionPrecedence.class);

    private final String countFilter = "$count=true";
    private final String orderByFilter = "$orderby=id";
    private final String selectFilter = "$select=id";
    private final String skipFilter = "$skip=0";
    private final String topFilter = "$top=1";
    private final String filterFilter = "$filter=id eq '52N'";
    private final String expandFilter = "$expand=Datastreams";

    private final String[] allFilters = {
        countFilter,
        orderByFilter,
        selectFilter,
        skipFilter,
        topFilter,
        filterFilter,
        expandFilter
    };

    public ITQueryOptionPrecedence(@Value("${server.config.service-root-url}") String rootUrl) throws Exception {
        super(rootUrl);

        // Create required test harness
        // Requires POST with deep insert to work.
        postEntity(EntityType.THING,
                   "{ \"description\": \"thing 1\", \"name\": \"thing name 1\", \"properties\": { "
                           +
                           "\"reference\": \"first\" }, \"Locations\": [ { \"description\": \"location 1\", \"name\": \"location"
                           +
                           " name 1\", \"location\": { \"type\": \"Point\", \"coordinates\": [ -117.05, 51.05 ] }, "
                           +
                           "\"encodingType\": \"application/vnd.geo+json\" } ], \"Datastreams\": [ { \"unitOfMeasurement\": { "
                           +
                           "\"name\": \"Lumen\", \"symbol\": \"lm\", \"definition\": \"http://www.qudt.org/qudt/owl/1.0"
                           +
                           ".0/unit/Instances.html/Lumen\" }, \"description\": \"datastream 1\", \"name\": \"datastream name "
                           +
                           "1\", \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\", "
                           +
                           "\"ObservedProperty\": { \"name\": \"Luminous Flux\", \"definition\": \"http://www.qudt"
                           +
                           ".org/qudt/owl/1.0.0/quantity/Instances.html/LuminousFlux\", \"description\": \"observedProperty 1\" "
                           +
                           "}, \"Sensor\": { \"description\": \"sensor 1\", \"name\": \"sensor name 1\", \"encodingType\": "
                           +
                           "\"application/pdf\", \"metadata\": \"Light flux sensor\" }, \"Observations\":[ { \"phenomenonTime\":"
                           +
                           " \"2015-03-03T00:00:00Z\", \"result\": 3 }, { \"phenomenonTime\": \"2015-03-04T00:00:00Z\", "
                           +
                           "\"result\": 4 } ] }, { \"unitOfMeasurement\": { \"name\": \"Centigrade\", \"symbol\": \"C\", "
                           +
                           "\"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen\" }, \"description\":"
                           +
                           " \"datastream 2\", \"name\": \"datastream name 2\", \"observationType\": \"http://www.opengis"
                           +
                           ".net/def/observationType/OGC-OM/2.0/OM_Measurement\", \"ObservedProperty\": { \"name\": "
                           +
                           "\"Tempretaure\", \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances"
                           +
                           ".html/Tempreture\", \"description\": \"observedProperty 2\" }, \"Sensor\": { \"description\": "
                           +
                           "\"sensor 2\", \"name\": \"sensor name 2\", \"encodingType\": \"application/pdf\", \"metadata\": "
                           +
                           "\"Tempreture sensor\" }, \"Observations\":[ { \"phenomenonTime\": \"2015-03-05T00:00:00Z\", "
                           +
                           "\"result\": 5 }, { \"phenomenonTime\": \"2015-03-06T00:00:00Z\", \"result\": 6 } ] } ] }");
    }

    /**
     * Code taken from https://stackoverflow.com/a/14444037 Licensed under MIT License as stated here:
     * https://meta.stackexchange
     * .com/questions/271080/the-mit-license-clarity-on-using-code-on-stack-overflow-and-stack-exchange
     */
    private static void testPermutations(java.util.List<Integer> arr,
                                         int k,
                                         Consumer<List<Integer>> consumer) {
        for (int i = k; i < arr.size(); i++) {
            java.util.Collections.swap(arr, i, k);
            testPermutations(arr, k + 1, consumer);
            java.util.Collections.swap(arr, k, i);
        }
        if (k == arr.size() - 1) {
            consumer.accept(arr);
        }
    }

    @Test
    public void testAllOrderingsThrowNoError() {
        testPermutations(Arrays.asList(0, 1, 2, 3, 4, 5, 6), 0, (list) -> {
            StringBuilder filters = new StringBuilder();
            for (Integer integer : list) {
                filters.append("&")
                       .append(allFilters[integer]);
            }
            logger.debug(filters.substring(1));
            try {
                getCollection(EntityType.THING, filters.substring(1));
            } catch (IOException e) {
                Assertions.fail("Caught IOException:" + e.getMessage());
            }
        });
    }
}
