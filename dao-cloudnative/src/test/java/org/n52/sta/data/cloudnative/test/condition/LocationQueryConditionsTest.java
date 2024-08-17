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

package org.n52.sta.data.cloudnative.test.condition;

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
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.n52.sta.data.cloudnative.condition.LocationQueryConditions;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
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

    private String read_parquet(String table) {
        return String.format("read_parquet('s3://52n-sta/%s') %s ", table, table);
    }

    @Test
    public void testWithHistoricalLocationStaIdentifier() {
        String staIdentifier = "HISTORICALLOCATION123";

        Condition result = locationQueryConditions.withHistoricalLocationStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select 1 one " +
                "from " +
                read_parquet("location_historical_location".toUpperCase()) +
                "join " +
                read_parquet("historical_location".toUpperCase()) +
                "on " +
                "location_historical_location.fk_historical_location_id = historical_location.historical_location_id ".toUpperCase() +
                "join " +
                read_parquet("location".toUpperCase()) +
                "on " +
                "location_historical_location.fk_location_id = location.location_id ".toUpperCase() +
                "where " +
                "historical_location.sta_identifier = 'historicalLocation123')".toUpperCase();

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithThingStaIdentifier() {
        String thingStaIdentifier = "THING123";

        Condition result = locationQueryConditions.withThingStaIdentifier(thingStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select 1 one " +
                "from " +
                read_parquet("location".toUpperCase()) +
                "join " +
                read_parquet("platform_location".toUpperCase()) +
                "on " +
                "platform_location.fk_location_id = location.location_id ".toUpperCase() +
                "join " +
                read_parquet("platform".toUpperCase()) +
                "on " +
                "platform_location.fk_platform_id = platform.platform_id ".toUpperCase() +
                "where " +
                "platform.sta_identifier = 'thing123')".toUpperCase();

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STEquals() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_EQUALS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Equals(ST_GeomFromWKB(LOCATION.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STDisjoint() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_DISJOINT;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Disjoint(ST_GeomFromWKB(LOCATION.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STWithin() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_WITHIN;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Within(ST_GeomFromWKB(LOCATION.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STTouches() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_TOUCHES;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Touches(ST_GeomFromWKB(LOCATION.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STOverlaps() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_OVERLAPS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Overlaps(ST_GeomFromWKB(LOCATION.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STCrosses() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_CROSSES;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Crosses(ST_GeomFromWKB(LOCATION.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STIntersects() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_INTERSECTS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Intersects(ST_GeomFromWKB(LOCATION.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STContains() {
        String propertyName = StaConstants.PROP_LOCATION;
        String functionName = ODataConstants.SpatialFunctions.ST_CONTAINS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = locationQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Contains(ST_GeomFromWKB(LOCATION.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

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
                "ST_GeomFromWKB(LOCATION.GEOM), " +
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
        String expectedSQL = "ST_Length(ST_GeomFromWKB(LOCATION.GEOM))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Things() {
        String propertyName = STAEntityDefinition.THINGS;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = locationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "LOCATION.LOCATION_ID in " +
                "(select PLATFORM_LOCATION.FK_LOCATION_ID " +
                "from " +
                read_parquet("PLATFORM_LOCATION") +
                "where " +
                "PLATFORM_LOCATION.FK_PLATFORM_ID in " +
                "(select PLATFORM.PLATFORM_ID " +
                "from " +
                read_parquet("PLATFORM") +
                "where " + propertyValue + "))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_HistoricalLocations() {
        String propertyName = STAEntityDefinition.HISTORICAL_LOCATIONS;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = locationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "LOCATION.LOCATION_ID in " +
                "(select LOCATION_HISTORICAL_LOCATION.FK_LOCATION_ID " +
                "from " +
                read_parquet("location_historical_location".toUpperCase()) +
                "where " +
                "LOCATION_HISTORICAL_LOCATION.FK_HISTORICAL_LOCATION_ID in " +
                "(select HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID " +
                "from " +
                read_parquet("historical_location".toUpperCase()) +
                "where " + propertyValue + "))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("LOCATION123");
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
        String expectedSQL = String.format("LOCATION.STA_IDENTIFIER = '%s'", propertyValue.getName());

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
        String expectedSQL = String.format("LOCATION.NAME = '%s'", propertyValue.getName());

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
        String expectedSQL = "LOCATION.DESCRIPTION = 'Location of the platform'";

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
        String expectedSQL = "LOCATION.LOCATION_ID in " +
                "(select LOCATION.LOCATION_ID " +
                "from " +
                read_parquet("LOCATION") +
                "join " +
                read_parquet("FORMAT") +
                "on " +
                "location.fk_format_id = format.format_id ".toUpperCase() +
                "where FORMAT.DEFINITION = 'Geo+JSON')";

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
        String expectedSQL = "LOCATION.LOCATION_ID in " +
                "(select LOCATION_PARAMETER.FK_LOCATION_ID " +
                "from " +
                read_parquet("LOCATION_PARAMETER") +
                "where " +
                "(LOCATION_PARAMETER.NAME = 'lcTag' and LOCATION_PARAMETER.VALUE_TEXT = 'xzw.1223'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

}
