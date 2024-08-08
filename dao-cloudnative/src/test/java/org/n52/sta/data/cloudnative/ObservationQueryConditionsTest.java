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

import org.jooq.*;
import org.jooq.impl.DSL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Assertions;

import org.n52.series.db.common.Utils;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;

import org.n52.sta.data.cloudnative.condition.ObservationQueryConditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnative")
public class ObservationQueryConditionsTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired private DSLContext ctx;
    private ObservationQueryConditions observationQueryConditions;

    @BeforeEach
    public void setUp() {
        observationQueryConditions = new ObservationQueryConditions();
        observationQueryConditions.setDslContext(ctx);
    }

    private String read_parquet(String table) {
        return String.format("read_parquet('s3://52n-sta/%s') %s ", table, table);
    }

    @Test
    public void testWithFeatureOfInterestStaIdentifier_TP() {

        String featureIdentifier = "feature123";

        Condition result = observationQueryConditions.withFeatureOfInterestStaIdentifier(featureIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "OBSERVATION.FK_DATASET_ID in " +
                "(select DATASET.DATASET_ID from " +
                read_parquet("DATASET") +
                "join " +
                read_parquet("FEATURE") +
                "on DATASET.FK_FEATURE_ID = FEATURE.FEATURE_ID " +
                "where FEATURE.STA_IDENTIFIER = 'feature123')";


        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDatastreamStaIdentifier_TP() {

        String datastreamStaIdentifier = "datastream123";

        Condition result = observationQueryConditions.withDatastreamStaIdentifier(datastreamStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "OBSERVATION.FK_DATASET_ID in " +
                "(select DATASET.DATASET_ID from " +
                read_parquet("DATASET") +
                "join " +
                read_parquet("OBSERVATION") +
                "on " +
                "observation.fk_dataset_id = dataset.dataset_id ".toUpperCase() +
                "where DATASET.STA_IDENTIFIER = 'datastream123')";


        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDatasetId_TP() {

        long datastreamId = 1L;

        Condition result = observationQueryConditions.withDatastreamId(datastreamId);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "OBSERVATION.FK_DATASET_ID = 1";


        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Datastream_TP() {

        String propertyName = "Datastream";
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);

        Condition result = null;
        try {
            result = observationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "OBSERVATION.FK_DATASET_ID in " +
                "(select DATASET.DATASET_ID " +
                "from " +
                read_parquet("DATASET") +
                "where " +
                conditionSQL +
                ")";


        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Feature_TP() {

        String propertyName = "FeatureOfInterest";
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);


        Condition result = null;
        try {
            result = observationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "OBSERVATION.FK_DATASET_ID in " +
                "(select DATASET.DATASET_ID " +
                "from " +
                read_parquet("DATASET") +
                "where DATASET.FK_FEATURE_ID in " +
                "(select FEATURE.FEATURE_ID " +
                "from " +
                read_parquet("FEATURE") +
                "where " +
                conditionSQL +
                "))";


        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Parameters_TP() {

        String propertyName = "parameters";
        Condition propertyValue = DSL.condition("");
        String conditionSQL = ctx.renderInlined(propertyValue);


        Condition result = null;
        try {
            result = observationQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "OBSERVATION.OBSERVATION_ID in " +
                "(select OBSERVATION_PARAMETER.FK_OBSERVATION_ID " +
                "from " +
                read_parquet("OBSERVATION_PARAMETER") +
                "where " +
                conditionSQL +
                ")";


        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilter_Id_TP() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("observation123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = observationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "OBSERVATION.STA_IDENTIFIER = 'observation123'";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilter_resultTime_TP() {
        String propertyName = StaConstants.PROP_RESULT_TIME;
        Date currentTime = Utils.createUnmutableTimestamp(new Date());
        Field<Date> propertyValue = DSL.val(currentTime);
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = observationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "OBSERVATION.RESULT_TIME = timestamp '%s'",
                currentTime.toString());

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilter_phenomenonTimeEq_TP() {
        String propertyName = StaConstants.PROP_PHENOMENON_TIME;
        Date currentTime = Utils.createUnmutableTimestamp(new Date());
        Field<Date> propertyValue = DSL.val(currentTime);
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;

        try {
            result = observationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "(OBSERVATION.SAMPLING_TIME_START = timestamp '%s' " +
                        "and OBSERVATION.SAMPLING_TIME_END = timestamp '%s')",
                currentTime,
                currentTime
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilter_phenomenonTimeLt_TP() {
        String propertyName = StaConstants.PROP_PHENOMENON_TIME;
        Date currentTime = Utils.createUnmutableTimestamp(new Date());
        Field<Date> propertyValue = DSL.val(currentTime);
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsLessThan;
        Condition result = null;

        try {
            result = observationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "OBSERVATION.SAMPLING_TIME_END < timestamp '%s'",
                currentTime.toString()
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilter_phenomenonTimeGt_TP() {
        String propertyName = StaConstants.PROP_PHENOMENON_TIME;
        Date currentTime = Utils.createUnmutableTimestamp(new Date());
        Field<Date> propertyValue = DSL.val(currentTime);
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsGreaterThan;
        Condition result = null;

        try {
            result = observationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "OBSERVATION.SAMPLING_TIME_START > timestamp '%s'",
                currentTime.toString()
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilter_phenomenonTimeNe_TP() {
        String propertyName = StaConstants.PROP_PHENOMENON_TIME;
        Date currentTime = Utils.createUnmutableTimestamp(new Date());
        Field<Date> propertyValue = DSL.val(currentTime);
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsNotEqualTo;
        Condition result = null;

        try {
            result = observationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "(OBSERVATION.SAMPLING_TIME_START <> timestamp '%s'" +
                " or OBSERVATION.SAMPLING_TIME_END <> timestamp '%s')",
                currentTime,
                currentTime
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilter_result() {

    }

    @Test
    public void testWithDirectPropertyFilterDefault_TP() {
        String propertyName = "parameters/valid";
        Field<String> propertyValue = DSL.val("true");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;

        try {
            result = observationQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "OBSERVATION.OBSERVATION_ID in " +
                "(select OBSERVATION_PARAMETER.FK_OBSERVATION_ID " +
                "from " +
                read_parquet("OBSERVATION_PARAMETER") +
                "where " +
                "(OBSERVATION_PARAMETER.NAME = 'valid' and OBSERVATION_PARAMETER.VALUE_TEXT = 'true'))";

        Assertions.assertEquals(expectedSQL, sql);
    }

}
