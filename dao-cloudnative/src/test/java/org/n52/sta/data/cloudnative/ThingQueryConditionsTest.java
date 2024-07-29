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

import org.n52.sta.data.cloudnative.condition.EntityQueryConstants;
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

    @Test
    public void testWithLocationStaIdentifier_TP() {
        final String staIdentifier = "location123";

        Condition result = thingQueryConditions.withLocationStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select 1 one " +
                "from " +
                "platform_location join platform " +
                "on platform.platform_id = platform_location.fk_platform_id " +
                "join location on location.location_id = platform_location.fk_location_id " +
                "where location.sta_identifier = 'location123')";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithHistoricalLocationStaIdentifier_TP() {
        final String staIdentifier = "historicalLocation123";

        Condition result = thingQueryConditions.withHistoricalLocationStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select 1 one " +
                "from " +
                "platform join historical_location " +
                "on platform.platform_id = historical_location.fk_platform_id " +
                "where historical_location.sta_identifier = 'historicalLocation123')";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDatastreamStaIdentifier_TP() {
        final String staIdentifier = "datastream123";

        Condition result = thingQueryConditions.withDatastreamStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select 1 one " +
                "from " +
                "platform join dataset " +
                "on platform.platform_id = dataset.fk_platform_id " +
                "where dataset.sta_identifier = 'datastream123')";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Datastream_TP() {
        final String propertyName = EntityQueryConstants.DATASTREAMS;
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);

        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "platform_id in " +
                "(select " +
                "dataset.fk_platform_id " +
                "from dataset " +
                "where " + conditionSQL + ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Datastream_FP() {
        final String propertyName = EntityQueryConstants.DATASTREAMS;
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
        String expectedSQL = "platform_id in " +
                "(select " +
                "dataset.fk_platform_id " +
                "from dataset " +
                "where " + conditionSQL + " AND some_nonexistent_condition)";

        Assertions.assertNotEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Location_TP() {
        final String propertyName = EntityQueryConstants.LOCATIONS;
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);

        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);

        String expectedSQL = "platform_id in " +
                "(select " +
                "platform_location.fk_platform_id " +
                "from platform_location " +
                "join location " +
                "on platform_location.fk_location_id = location.location_id " +
                "where " + conditionSQL + ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Location_FP() {
        final String propertyName = EntityQueryConstants.LOCATIONS;
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
        String expectedSQL = "platform_id in " +
                "(select " +
                "platform_location.fk_platform_id " +
                "from platform_location " +
                "join location " +
                "on platform_location.fk_location_id = location.location_id " +
                "where " + conditionSQL + " AND some_nonexistent_condition)";

        Assertions.assertNotEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_HistoricalLocation_TP() {
        final String propertyName = EntityQueryConstants.HISTORICAL_LOCATIONS;
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);

        Condition result = null;
        try {
            result = thingQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);

        String expectedSQL = "platform_id in " +
                "(select " +
                "historical_location.fk_platform_id " +
                "from historical_location " +
                "join platform " +
                "on platform.platform_id = historical_location.fk_platform_id " +
                "where " + conditionSQL + ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_HistoricalLocation_FP() {
        final String propertyName = EntityQueryConstants.HISTORICAL_LOCATIONS;
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
        String expectedSQL = "platform_id in " +
                "(select " +
                "historical_location.fk_platform_id " +
                "from historical_location " +
                "join platform " +
                "on platform.platform_id = historical_location.fk_platform_id " +
                "where " + conditionSQL + " AND some_nonexistent_condition)";

        Assertions.assertNotEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_TN() {
        // Unrelated entity
        final String propertyName = EntityQueryConstants.FEATUREOFINTEREST;
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
        String expectedSQL = "sta_identifier <> cast('thing123' as varchar)";

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
        String expectedSQL = "sta_identifier >= 'thing123'";

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
                        + EntityQueryConstants.INVALID_DATATYPE_CANNOT_CAST
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
        String expectedSQL = "name > cast('Site.203.182' as varchar)";

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
        String expectedSQL = "description = cast('site to measure composite environmental pollutants' as varchar)";

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
        String expectedSQL = "platform_id in " +
                "(select fk_platform_id " +
                "from platform_parameter " +
                "where " +
                "(name = 'site_depth' and value_text <= cast('-23m' as varchar)))";

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
                        String.format(EntityQueryConstants.ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE,
                                "site_depth",
                                "String"),
                e.getMessage());
    }

}
