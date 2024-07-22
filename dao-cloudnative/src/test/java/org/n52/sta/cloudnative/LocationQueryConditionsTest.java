/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.cloudnative;

import org.jooq.DSLContext;
import org.jooq.Condition;

import org.jooq.Field;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;

import org.n52.sta.cloudnative.condition.LocationQueryConditions;
import org.n52.sta.cloudnative.condition.EntityQueryConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnativedao")
public class LocationQueryConditionsTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    LocationQueryConditions locationQueryConditions;

    @BeforeEach
    public void setUp() {
        locationQueryConditions = new LocationQueryConditions();
        locationQueryConditions.setDslContext(ctx);
    }

    @Test
    public void testWithHistoricalLocationStaIdentifier() {
        String staIdentifier = "historicalLocation123";

        Condition result = locationQueryConditions.withHistoricalLocationStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select 1 one " +
                "from " +
                "location_historical_location " +
                "join historical_location " +
                "on " +
                "location_historical_location.fk_historical_location_id = historical_location.historical_location_id " +
                "join location " +
                "on location_historical_location.fk_location_id = location.location_id " +
                "where historical_location.sta_identifier = 'historicalLocation123')";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithThingStaIdentifier() {
        String thingStaIdentifier = "thing123";

        Condition result = locationQueryConditions.withThingStaIdentifier(thingStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select 1 one " +
                "from location " +
                "join platform_location " +
                "on location.location_id = platform_location.fk_location_id " +
                "join platform " +
                "on platform_location.fk_platform_id = platform.platform_id " +
                "where platform.sta_identifier = 'thing123')";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STEquals() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_EQUALS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Equals(ST_GeomFromWKB(geom), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STDisjoint() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_DISJOINT;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Disjoint(ST_GeomFromWKB(geom), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STWithin() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_WITHIN;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Within(ST_GeomFromWKB(geom), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STTouches() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_TOUCHES;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Touches(ST_GeomFromWKB(geom), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STOverlaps() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_OVERLAPS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Overlaps(ST_GeomFromWKB(geom), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STCrosses() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_CROSSES;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Crosses(ST_GeomFromWKB(geom), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STIntersects() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_INTERSECTS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Intersects(ST_GeomFromWKB(geom), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STContains() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_CONTAINS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Contains(ST_GeomFromWKB(geom), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithHandleGeospatial_DistanceExpr() {
        GeoValueExpr expr = new GeoValueExpr("geography'LINESTRING(1 2, 3 4)'");
        String functionName = ODataConstants.GeoFunctions.GEO_DISTANCE;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Field<Double> result = locationQueryConditions.handleGeospatial(expr, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Distance(" +
                "ST_GeomFromText('LINESTRING(1 2, 3 4)'), " +
                "ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)')" +
                ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithHandleGeospatial_DistanceField() {
        GeoValueExpr expr = new GeoValueExpr(StaConstants.PROP_LOCATION);
        String functionName = ODataConstants.GeoFunctions.GEO_DISTANCE;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Field<Double> result = locationQueryConditions.handleGeospatial(expr, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Distance(" +
                "ST_GeomFromWKB(geom), " +
                "ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)')" +
                ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithHandleGeospatial_LengthExpr() {
        GeoValueExpr expr = new GeoValueExpr("geography'LINESTRING(1 2, 3 4)'");
        String functionName = ODataConstants.GeoFunctions.GEO_LENGTH;

        Field<Double> result = locationQueryConditions.handleGeospatial(expr, functionName, null);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Length(ST_GeomFromText('LINESTRING(1 2, 3 4)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithHandleGeospatial_LengthField() {
        GeoValueExpr expr = new GeoValueExpr(StaConstants.PROP_LOCATION);
        String functionName = ODataConstants.GeoFunctions.GEO_LENGTH;

        Field<Double> result = locationQueryConditions.handleGeospatial(expr, functionName, null);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Length(ST_GeomFromWKB(geom))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Things() {
        String propertyName = EntityQueryConstants.THINGS;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = locationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "location_id in " +
                "(select fk_location_id " +
                "from platform_location " +
                "where " +
                "fk_platform_id in " +
                "(select platform_id " +
                "from platform " +
                "where " + propertyValue + "))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_HistoricalLocations() {
        String propertyName = EntityQueryConstants.HISTORICAL_LOCATIONS;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = locationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "location_id in " +
                "(select fk_location_id " +
                "from location_historical_location " +
                "where " +
                "fk_historical_location_id in " +
                "(select historical_location_id " +
                "from historical_location " +
                "where " + propertyValue + "))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("location123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = locationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "sta_identifier = cast('location123' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterName() {
        String propertyName = StaConstants.PROP_NAME;
        Field<String> propertyValue = DSL.val("location_of_interest");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = locationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "name = cast('location_of_interest' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDescription() {
        String propertyName = StaConstants.PROP_DESCRIPTION;
        Field<String> propertyValue = DSL.val("Location of the platform");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = locationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "description = cast('Location of the platform' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterEncodingType() {
        String propertyName = StaConstants.PROP_ENCODINGTYPE;
        Field<String> propertyValue = DSL.val("Geo+JSON");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = locationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "location_id in " +
                "(select location.location_id " +
                "from location " +
                "join format " +
                "on location.fk_format_id = format.format_id " +
                "where format.definition = cast('Geo+JSON' as varchar))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDefault() {
        String propertyName = "properties/lcTag";
        Field<String> propertyValue = DSL.val("xzw.1223");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = locationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "location_id in " +
                "(select fk_location_id " +
                "from location_parameter " +
                "where " +
                "(name = 'lcTag' and value_text = cast('xzw.1223' as varchar)))";

        Assertions.assertEquals(expectedSQL, sql);
    }

}
