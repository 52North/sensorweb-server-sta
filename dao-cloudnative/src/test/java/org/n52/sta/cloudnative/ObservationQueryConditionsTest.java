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

import org.n52.sta.cloudnative.condition.ObservationQueryConditions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnativedao")
public class ObservationQueryConditionsTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired private DSLContext ctx;
    private ObservationQueryConditions observationQueryConditions;

    @BeforeEach
    public void setUp() {
        observationQueryConditions = new ObservationQueryConditions();
        observationQueryConditions.setDslContext(ctx);
    }

    @Test
    public void testWithFeatureOfInterestStaIdentifier_TP() {

        String featureIdentifier = "feature123";

        Condition result = observationQueryConditions.withFeatureOfInterestStaIdentifier(featureIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "observation.fk_dataset_id in " +
                "(select dataset.dataset_id from " +
                "dataset join feature " +
                "on dataset.fk_feature_id = feature.feature_id " +
                "where feature.sta_identifier = 'feature123')";


        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDatastreamStaIdentifier_TP() {

        String datastreamStaIdentifier = "datastream123";

        Condition result = observationQueryConditions.withDatastreamStaIdentifier(datastreamStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "observation.fk_dataset_id in " +
                "(select dataset.dataset_id " +
                "from dataset join observation " +
                "on dataset.dataset_id = observation.fk_dataset_id " +
                "where dataset.sta_identifier = 'datastream123')";


        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDatasetId_TP() {

        long datastreamId = 1L;

        Condition result = observationQueryConditions.withDatastreamId(datastreamId);

        String sql = ctx.renderInlined(result);
        String expectedSQL = "fk_dataset_id = 1";


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
        String expectedSQL = "fk_dataset_id in " +
                "(select dataset_id " +
                "from dataset " +
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
        String expectedSQL = "fk_dataset_id in " +
                "(select dataset_id " +
                "from dataset " +
                "where fk_feature_id in " +
                "(select feature_id " +
                "from feature " +
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
        String expectedSQL = "observation_id in " +
                "(select fk_observation_id " +
                "from observation_parameter " +
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
        String expectedSQL = "sta_identifier = cast('observation123' as varchar)";

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
                "result_time = cast(timestamp '%s' as timestamp)",
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
                "(sampling_time_start = cast(timestamp '%s' as timestamp) " +
                        "and sampling_time_end = cast(timestamp '%s' as timestamp))",
                currentTime.toString(),
                currentTime.toString()
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
                "sampling_time_end < cast(timestamp '%s' as timestamp)",
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
                "sampling_time_start > cast(timestamp '%s' as timestamp)",
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
                "(sampling_time_start <> cast(timestamp '%s' as timestamp)" +
                " or sampling_time_end <> cast(timestamp '%s' as timestamp))",
                currentTime.toString(),
                currentTime.toString()
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
        String expectedSQL = "observation_id in " +
                "(select fk_observation_id " +
                "from observation_parameter " +
                "where " +
                "(name = 'valid' and value_text = cast('true' as varchar)))";

        Assertions.assertEquals(expectedSQL, sql);
    }

}
