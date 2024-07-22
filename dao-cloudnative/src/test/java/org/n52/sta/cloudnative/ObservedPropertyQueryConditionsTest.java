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
import org.n52.sta.cloudnative.condition.EntityQueryConstants;
import org.n52.sta.cloudnative.condition.ObservedPropertyQueryConditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnativedao")
public class ObservedPropertyQueryConditionsTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private ObservedPropertyQueryConditions observedPropertyQueryConditions;

    @BeforeEach
    public void setUp() {
        observedPropertyQueryConditions = new ObservedPropertyQueryConditions();
        observedPropertyQueryConditions.setDslContext(ctx);
    }

    @Test
    public void testWithDatastreamStaIdentifier_TP() {

        final String staIdentifier = "datastream123";

        Condition result = observedPropertyQueryConditions.withDatastreamStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select 1 one " +
                "from " +
                "dataset join phenomenon " +
                "on phenomenon.phenomenon_id = dataset.fk_phenomenon_id " +
                "where dataset.sta_identifier = 'datastream123')";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_TP() {

        String propertyName = EntityQueryConstants.DATASTREAMS;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = observedPropertyQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderNamedOrInlinedParams(result);
        String expectedSQL = "phenomenon_id in " +
                "(select " +
                "dataset.fk_phenomenon_id " +
                "from " +
                "dataset " +
                "where ())";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithRelatedPropertyFilter_TN() {
        // Unrelated entity
        final String propertyName = EntityQueryConstants.FEATUREOFINTEREST;
        Condition propertyValue = DSL.condition("");

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
            observedPropertyQueryConditions.getFilterForRelation(propertyName, propertyValue);
        });

        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: " +
                        "Could not find related property: " +
                        propertyName,
                e.getMessage());
    }

    @Test
    public void testWithDirectPropertyFilterId_TP() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("observedProperty123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsGreaterThanOrEqualTo;
        Condition result = null;
        try {
            result = observedPropertyQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "sta_identifier >= cast('observedProperty123' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId_FP() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("observedProperty123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsGreaterThan;
        Condition result = null;
        try {
            result = observedPropertyQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "sta_identifier >= 'observedProperty123'";

        Assertions.assertNotEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId_TN() {
        String propertyName = StaConstants.PROP_ID;
        Field<Integer> propertyValue = DSL.val(Integer.valueOf(123));
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsLessThan;

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
            observedPropertyQueryConditions.getFilterForProperty(propertyName, propertyValue, operator, false);
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
        Field<String> propertyValue = DSL.val("O3 conc.");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = observedPropertyQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "name = cast('O3 conc.' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterName_TN() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("O3 conc.");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsBetween;

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
            observedPropertyQueryConditions.getFilterForProperty(propertyName, propertyValue, operator, false);
                }
        );
        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: " +
                        "Operator \"PropertyIsBetween\" is not supported for given arguments.",
                e.getMessage());
    }

    @Test
    public void testWithDirectPropertyFilterDescription_TP() {
        String propertyName = StaConstants.PROP_DESCRIPTION;
        Field<String> propertyValue = DSL.val("ozone concentration levels in mg");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = observedPropertyQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "description = cast('ozone concentration levels in mg' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDescription_TN() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("ozone concentration levels in mg");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsNil;

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
            observedPropertyQueryConditions.getFilterForProperty(propertyName, propertyValue, operator, false);
                }
        );
        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: " +
                        "Operator \"PropertyIsNil\" is not supported for given arguments.",
                e.getMessage());
    }

    @Test
    public void testWithDirectPropertyFilterDefinition_TP() {
        String propertyName = StaConstants.PROP_DEFINITION;
        Field<String> propertyValue = DSL.val("pdf/o3");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = observedPropertyQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "definition = cast('pdf/o3' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDefinition_TN() {
        String propertyName = StaConstants.PROP_ID;
        Field<Integer> propertyValue = DSL.val(Integer.valueOf(985413421));
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
                    observedPropertyQueryConditions.getFilterForProperty(propertyName, propertyValue, operator, false);
                }
        );
        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: " +
                        EntityQueryConstants.INVALID_DATATYPE_CANNOT_CAST +
                        propertyValue.getDataType().getType() + " to String.class",
                e.getMessage());
    }

    @Test
    public void testWithDirectPropertyFilterDefault_TP() {
        String propertyName = "properties/sensitive";
        Field<String> propertyValue = DSL.val("true");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = observedPropertyQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "phenomenon_id in " +
                "(select fk_phenomenon_id " +
                "from phenomenon_parameter " +
                "where " +
                "(name = 'sensitive' and value_text = cast('true' as varchar)))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDefault_TN() {
        String propertyName = "properties/sensitive";
        Field<Integer> propertyValue = DSL.val(Integer.valueOf(0));
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsLessThanOrEqualTo;

        Exception e = Assertions.assertThrows(RuntimeException.class, () -> {
            observedPropertyQueryConditions.getFilterForProperty(propertyName, propertyValue, operator, false);
                }
        );
        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: " +
                        String.format(EntityQueryConstants.ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE,
                                "sensitive",
                                "String"),
                e.getMessage());
    }

}
