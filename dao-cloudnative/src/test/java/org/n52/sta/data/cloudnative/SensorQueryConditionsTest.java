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
import org.n52.sta.data.cloudnative.condition.SensorQueryConditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class SensorQueryConditionsTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired private DSLContext ctx;
    private SensorQueryConditions sensorQueryConditions;

    @BeforeEach
    public void setUp() {
        sensorQueryConditions = new SensorQueryConditions();
        sensorQueryConditions.setDslContext(ctx);
    }

    @Test
    public void testWithDatastreamStaIdentifier_TP() {

        final String staIdentifier = "datastream123";

        Condition result = sensorQueryConditions.withDatastreamStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "exists " +
                "(select 1 one " +
                "from " +
                "dataset join procedure " +
                "on dataset.fk_procedure_id = procedure.procedure_id " +
                "where dataset.sta_identifier = 'datastream123')";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithRelatedPropertyFilter_TP() {

        String propertyName = EntityQueryConstants.DATASTREAMS;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = sensorQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderNamedOrInlinedParams(result);
        String expectedSQL = "procedure_id in " +
                "(select " +
                "dataset.fk_procedure_id " +
                "from " +
                "dataset " +
                "where ())";

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithDirectPropertyFilterId_TP() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("sensor123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = sensorQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "sta_identifier = cast('sensor123' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId_FP() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("sensor123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsNotEqualTo;
        Condition result = null;
        try {
            result = sensorQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "sta_identifier = 'sensor123'";

        Assertions.assertNotEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId_TN() {
        String propertyName = StaConstants.PROP_ID;
        Field<Integer> propertyValue = DSL.val(Integer.valueOf(123));
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;

        Exception e = Assertions.assertThrows(
                RuntimeException.class,
                () -> {
                    sensorQueryConditions.getFilterForProperty(propertyName, propertyValue, operator, false);
                }
        );
        Assertions.assertEquals("org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException: "
                        + EntityQueryConstants.INVALID_DATATYPE_CANNOT_CAST
                        + propertyValue.getDataType().getType()
                        + " to String.class",
                e.getMessage());
    }

    @Test
    public void testWithDirectPropertyFilterName_TP() {
        String propertyName = StaConstants.PROP_NAME;
        Field<String> propertyValue = DSL.val("airSensor");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = sensorQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "name = cast('airSensor' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDescription_TP() {
        String propertyName = StaConstants.PROP_DESCRIPTION;
        Field<String> propertyValue = DSL.val("Sensor to measure C02 levels in the atmosphere");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = sensorQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "description = cast('Sensor to measure C02 levels in the atmosphere' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterEncodingType_TP() {
        String propertyName = StaConstants.PROP_ENCODINGTYPE;
        Field<String> propertyValue = DSL.val("pdf");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = sensorQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "procedure_id in " +
                "(select procedure.procedure_id " +
                "from procedure " +
                "join format " +
                "on procedure.fk_format_id = format.format_id " +
                "where format.definition = cast('pdf' as varchar))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterMetadata_TP() {
        String propertyName = StaConstants.PROP_METADATA;
        Field<String> propertyValue = DSL.val("metadata_info");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = sensorQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "description_file = cast('metadata_info' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDefault_TP() {
        String propertyName = "properties/valid";
        Field<String> propertyValue = DSL.val("true");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = sensorQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "procedure_id in " +
                "(select fk_procedure_id " +
                "from procedure_parameter " +
                "where " +
                "(name = 'valid' and value_text = cast('true' as varchar)))";

        Assertions.assertEquals(expectedSQL, sql);
    }
}
