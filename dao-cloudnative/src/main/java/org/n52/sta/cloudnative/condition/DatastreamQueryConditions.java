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
package org.n52.sta.cloudnative.condition;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import java.util.List;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class DatastreamQueryConditions extends EntityQueryConditions {

    @Override
    public Condition withName(final String name) {
        return DSL.field(FK_AGGREGATE_ID_FIELD).isNull().and(DSL.field(STA_NAME_FIELD).eq(name));
    }

    @Override
    public Condition withStaIdentifier(final String name) {
        return DSL.field(FK_AGGREGATE_ID_FIELD).isNull().and(DSL.field(STA_IDENTIFIER_FIELD).eq(name));
    }

    @Override
    public Condition withStaIdentifier(final List<String> identifiers) {
        return DSL.field(FK_AGGREGATE_ID_FIELD).isNull().and(DSL.field(STA_IDENTIFIER_FIELD).in(identifiers));
    }


    @Override
    protected <T extends Comparable<? super T>> Condition handleDirectPropertyFilter(
            String propertyName,
            Field<T> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        try {
            switch (propertyName) {
                case StaConstants.PROP_ID:
                    return handleDirectStringPropertyFilter(
                            DSL.field(STA_IDENTIFIER_FIELD, String.class),
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(
                            DSL.field(STA_NAME_FIELD, String.class),
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            DSL.field(STA_DESCRIPTION_FIELD, String.class),
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_OBSERVATION_TYPE:
                    Condition subCondition = handleDirectStringPropertyFilter(
                            DSL.field(DSL.name(FORMAT_TABLE, STA_DEFINITION_FIELD), String.class),
                            propertyValue,
                            operator,
                            switched);
                    SelectConditionStep<Record1<Object>> subquery = ctx
                            .select(DSL.field(FORMAT_ID_FIELD))
                            .from(DSL.table(FORMAT_TABLE))
                            .where(subCondition);
                    return DSL.field(FK_FORMAT_ID_FIELD).in(subquery);
                default:
                    // We are filtering on variable keys on properties
                    if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                        return handleProperties(
                                propertyName,
                                propertyValue,
                                operator,
                                switched,
                                FK_DATASTREAM_ID_FIELD,
                                ParameterFactory.EntityType.DATASET);
                    } else {
                        throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP, propertyName));
                    }
            }
        } catch (STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Condition handleRelatedPropertyFilter(String propertyName, Condition propertyValue) {
        try {
            switch (propertyName) {
                case SENSOR: {
                    SelectConditionStep<Record1<Object>> subquery = ctx
                            .select(DSL.field(SENSOR_ID_FIELD))
                            .from(DSL.table(SENSOR_TABLE))
                            .where(propertyValue);
                    return DSL.field(FK_SENSOR_ID_FIELD).in(subquery);
                }
                case OBSERVED_PROPERTY: {
                    SelectConditionStep<Record1<Object>> subquery = ctx
                            .select(DSL.field(OBSERVED_PROPERTY_ID_FIELD))
                            .from(DSL.table(OBSERVED_PROPERTY_TABLE))
                            .where(propertyValue);
                    return DSL.field(FK_OBSERVED_PROPERTY_ID_FIELD).in(subquery);
                }
                case THING: {
                    SelectConditionStep<Record1<Object>> subquery = ctx
                            .select(DSL.field(THING_ID_FIELD))
                            .from(DSL.table(THING_TABLE))
                            .where(propertyValue);
                    return DSL.field(FK_THING_ID_FIELD).in(subquery);
                }
                case OBSERVATIONS: {
                    SelectConditionStep<Record1<Object>> subquery = ctx
                            .select(DSL.field(FK_DATASTREAM_ID_FIELD))
                            .from(DSL.table(OBSERVATION_TABLE))
                            .where(propertyValue);
                    return DSL.field(DATASTREAM_ID_FIELD).in(subquery);
                }
                default:
                    throw new STAInvalidFilterExpressionException(COULD_NOT_FIND_RELATED_PROPERTY + propertyName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected <T extends Comparable<? super T>> Condition handleProperties(
                                         String propertyName,
                                         Field<T> propertyValue,
                                         FilterConstants.ComparisonOperator operator,
                                         boolean switched,
                                         String referenceName,
                                         ParameterFactory.EntityType entityType)
            throws STAInvalidFilterExpressionException {

        String key = propertyName.substring(11);
        final String categoryPrefix = "category";
        Field<Double> categoryId = DSL.field(CATEGORY_ID_FIELD, Double.class);

        // Handle filter on Category->id
        if (Number.class.isAssignableFrom(propertyValue.getDataType().getType())) {
            if (key.startsWith(categoryPrefix)) {
                if (key.substring(categoryPrefix.length()).equals("Id")) {
                    Condition IdCondition = handleDirectNumberPropertyFilter(
                            categoryId,
                            propertyValue,
                            operator);
                    return DSL.exists(
                            ctx.selectOne()
                                    .from(DSL.table(CATEGORY_TABLE))
                                    .innerJoin(DSL.table(DATASTREAM_TABLE))
                                    .onKey()
                                    .where(IdCondition)
                    );

                } else {
                    throw new STAInvalidFilterExpressionException(
                            String.format(ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE, key, "Long"));
                }
            }
        }

        // Handle filter on Category->name and Category->description
        if (propertyValue.getDataType().getType().isAssignableFrom(String.class)) {
            if (key.startsWith(categoryPrefix)) {
                String property = null;
                switch (key.substring(categoryPrefix.length())) {
                    case "Name":
                        property = STA_NAME_FIELD;
                        break;
                    case "Description":
                        property = STA_DESCRIPTION_FIELD;
                        break;
                    default:
                        throw new STAInvalidFilterExpressionException(
                                String.format(ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE, key, "String"));
                }
                Field<String> categoryField = DSL.field(property, String.class);
                Condition categoryCondition = handleDirectStringPropertyFilter(
                        categoryField,
                        propertyValue,
                        operator,
                        switched);
                SelectConditionStep<Record1<Double>> subquery = ctx
                        .select(categoryId)
                        .from(DSL.table(CATEGORY_TABLE))
                        .where(categoryCondition);
                return DSL.field(FK_CATEGORY_ID_FIELD).in(subquery);
            }
        }

        return super.handleProperties(
                propertyName,
                propertyValue,
                operator,
                switched,
                referenceName,
                entityType);

    }

    public Condition withFeatureStaIdentifier(final String featureIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(DATASTREAM_TABLE))
                        .innerJoin(DSL.table(FEATURE_TABLE))
                        .onKey()
                        .where(DSL.field(DSL.name(FEATURE_TABLE, STA_IDENTIFIER_FIELD))
                                .eq(featureIdentifier))
        );
    }

    public Condition withObservedPropertyStaIdentifier(final String observablePropertyIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(DATASTREAM_TABLE))
                        .innerJoin(DSL.table(OBSERVED_PROPERTY_TABLE))
                        .onKey()
                        .where(DSL.field(DSL.name(OBSERVED_PROPERTY_TABLE, STA_IDENTIFIER_FIELD))
                                .eq(observablePropertyIdentifier))
        );
    }

    public Condition withObservedPropertyName(final String name) {
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(DATASTREAM_TABLE))
                        .innerJoin(DSL.table(OBSERVED_PROPERTY_TABLE))
                        .onKey()
                        .where(DSL.field(DSL.name(OBSERVED_PROPERTY_TABLE, STA_NAME_FIELD))
                                .eq(name))
        );
    }

    public Condition withThingStaIdentifier(final String thingIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(DATASTREAM_TABLE))
                        .innerJoin(DSL.table(THING_TABLE))
                        .onKey()
                        .where(DSL.field(DSL.name(THING_TABLE, STA_IDENTIFIER_FIELD))
                                .eq(thingIdentifier))
        );
    }

    public Condition withThingName(final String name) {
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(DATASTREAM_TABLE))
                        .innerJoin(DSL.table(THING_TABLE))
                        .onKey()
                        .where(DSL.field(DSL.name(THING_TABLE, STA_NAME_FIELD))
                                .eq(name))
        );
    }

    public Condition withSensorStaIdentifier(final String sensorIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(DATASTREAM_TABLE))
                        .innerJoin(DSL.table(SENSOR_TABLE))
                        .onKey()
                        .where(DSL.field(DSL.name(SENSOR_TABLE, STA_IDENTIFIER_FIELD))
                                .eq(sensorIdentifier))
        );
    }

    public Condition withSensorName(final String name) {
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(DATASTREAM_TABLE))
                        .innerJoin(DSL.table(SENSOR_TABLE))
                        .onKey()
                        .where(DSL.field(DSL.name(SENSOR_TABLE, STA_NAME_FIELD))
                                .eq(name))
        );
    }

    public Condition withObservationStaIdentifier(String observationIdentifier) {

        // Subquery to get dataset_id from Observation where sta_identifier matches
        SelectConditionStep<Record1<Object>> sq = ctx.select(DSL.field(DATASTREAM_ID_FIELD))
                .from(OBSERVATION_TABLE)
                .where(DSL.field(STA_IDENTIFIER_FIELD).eq(observationIdentifier));

        // Subquery to get fk_aggregate_id from Datastream where id matches the result from the first subquery
        SelectConditionStep<Record1<Object>> subquery = ctx.select(DSL.field(FK_AGGREGATE_ID_FIELD))
                .from(DATASTREAM_TABLE)
                .where(DSL.field(DATASTREAM_ID_FIELD).in(sq));

        // Either id matches the result from the first subquery or id matches the result from the second subquery
        return DSL.field(DATASTREAM_ID_FIELD).in(sq)
                .or(DSL.field(DATASTREAM_ID_FIELD).in(subquery));
    }

        public String checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_PHENOMENON_TIME:
                return DATASTREAM_PHENOMENONTIME_START_FIELD;
            case StaConstants.PROP_RESULT_TIME:
                return DATASTREAM_RESULTTIME_START_FIELD;
            default:
                return super.checkPropertyName(property);
        }
    }

}
