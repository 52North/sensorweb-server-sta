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

package org.n52.sta.data.cloudnative;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;

import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.cloudnative.condition.StaEntity;
import org.n52.sta.data.cloudnative.condition.ThingQueryConditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class ThingQueryConditionsTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired private DSLContext ctx;
    private ThingQueryConditions thingQueryConditions;

    @BeforeEach
    public void setUp() {
        thingQueryConditions = new ThingQueryConditions();
        thingQueryConditions.setDslContext(ctx);
    }

    private String read_parquet(String table) {
        return String.format("read_parquet('s3://52n-sta/%s') %s ", table, table);
    }

    @Test
    public void testWithLocationStaIdentifier_TP() {
        final String staIdentifier = "location123";

        Condition result = thingQueryConditions.withLocationStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select LOCATION.LOCATION_ID " +
                "from " +
                read_parquet("PLATFORM_LOCATION") +
                "join " +
                read_parquet("PLATFORM") +
                "on PLATFORM_LOCATION.FK_PLATFORM_ID = PLATFORM.PLATFORM_ID " +
                "join " +
                read_parquet("LOCATION") +
                "on PLATFORM_LOCATION.FK_LOCATION_ID = LOCATION.LOCATION_ID " +
                "where LOCATION.STA_IDENTIFIER = 'location123')";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithHistoricalLocationStaIdentifier_TP() {
        final String staIdentifier = "historicalLocation123";

        Condition result = thingQueryConditions.withHistoricalLocationStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID " +
                "from " +
                read_parquet("PLATFORM") +
                "join " +
                read_parquet("HISTORICAL_LOCATION") +
                "on HISTORICAL_LOCATION.FK_PLATFORM_ID = PLATFORM.PLATFORM_ID " +
                "where HISTORICAL_LOCATION.STA_IDENTIFIER = 'historicalLocation123')";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDatastreamStaIdentifier_TP() {
        final String staIdentifier = "datastream123";

        Condition result = thingQueryConditions.withDatastreamStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select DATASET.DATASET_ID " +
                "from " +
                read_parquet("PLATFORM") +
                "join " +
                read_parquet("DATASET") +
                "on DATASET.FK_PLATFORM_ID = PLATFORM.PLATFORM_ID " +
                "where DATASET.STA_IDENTIFIER = 'datastream123')";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Datastream_TP() {
        final String propertyName = STAEntityDefinition.DATASTREAMS;
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);

        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "PLATFORM.PLATFORM_ID in " +
                "(select " +
                "DATASET.FK_PLATFORM_ID " +
                "from " +
                read_parquet("DATASET") +
                "where " + conditionSQL + ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Datastream_FP() {
        final String propertyName = STAEntityDefinition.DATASTREAMS;
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);

        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);

        // This expected SQL is intentionally incorrect to create a false positive test case
        String expectedSQL = "PLATFORM.PLATFORM_ID in " +
                "(select " +
                "DATASET.FK_PLATFORM_ID " +
                "from " +
                read_parquet("DATASET") +
                "where " + conditionSQL + " AND some_nonexistent_condition)";

        Assertions.assertNotEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Location_TP() {
        final String propertyName = STAEntityDefinition.LOCATIONS;
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);

        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);

        String expectedSQL = "PLATFORM.PLATFORM_ID in " +
                "(select " +
                "PLATFORM_LOCATION.FK_PLATFORM_ID " +
                "from " +
                read_parquet("PLATFORM_LOCATION") +
                "join " +
                read_parquet("LOCATION") +
                "on PLATFORM_LOCATION.FK_LOCATION_ID = LOCATION.LOCATION_ID " +
                "where " + conditionSQL + ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Location_FP() {
        final String propertyName = STAEntityDefinition.LOCATIONS;
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);

        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);

        // This expected SQL is intentionally incorrect to create a false positive test case
        String expectedSQL = "PLATFORM.PLATFORM_ID in " +
                "(select " +
                "PLATFORM_LOCATION.FK_PLATFORM_ID " +
                "from " +
                read_parquet("PLATFORM_LOCATION") +
                "join " +
                read_parquet("LOCATION") +
                "on PLATFORM_LOCATION.FK_LOCATION_ID = LOCATION.LOCATION_ID " +
                "where " + conditionSQL + " AND some_nonexistent_condition)";

        Assertions.assertNotEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_HistoricalLocation_TP() {
        final String propertyName = STAEntityDefinition.HISTORICAL_LOCATIONS;
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);

        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);

        String expectedSQL = "PLATFORM.PLATFORM_ID in " +
                "(select " +
                "HISTORICAL_LOCATION.FK_PLATFORM_ID " +
                "from " +
                read_parquet("HISTORICAL_LOCATION") +
                "join " +
                read_parquet("PLATFORM") +
                "on HISTORICAL_LOCATION.FK_PLATFORM_ID = PLATFORM.PLATFORM_ID " +
                "where " + conditionSQL + ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_HistoricalLocation_FP() {
        final String propertyName = STAEntityDefinition.HISTORICAL_LOCATIONS;
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);

        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);

        // This expected SQL is intentionally incorrect to create a false positive test case
        String expectedSQL = "PLATFORM.PLATFORM_ID in " +
                "(select " +
                "HISTORICAL_LOCATION.FK_PLATFORM_ID " +
                "from " +
                read_parquet("HISTORICAL_LOCATION") +
                "join " +
                read_parquet("PLATFORM") +
                "on HISTORICAL_LOCATION.FK_PLATFORM_ID = PLATFORM.PLATFORM_ID " +
                "where " + conditionSQL + " AND some_nonexistent_condition)";

        Assertions.assertNotEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_TN() {
        // Unrelated entity
        final String propertyName = STAEntityDefinition.FEATURE_OF_INTEREST;
        Condition propertyValue = DSL.condition("");

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
            thingQueryConditions.getFilterForRelation(propertyName, propertyValue);
        });

        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: " +
                "Could not find related property: " +
                propertyName,
                e.getMessage());
    }

    @Test
    public void testWithDirectPropertyFilterId_TP() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("thing123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsNotEqualTo;
        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "PLATFORM.STA_IDENTIFIER <> 'thing123'";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId_FP() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("thing123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsGreaterThan;
        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "PLATFORM.STA_IDENTIFIER >= 'thing123'";

        Assertions.assertNotEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId_TN() {
        String propertyName = StaConstants.PROP_ID;
        Field<Integer> propertyValue = DSL.val(Integer.valueOf(123));
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsLessThan;

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
                    thingQueryConditions.getFilterForProperty(propertyName, propertyValue, operator, false);
        });
        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: "
                        + "Invalid Datatypes found. Cannot cast "
                        + propertyValue.getDataType().getType()
                        + " to String.class",
                e.getMessage());
    }

    @Test
    public void testWithDirectPropertyFilterName_TP() {
        String propertyName = StaConstants.PROP_NAME;
        Field<String> propertyValue = DSL.val("Site.203.182");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsGreaterThan;
        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "PLATFORM.NAME > 'Site.203.182'";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterName_TN() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val(String.valueOf("Site.203.182"));
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsLike;

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
                    thingQueryConditions.getFilterForProperty(propertyName, propertyValue, operator, false);
                }
        );
        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: " +
                "Operator \"PropertyIsLike\" is not supported for given arguments.",
                e.getMessage());
    }

    @Test
    public void testWithDirectPropertyFilterDescription_TP() {
        String propertyName = StaConstants.PROP_DESCRIPTION;
        Field<String> propertyValue = DSL.val("site to measure composite environmental pollutants");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "PLATFORM.DESCRIPTION = 'site to measure composite environmental pollutants'";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDescription_TN() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("site to measure composite environmental pollutants");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsNull;

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
                    thingQueryConditions.getFilterForProperty(propertyName, propertyValue, operator, false);
                }
        );
        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: " +
                        "Operator \"PropertyIsNull\" is not supported for given arguments.",
                e.getMessage());
    }

    @Test
    public void testWithDirectPropertyFilterDefault_TP() {
        String propertyName = "properties/site_depth";
        Field<String> propertyValue = DSL.val(String.valueOf("-23m"));
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsLessThanOrEqualTo;
        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "PLATFORM.PLATFORM_ID in " +
                "(select PLATFORM_PARAMETER.FK_PLATFORM_ID " +
                "from " +
                read_parquet("PLATFORM_PARAMETER") +
                "where " +
                "(PLATFORM_PARAMETER.NAME = 'site_depth' and PLATFORM_PARAMETER.VALUE_TEXT <= '-23m'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDefault_TN() {
        String propertyName = "properties/site_depth";
        Field<Integer> propertyValue = DSL.val(Integer.valueOf(-23));
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsLessThanOrEqualTo;

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
                    thingQueryConditions.getFilterForProperty(propertyName, propertyValue, operator, false);
                }
        );
        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: " +
                        String.format("Error getting filter for Property: '%s'. No such property with type %s in Entity.",
                                "site_depth",
                                "String"),
                e.getMessage());
    }

}
