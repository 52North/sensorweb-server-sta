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

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import java.util.Date;
import org.jooq.impl.DSL;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class ObservationQueryConditions extends EntityQueryConditions {

    public Condition withFeatureOfInterestStaIdentifier(final String featureIdentifier) {

        SelectConditionStep<Record1<Object>> subquery = ctx
                .select(DSL.field(DSL.name(DATASTREAM_TABLE, DATASTREAM_ID_FIELD)))
                .from(DSL.table(DATASTREAM_TABLE))
                .innerJoin(DSL.table(FEATURE_TABLE))
                .onKey()
                .where(DSL.field(DSL.name(FEATURE_TABLE, STA_IDENTIFIER_FIELD))
                        .eq(featureIdentifier));

           return DSL.field(DSL.name(OBSERVATION_TABLE, FK_DATASTREAM_ID_FIELD)).in(subquery);
    }

    public Condition withDatastreamStaIdentifier(final String datastreamStaIdentifier) {
        SelectConditionStep<Record1<Object>> subquery = ctx
                .select(DSL.field(DSL.name(DATASTREAM_TABLE, DATASTREAM_ID_FIELD)))
                .from(DSL.table(DATASTREAM_TABLE))
                .innerJoin(DSL.table(OBSERVATION_TABLE))
                .onKey()
                .where(DSL.field(DSL.name(DATASTREAM_TABLE, STA_IDENTIFIER_FIELD))
                        .eq(datastreamStaIdentifier));

        return DSL.field(DSL.name(OBSERVATION_TABLE, FK_DATASTREAM_ID_FIELD)).in(subquery);
    }

    public Condition withDatasetId(final long datasetId) {
        return DSL.field(DATASTREAM_ID_FIELD).eq(datasetId);
    }

    /*
    public Condition withParent(final long parentId) {
        return null;
    }
    */

    @Override
    protected <T extends Comparable<? super T>> Condition
    handleDirectPropertyFilter(String propertyName,
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
                case StaConstants.PROP_RESULT:
                    if (Double.class.isAssignableFrom(propertyValue.getDataType().getType())
                            || Integer.class.isAssignableFrom(propertyValue.getDataType().getType())) {
                        Condition countCondition = handleDirectNumberPropertyFilter(
                                // Integer Field
                                DSL.field(OBSERVATION_VALUE_COUNT_FIELD, Double.class),
                                propertyValue,
                                operator);

                        Condition quantityCondition = handleDirectNumberPropertyFilter(
                                // Numeric(20,10) field
                                DSL.field(OBSERVATION_VALUE_QUANTITY_FIELD, Double.class),
                                propertyValue,
                                operator);

                        // Check for quantity or count as those are numeric
                        // Do not return observations with non-numeric result type
                        return countCondition
                                .or(DSL.field(OBSERVATION_VALUE_COUNT_FIELD).isNull())
                                .and(quantityCondition.or(DSL.field(OBSERVATION_VALUE_QUANTITY_FIELD).isNull()))
                                .and(DSL.field(OBSERVATION_VALUE_CATEGORY_FIELD).isNull())
                                .and(DSL.field(OBSERVATION_VALUE_TEXT_FIELD).isNull())
                                .and(DSL.field(OBSERVATION_VALUE_BOOLEAN_FIELD).isNull());

                    } else if (String.class.isAssignableFrom(propertyValue.getDataType().getType())) {
                        Condition categoryCondition = handleDirectStringPropertyFilter(
                                DSL.field(OBSERVATION_VALUE_CATEGORY_FIELD, String.class),
                                propertyValue,
                                operator,
                                false);

                        Condition textCondition = handleDirectStringPropertyFilter(
                                DSL.field(OBSERVATION_VALUE_TEXT_FIELD, String.class),
                                propertyValue,
                                operator,
                                false);

                        /*
                        Condition boolCondition = handleDirectStringPropertyFilter(
                                DSL.field(OBSERVATION_VALUE_BOOLEAN_FIELD, Boolean.class),
                                propertyValue,
                                operator,
                                false);
                        */

                        // Check for category, text, boolean as those represented by String in query
                        // Do not return observations with numeric result type as we are filtering on string
                        return categoryCondition.or(DSL.field(OBSERVATION_VALUE_CATEGORY_FIELD).isNull())
                                        .and(textCondition.or(DSL.field(OBSERVATION_VALUE_TEXT_FIELD).isNull()))
                                        // .and(boolCondition.or(DSL.field(OBSERVATION_VALUE_BOOLEAN_FIELD).isNull())
                                        .and(DSL.field(OBSERVATION_VALUE_COUNT_FIELD).isNull())
                                        .and(DSL.field(OBSERVATION_VALUE_QUANTITY_FIELD).isNull());
                    } else {
                        throw new STAInvalidFilterExpressionException("Value type not supported!");
                    }
                case StaConstants.PROP_RESULT_TIME:
                    return this.handleDirectDateTimePropertyFilter(
                            DSL.field(OBSERVATION_RESULT_TIME_FIELD, Date.class),
                            propertyValue,
                            operator);
                case StaConstants.PROP_PHENOMENON_TIME:
                    switch (operator) {
                        case PropertyIsLessThan:
                        case PropertyIsLessThanOrEqualTo:
                            return handleDirectDateTimePropertyFilter(
                                    DSL.field(OBSERVATION_SAMPLING_TIME_END_FIELD, Date.class),
                                    propertyValue,
                                    operator);
                        case PropertyIsGreaterThan:
                        case PropertyIsGreaterThanOrEqualTo:
                            return handleDirectDateTimePropertyFilter(
                                    DSL.field(OBSERVATION_SAMPLING_TIME_START_FIELD, Date.class),
                                    propertyValue,
                                    operator);
                        case PropertyIsEqualTo:
                            Condition eqStart = handleDirectDateTimePropertyFilter(
                                    DSL.field(OBSERVATION_SAMPLING_TIME_START_FIELD, Date.class),
                                    propertyValue,
                                    operator);
                            Condition eqEnd = handleDirectDateTimePropertyFilter(
                                    DSL.field(OBSERVATION_SAMPLING_TIME_END_FIELD, Date.class),
                                    propertyValue,
                                    operator);
                            return eqStart.and(eqEnd);
                        case PropertyIsNotEqualTo:
                            Condition neStart = handleDirectDateTimePropertyFilter(
                                    DSL.field(OBSERVATION_SAMPLING_TIME_START_FIELD, Date.class),
                                    propertyValue,
                                    operator);
                            Condition neEnd = handleDirectDateTimePropertyFilter(
                                    DSL.field(OBSERVATION_SAMPLING_TIME_END_FIELD, Date.class),
                                    propertyValue,
                                    operator);
                            return neStart.or(neEnd);
                        default:
                            throw new STAInvalidFilterExpressionException(
                                    "Unknown operator: " + operator.toString());
                    }
                default:
                    // We are filtering on variable keys on parameters
                    if (propertyName.startsWith(StaConstants.PROP_PARAMETERS)) {
                        return handleProperties(
                                propertyName,
                                propertyValue,
                                operator,
                                switched,
                                FK_OBSERVATION_ID_FIELD,
                                ParameterFactory.EntityType.OBSERVATION);
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
        SelectConditionStep<Record1<Object>> subquery;
        try {
            if (DATASTREAM.equals(propertyName)) {
                subquery = ctx.select(DSL.field(DATASTREAM_TABLE + "." + DATASTREAM_ID_FIELD))
                        .from(DSL.table(DATASTREAM_TABLE))
                        .where(propertyValue);

                return DSL.field(DATASTREAM_ID_FIELD).in(subquery);

            } else if (FEATUREOFINTEREST.equals(propertyName)) {
                SelectConditionStep<Record1<Object>> subSubquery;

                subSubquery = ctx.select(DSL.field(FEATURE_ID_FIELD))
                        .from(DSL.table(FEATURE_TABLE))
                        .where(propertyValue);

                subquery = ctx.select(DSL.field(DATASTREAM_ID_FIELD))
                        .from(DSL.table(DATASTREAM_TABLE))
                        .where(DSL.field(FEATURE_ID_FIELD).in(subSubquery));

                return DSL.field(DATASTREAM_ID_FIELD).in(subquery);
            } else if (StaConstants.PROP_PARAMETERS.equals(propertyName)) {
                subquery = ctx.select(DSL.field(FK_OBSERVATION_ID_FIELD))
                        .from(DSL.table(OBSERVATION_PARAMETER_TABLE))
                        .where(propertyValue);
                return DSL.field(OBSERVATION_ID_FIELD).in(subquery);
            } else {
                throw new STAInvalidFilterExpressionException("Could not find related property: " + propertyName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_PHENOMENON_TIME:
                // TODO: proper ISO8601 comparison
                return OBSERVATION_SAMPLING_TIME_END_FIELD;

            /* This is handled separately as result is split up over multiple columns
            case "result":
                return "valueBoolean";

             */
            default:
                return super.checkPropertyName(property);
        }
    }
}
