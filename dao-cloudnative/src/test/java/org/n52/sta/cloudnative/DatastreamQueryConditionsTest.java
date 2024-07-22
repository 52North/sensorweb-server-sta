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

import org.n52.sta.cloudnative.condition.DatastreamQueryConditions;
import org.n52.sta.cloudnative.condition.EntityQueryConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@ExtendWith(SpringExtension.class)
@Import(TestDatabaseConfig.class)
@ActiveProfiles("cloudnativedao")
public class DatastreamQueryConditionsTest {
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DSLContext ctx;
    private DatastreamQueryConditions datastreamQueryConditions;

    @BeforeEach
    public void setUp() {
        datastreamQueryConditions = new DatastreamQueryConditions();
        datastreamQueryConditions.setDslContext(ctx);
    }

    @Test
    public void testWithName() {
        final String name = "Nitric Datastream";

        Condition result = datastreamQueryConditions.withName(name);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "(fk_aggregate_id is null " +
                "and " +
                "name = '%s')",
                name);

        Assertions.assertEquals(expectedSQL, sql);

    }

    @Test
    public void testWithStaIdentifier() {
        final String staIdentifier = "datastream123";

        Condition result = datastreamQueryConditions.withStaIdentifier(staIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "(fk_aggregate_id is null " +
                "and " +
                "sta_identifier = '%s')",
                staIdentifier);

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithStaIdentifierList() {
        List<String> staIdentifierList = new ArrayList<String>();
        staIdentifierList.add("datastream101");
        staIdentifierList.add("datastream102");
        staIdentifierList.add("datastream103");

        Condition result = datastreamQueryConditions.withStaIdentifier(staIdentifierList);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "(fk_aggregate_id is null " +
                "and " +
                "sta_identifier in " +
                "(%s))",
                staIdentifierList
                        .stream()
                        .map(identifier -> "'" + identifier + "'")
                        .collect(Collectors.joining(", "))
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithFeatureStaIdentifier() {
        final String featureStaIdentifier = "datastream101";

        Condition result = datastreamQueryConditions.withFeatureStaIdentifier(featureStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format("exists " +
                "(select 1 one " +
                "from dataset " +
                "join feature " +
                "on " +
                "dataset.fk_feature_id = feature.feature_id " +
                "where " +
                "feature.sta_identifier = '%s')",
                featureStaIdentifier);

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithObservedPropertyStaIdentifier() {
        final String observedPropertyStaIdentifier = "observedProperty123";

        Condition result = datastreamQueryConditions.withObservedPropertyStaIdentifier(observedPropertyStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "exists " +
                        "(select 1 one " +
                        "from dataset " +
                        "join phenomenon " +
                        "on " +
                        "dataset.fk_phenomenon_id = phenomenon.phenomenon_id " +
                        "where " +
                        "phenomenon.sta_identifier = '%s')",
                observedPropertyStaIdentifier
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithObservedPropertyName() {
        final String name = "observed_property_name";

        Condition result = datastreamQueryConditions.withObservedPropertyName(name);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "exists " +
                        "(select 1 one " +
                        "from dataset " +
                        "join phenomenon " +
                        "on " +
                        "dataset.fk_phenomenon_id = phenomenon.phenomenon_id " +
                        "where " +
                        "phenomenon.name = '%s')",
                name
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithThingStaIdentifier() {
        final String thingStaIdentifier = "thing123";

        Condition result = datastreamQueryConditions.withThingStaIdentifier(thingStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "exists " +
                        "(select 1 one " +
                        "from dataset " +
                        "join platform " +
                        "on " +
                        "dataset.fk_platform_id = platform.platform_id " +
                        "where " +
                        "platform.sta_identifier = '%s')",
                thingStaIdentifier
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithThingName() {
        final String thingName = "thing_name";

        Condition result = datastreamQueryConditions.withThingName(thingName);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "exists " +
                        "(select 1 one " +
                        "from dataset " +
                        "join platform " +
                        "on " +
                        "dataset.fk_platform_id = platform.platform_id " +
                        "where " +
                        "platform.name = '%s')",
                thingName
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithSensorStaIdentifier() {
        final String sensorStaIdentifier = "sensor123";

        Condition result = datastreamQueryConditions.withSensorStaIdentifier(sensorStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "exists " +
                        "(select 1 one " +
                        "from dataset " +
                        "join procedure " +
                        "on " +
                        "dataset.fk_procedure_id = procedure.procedure_id " +
                        "where " +
                        "procedure.sta_identifier = '%s')",
                sensorStaIdentifier
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithSensorName() {
        final String sensorName = "sensor_name";

        Condition result = datastreamQueryConditions.withSensorName(sensorName);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format(
                "exists " +
                        "(select 1 one " +
                        "from dataset " +
                        "join procedure " +
                        "on " +
                        "dataset.fk_procedure_id = procedure.procedure_id " +
                        "where " +
                        "procedure.name = '%s')",
                sensorName
        );

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithObservationStaIdentifier() {
        final String observationStaIdentifier = "observation123";

        Condition result = datastreamQueryConditions.withObservationStaIdentifier(observationStaIdentifier);

        String sql = ctx.renderInlined(result);
        String expectedSQL = String.format("(dataset_id in " +
                "(select fk_dataset_id " +
                "from observation " +
                "where sta_identifier = '%s') " +
                "or dataset_id in " +
                "(select fk_aggregate_id " +
                "from dataset " +
                "where dataset_id in " +
                    "(select fk_dataset_id " +
                    "from observation " +
                    "where sta_identifier = '%s'))" +
                ")",
                observationStaIdentifier, observationStaIdentifier);

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Observation() {
        String propertyName = EntityQueryConstants.OBSERVATIONS;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = datastreamQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "dataset_id in " +
                "(select fk_dataset_id " +
                "from observation " +
                "where " + propertyValue + ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Thing() {
        String propertyName = EntityQueryConstants.THING;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = datastreamQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "fk_platform_id in " +
                "(select platform_id " +
                "from platform " +
                "where " + propertyValue + ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_ObservedProperty() {
        String propertyName = EntityQueryConstants.OBSERVED_PROPERTY;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = datastreamQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "fk_phenomenon_id in " +
                "(select phenomenon_id " +
                "from phenomenon " +
                "where " + propertyValue + ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithRelatedPropertyFilter_Sensor() {
        String propertyName = EntityQueryConstants.SENSOR;
        Condition propertyValue = DSL.condition("");
        Condition result = null;

        try {
            result = datastreamQueryConditions.getFilterForRelation(propertyName, propertyValue);
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }

        String sql = ctx.renderInlined(result);
        String expectedSQL = "fk_procedure_id in " +
                "(select procedure_id " +
                "from procedure " +
                "where " + propertyValue + ")";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterId() {
        String propertyName = StaConstants.PROP_ID;
        Field<String> propertyValue = DSL.val("datastream123");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = datastreamQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            System.out.println(e);
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "sta_identifier = cast('datastream123' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterName() {
        String propertyName = StaConstants.PROP_NAME;
        Field<String> propertyValue = DSL.val("datastream_collection");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = datastreamQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "name = cast('datastream_collection' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDescription() {
        String propertyName = StaConstants.PROP_DESCRIPTION;
        Field<String> propertyValue = DSL.val("description of the datastream");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = datastreamQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "description = cast('description of the datastream' as varchar)";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterType() {
        String propertyName = StaConstants.PROP_OBSERVATION_TYPE;
        Field<String> propertyValue = DSL.val("type of the datastream");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = datastreamQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "fk_format_id in " +
                "(select format_id " +
                "from format " +
                "where " +
                "format.definition = cast('type of the datastream' as varchar))";

        Assertions.assertEquals(expectedSQL, sql);
    }

    @Test
    public void testWithDirectPropertyFilterDefault() {
        String propertyName = "properties/dsNo";
        Field<String> propertyValue = DSL.val("08.09.10");
        FilterConstants.ComparisonOperator operator = FilterConstants.ComparisonOperator.PropertyIsEqualTo;
        Condition result = null;
        try {
            result = datastreamQueryConditions.getFilterForProperty(
                    propertyName,
                    propertyValue,
                    operator,
                    false
            );
        } catch (STAInvalidFilterExpressionException e) {
            e.printStackTrace();
        }
        String sql = ctx.renderInlined(result);
        String expectedSQL = "dataset_id in " +
                "(select fk_dataset_id " +
                "from dataset_parameter " +
                "where " +
                "(name = 'dsNo' and value_text = cast('08.09.10' as varchar)))";

        Assertions.assertEquals(expectedSQL, sql);
    }

}
