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
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.cloudnative.test.TestDatabaseConfig;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;

import org.n52.sta.data.cloudnative.condition.FeatureOfInterestQueryConditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class FeatureOfInterestQueryConditionsTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    FeatureOfInterestQueryConditions featureQueryConditions;

    @BeforeEach
    public void setUp() {
        featureQueryConditions = new FeatureOfInterestQueryConditions();
        featureQueryConditions.setDslContext(ctx);
    }

    /*private String read_parquet(String table) {
        return String.format("read_parquet('s3://52n-sta/%s.parquet') %s ", table, table);
    }*/

    @Test
    public void testWithObservationStaIdentifier() {
        final String staIdentifier = "observation123";

        Condition result = featureQueryConditions.withObservationStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select OBSERVATION.OBSERVATION_ID " +
                "from " +
                "FEATURE " +
                "join " +
                "DATASET " +
                "on " +
                "dataset.fk_feature_id = feature.feature_id ".toUpperCase() +
                "join " +
                "OBSERVATION " +
                "on " +
                "observation.fk_dataset_id = dataset.dataset_id ".toUpperCase() +
                "where OBSERVATION.STA_IDENTIFIER = 'observation123')";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Observation() {
        String propertyName = STAEntityDefinition.OBSERVATIONS;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = featureQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "FEATURE.FEATURE_ID in " +
                "(select DATASET.FK_FEATURE_ID " +
                "from " +
                "DATASET " +
                "where DATASET.DATASET_ID in " +
                "(select OBSERVATION.FK_DATASET_ID " +
                "from " +
                "OBSERVATION " +
                "where " + propertyValue + "))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STEquals() {
        String propertyName = StaConstants.PROP_FEATURE;
        String functionName = ODataConstants.SpatialFunctions.ST_EQUALS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = featureQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Equals(ST_GeomFromWKB(FEATURE.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STDisjoint() {
        String propertyName = StaConstants.PROP_FEATURE;
        String functionName = ODataConstants.SpatialFunctions.ST_DISJOINT;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = featureQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Disjoint(ST_GeomFromWKB(FEATURE.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STWithin() {
        String propertyName = StaConstants.PROP_FEATURE;
        String functionName = ODataConstants.SpatialFunctions.ST_WITHIN;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = featureQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Within(ST_GeomFromWKB(FEATURE.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STTouches() {
        String propertyName = StaConstants.PROP_FEATURE;
        String functionName = ODataConstants.SpatialFunctions.ST_TOUCHES;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = featureQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Touches(ST_GeomFromWKB(FEATURE.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STOverlaps() {
        String propertyName = StaConstants.PROP_FEATURE;
        String functionName = ODataConstants.SpatialFunctions.ST_OVERLAPS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = featureQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Overlaps(ST_GeomFromWKB(FEATURE.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STCrosses() {
        String propertyName = StaConstants.PROP_FEATURE;
        String functionName = ODataConstants.SpatialFunctions.ST_CROSSES;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = featureQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Crosses(ST_GeomFromWKB(FEATURE.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STIntersects() {
        String propertyName = StaConstants.PROP_FEATURE;
        String functionName = ODataConstants.SpatialFunctions.ST_INTERSECTS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = featureQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Intersects(ST_GeomFromWKB(FEATURE.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithGeoSpatialPropertyFilter_STContains() {
        String propertyName = StaConstants.PROP_FEATURE;
        String functionName = ODataConstants.SpatialFunctions.ST_CONTAINS;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Condition result = featureQueryConditions.handleGeoSpatialPropertyFilter(propertyName, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Contains(ST_GeomFromWKB(FEATURE.GEOM), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithHandleGeospatial_DistanceExpr() {
        GeoValueExpr expr = new GeoValueExpr("geography'LINESTRING(1 2, 3 4)'");
        String functionName = ODataConstants.GeoFunctions.GEO_DISTANCE;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Field<Double> result = featureQueryConditions.handleGeospatial(expr, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Distance(" +
                "ST_GeomFromText('LINESTRING(1 2, 3 4)'), " +
                "ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)')" +
                ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithHandleGeospatial_DistanceField() {
        GeoValueExpr expr = new GeoValueExpr(StaConstants.PROP_FEATURE);
        String functionName = ODataConstants.GeoFunctions.GEO_DISTANCE;
        String argument = "geography'LINESTRING(0 0, 5 5, 10 10)'";

        Field<Double> result = featureQueryConditions.handleGeospatial(expr, functionName, argument);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Distance(" +
                "ST_GeomFromWKB(FEATURE.GEOM), " +
                "ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)')" +
                ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithHandleGeospatial_LengthExpr() {
        GeoValueExpr expr = new GeoValueExpr("geography'LINESTRING(1 2, 3 4)'");
        String functionName = ODataConstants.GeoFunctions.GEO_LENGTH;

        Field<Double> result = featureQueryConditions.handleGeospatial(expr, functionName, null);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Length(ST_GeomFromText('LINESTRING(1 2, 3 4)'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithHandleGeospatial_LengthField() {
        GeoValueExpr expr = new GeoValueExpr(StaConstants.PROP_FEATURE);
        String functionName = ODataConstants.GeoFunctions.GEO_LENGTH;

        Field<Double> result = featureQueryConditions.handleGeospatial(expr, functionName, null);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "ST_Length(ST_GeomFromWKB(FEATURE.GEOM))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("feature123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = featureQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "FEATURE.STA_IDENTIFIER = 'feature123'";

        assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterName() {
        String propertyName = StaConstants.PROP_NAME;
        Field<String> propertyValue = DSL.val("feature_of_interest");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = featureQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "FEATURE.NAME = 'feature_of_interest'";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDescription() {
        String propertyName = StaConstants.PROP_DESCRIPTION;
        Field<String> propertyValue = DSL.val("description of the feature");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = featureQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "FEATURE.DESCRIPTION = 'description of the feature'";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterEncodingType_PropertyEq() {
        String propertyName = StaConstants.PROP_ENCODINGTYPE;
        Field<String> propertyValue = DSL.val("");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = featureQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "FEATURE.FK_FORMAT_ID in " +
                "(select FEATURE.FK_FORMAT_ID " +
                "from " +
                "FEATURE " +
                "join " +
                "FORMAT " +
                "on " +
                "feature.fk_format_id = format.format_id ".toUpperCase() +
                "where " +
                "(FORMAT.DEFINITION = 'application/vnd.geo+json' " +
                "or " +
                "FORMAT.DEFINITION = 'application/vnd.geo json')" +
                ")";

        Assertions.assertEquals(expectedSQL, sql);
    }


    @Test
    public void testWithDirectPropertyFilterDefault() {
        String propertyName = "properties/fNo";
        Field<String> propertyValue = DSL.val("23.11.09");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = featureQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "FEATURE.FEATURE_ID in " +
                "(select FEATURE_PARAMETER.FK_FEATURE_ID " +
                "from " +
                "FEATURE_PARAMETER " +
                "where " +
                "(FEATURE_PARAMETER.NAME = 'fNo' and FEATURE_PARAMETER.VALUE_TEXT = '23.11.09'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

}
