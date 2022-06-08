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

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test filtering using spatial operators. For now only checks if no error is thrown.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ITFilterBySpatial extends ConformanceTests implements TestUtil {

    public ITFilterBySpatial(@Value("${server.config.service-root-url}") String rootUrl) {
        super(rootUrl);
    }

    private void init() throws Exception {
        postEntity(EntityType.LOCATION,
                   "{\n"
                           + "  \"name\": \"52N HQ\",\n"
                           + "  \"description\": \"test location at 52N52E\",\n"
                           + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                           + "  \"location\": { \"type\": \"Point\", \"coordinates\": [52, 52] }\n"
                           + "}");

        postEntity(EntityType.FEATURE_OF_INTEREST,
                   "{\n"
                           +
                           "    \"name\": \"MSGuenter-Trip-01\",\n"
                           +
                           "    \"description\": \"This is the first Forschungstrip of MS Guenther\",\n"
                           +
                           "    \"encodingType\": \"application/vnd.geo+json\",\n"
                           +
                           "    \"feature\": {\n"
                           +
                           "      \"type\": \"Feature\","
                           +
                           "      \"geometry\": {\n"
                           +
                           "        \"type\": \"LineString\",\n"
                           +
                           "        \"coordinates\": [\n"
                           +
                           "          [0, 0], [52, 52]\n"
                           +
                           "        ]\n"
                           +
                           "      }\n"
                           +
                           "    }\n"
                           +
                           "}");
    }

    @Test
    public void testFilterSTequals() throws Exception {
        init();
        JsonNode response = getCollection(EntityType.LOCATION,
                                          "$filter=st_equals(location, geography'POINT (30 10)')");
        assertEmptyResponse(response);

        response = getCollection(EntityType.LOCATION,
                                 "$filter=not st_equals(location, geography'POINT (30 10)')");
        assertResponseCount(response, 1);

        response = getCollection(EntityType.LOCATION,
                                 "$filter=st_equals(location, geography'POINT (52 52)')");
        assertResponseCount(response, 1);

        response = getCollection(EntityType.LOCATION,
                                 "$filter=not st_equals(location, geography'POINT (52 52)')");
        assertEmptyResponse(response);
    }

    @Test
    public void testFilterSTdisjoint() throws Exception {
        init();
        JsonNode response = getCollection(EntityType.LOCATION,
                                          "$filter=st_disjoint(location, geography'POINT (30 10)')");
        assertResponseCount(response, 1);

        response = getCollection(EntityType.LOCATION,
                                 "$filter=not st_disjoint(location, geography'POINT (30 10)')");
        assertEmptyResponse(response);

        response = getCollection(EntityType.LOCATION,
                                 "$filter=st_disjoint(location, geography'POINT (52 52)')");
        assertEmptyResponse(response);

        response = getCollection(EntityType.LOCATION,
                                 "$filter=not st_disjoint(location, geography'POINT (52 52)')");
        assertResponseCount(response, 1);
    }

    @Test
    public void testFilterSTtouches() throws Exception {
        init();
        JsonNode response = getCollection(
                                          EntityType.LOCATION,
                                          "$filter=st_touches(location, geography'LINESTRING(0 0, 52 52, 0 2)')");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.LOCATION,
                                 "$filter=not st_touches(location, geography'LINESTRING(0 0, 52 52, 0 2)')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.LOCATION,
                                 "$filter=st_touches(location, geography'LINESTRING(0 0, 40 40, 52 52)')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.LOCATION,
                                 "$filter=not st_touches(location, geography'LINESTRING(0 0, 40 40, 52 52)')");
        assertEmptyResponse(response);
    }

    @Test
    public void testFilterSTwithin() throws Exception {
        init();
        JsonNode response = getCollection(
                                          EntityType.LOCATION,
                                          "$filter=st_within(location, geography'POLYGON((0 0, 0 40, 40 40, 40 0, 0 0))')");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.LOCATION,
                                 "$filter=not st_within(location, geography'POLYGON((0 0, 0 40, 40 40, 40 0, 0 0))')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.LOCATION,
                                 "$filter=st_within(location, geography'POLYGON((0 0, 0 60, 60 60, 60 0, 0 0))')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.LOCATION,
                                 "$filter=not st_within(location, geography'POLYGON((0 0, 0 60, 60 60, 60 0, 0 0))')");
        assertEmptyResponse(response);

    }

    @Test
    public void testFilterSToverlaps() throws Exception {
        init();
        JsonNode response = getCollection(
                                          EntityType.FEATURE_OF_INTEREST,
                                          "$filter=st_overlaps(feature, geography'POLYGON((0 0, 0 40, 40 40, 40 0, 0 0))')");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=not st_overlaps(feature, geography'POLYGON((0 0, 0 40, 40 40, 40 0, 0 0))')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=st_overlaps(feature, geography'POLYGON((1 1, 1 60, 60 60, 60 1, 1 1))')");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=not st_overlaps(feature, geography'POLYGON((1 1, 1 60, 60 60, 60 1, 1 1))')");
        assertResponseCount(response, 1);
    }

    @Test
    public void testFilterSTcrosses() throws Exception {
        init();
        JsonNode response = getCollection(
                                          EntityType.FEATURE_OF_INTEREST,
                                          "$filter=st_crosses(feature, geography'LINESTRING (10 10, 11 11)')");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=not st_crosses(feature, geography'LINESTRING (10 10, 11 11)')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=st_crosses(feature, geography'LINESTRING (0 52, 52 0)')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=not st_crosses(feature, geography'LINESTRING (0 52, 52 0)')");
        assertEmptyResponse(response);
    }

    @Test
    public void testFilterSTintersects() throws Exception {
        init();
        JsonNode response = getCollection(
                                          EntityType.FEATURE_OF_INTEREST,
                                          "$filter=st_intersects(feature, geography'LINESTRING (10 10, 11 11)')");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=not st_intersects(feature, geography'LINESTRING (10 10, 11 11)')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=st_intersects(feature, geography'LINESTRING (0 52, 52 52, 1 1)')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=not st_intersects(feature, geography'LINESTRING (0 52, 52 52, 1 1)')");
        assertEmptyResponse(response);
    }

    @Test
    public void testFilterSTcontains() throws Exception {
        init();
        JsonNode response = getCollection(
                                          EntityType.LOCATION,
                                          "$filter=st_contains(location, geography'POLYGON((0 0, 0 40, 40 40, 40 0, 0 0))')");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.LOCATION,
                                 "$filter=not st_contains(location, geography'POLYGON((0 0, 0 40, 40 40, 40 0, 0 0))')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.LOCATION,
                                 "$filter=st_contains(location, geography'POLYGON((0 0, 0 60, 60 60, 60 0, 0 0))')");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.LOCATION,
                                 "$filter=not st_contains(location, geography'POLYGON((0 0, 0 60, 60 60, 60 0, 0 0))')");
        assertResponseCount(response, 1);
    }

    @Test
    public void testFilterSTrelate() throws Exception {
        init();

        // TODO: implement
        // Assertions.fail();
    }

    @Test
    public void testFilterGEOlength() throws Exception {
        init();

        JsonNode response = getCollection(
                                          EntityType.FEATURE_OF_INTEREST,
                                          "$filter=geo.length(feature) le 5");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=geo.length(feature) gt 5");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=geo.length(geography'LINESTRING (0 0, 0 10)') le 5");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=geo.length(geography'LINESTRING (0 0, 0 10)') gt 5");
        assertResponseCount(response, 1);

        /*
         * response = getCollection( EntityType.FEATURE_OF_INTEREST,
         * "$filter=geo.length(geography'LINESTRING (0 0, 0 10)') eq 1105854.83323437"));
         * assertResponseCount(response,1);
         */
    }

    @Test
    public void testFilterGEOdistance() throws Exception {
        init();
        JsonNode response;
        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=geo.distance(feature, geography'POINT(0 0)') eq 0");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=geo.distance(feature, geography'POINT(0 0)') ne 99995");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=geo.distance(geography'LINESTRING (0 0, 0 10)',"
                                         +
                                         " geography'POINT(0 0)') eq 0");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=geo.distance(geography'LINESTRING (0 0, 0 10)',"
                                         +
                                         " geography'POINT(0 0)') gt 5");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=geo.distance(geography'LINESTRING (0 0, 0 10)',"
                                         +
                                         " geography'POINT(0 0)') eq 10");
        assertEmptyResponse(response);
    }

    @Test
    public void testFilterGEOintersects() throws Exception {
        init();
        JsonNode response = getCollection(
                                          EntityType.FEATURE_OF_INTEREST,
                                          "$filter=geo.intersects(feature, geography'LINESTRING (10 10, 11 11)')");
        assertEmptyResponse(response);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=not geo.intersects(feature, geography'LINESTRING (10 10, 11 11)')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=geo.intersects(feature, geography'LINESTRING (0 52, 52 52, 1 1)')");
        assertResponseCount(response, 1);

        response = getCollection(
                                 EntityType.FEATURE_OF_INTEREST,
                                 "$filter=not geo.intersects(feature, geography'LINESTRING (0 52, 52 52, 1 1)')");
        assertEmptyResponse(response);
    }

}
