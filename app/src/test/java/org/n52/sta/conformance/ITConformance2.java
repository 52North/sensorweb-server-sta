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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Implements Conformance Tests according to Section A.2 in OGC SensorThings API Part 1: Sensing (15-078r6)
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 * @see <a href="http://docs.opengeospatial.org/is/15-078r6/15-078r6.html#54"> OGC SensorThings API Part 1:
 *      Sensing (15-078r6)</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@TestPropertySource(locations = "classpath:application-test.properties")
public class ITConformance2 extends ConformanceTests implements TestUtil {

    private static final Logger logger = LoggerFactory.getLogger(ITConformance2.class);

    public ITConformance2(@Value("${server.config.service-root-url}") String rootUrl) throws IOException {
        super(rootUrl);
    }

    /*
     * - Prior to applying any server-driven pagination: $filter $count $orderby $skip $top - After applying
     * any server-driven pagination: $expand $select
     * @Test public void testQueryOptionOrder() { }
     */

    @Test
    public void testSelect() throws Exception {
        for (String collectionName : STAEntityDefinition.CORECOLLECTIONS) {
            STAEntityDefinition definition = STAEntityDefinition.definitions.get(collectionName);
            String prefix = "$select=";
            String filter;
            Set<String> allProps = new HashSet<>();
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
                                                  prefix
                                                          + filter.substring(0,
                                                                             filter.length() - 1));
                checkEntityProperties(new HashSet<>(Arrays.asList(filter.split(","))), response);
            }
        }
    }

    @Test
    public void testExpand() {

    }

}
