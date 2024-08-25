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
package org.n52.sta.data.cloudnative.condition;

import org.jooq.*;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import java.util.Date;
import java.util.List;

import org.jooq.impl.DSL;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.cloudnative.schema.tables.Observation;
import org.n52.sta.data.cloudnative.schema.tables.ObservationParameter;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class ObservationQueryConditions extends EntityQueryConditions {

    public static Observation StaEntity = OBSERVATION;

    public Condition withFeatureOfInterestStaIdentifier(final String featureIdentifier) {

        SelectConditionStep<Record1<Long>> subquery = ctx
                .select(DATASTREAM.DATASET_ID)
                .from(DATASTREAM)
                .join(FEATURE_OF_INTEREST)
                .onKey()
                .where(FEATURE_OF_INTEREST.STA_IDENTIFIER.eq(featureIdentifier));

           return OBSERVATION.FK_DATASET_ID.in(subquery);
    }

    public Condition withDatastreamStaIdentifier(final String datastreamStaIdentifier) {

        SelectConditionStep<Record1<Long>> subquery = ctx
                .select(DATASTREAM.DATASET_ID)
                .from(DATASTREAM)
                .join(OBSERVATION)
                .onKey()
                .where(DATASTREAM.STA_IDENTIFIER.eq(datastreamStaIdentifier));

        return OBSERVATION.FK_DATASET_ID.in(subquery);
    }

    public Condition withDatastreamId(final long datastreamId) {
        return OBSERVATION.FK_DATASET_ID.eq(datastreamId);
    }

    /*
    public Condition withParent(final long parentId) {
        return null;
    }
    */
    @Override
    public Condition withStaIdentifier(String staIdentifier) {
        return DATASTREAM.STA_IDENTIFIER.eq(staIdentifier);
    }

    @Override
    public Condition withStaIdentifier(List<String> identifiers) {
        return DATASTREAM.STA_IDENTIFIER.in(identifiers);
    }

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
                            OBSERVATION.STA_IDENTIFIER,
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_RESULT:
                    if (Double.class.isAssignableFrom(propertyValue.getDataType().getType())
                            || Integer.class.isAssignableFrom(propertyValue.getDataType().getType())) {
                        Condition countCondition = handleDirectNumberPropertyFilter(
                                // Integer Field
                                OBSERVATION.VALUE_COUNT,
                                propertyValue,
                                operator);

                        Condition quantityCondition = handleDirectNumberPropertyFilter(
                                // Numeric(19,2) field
                                OBSERVATION.VALUE_QUANTITY,
                                propertyValue,
                                operator);

                        // Check for quantity or count as those are numeric
                        // Do not return observations with non-numeric result type
                        return countCondition
                                .or(OBSERVATION.VALUE_COUNT.isNull())
                                .and(quantityCondition.or(OBSERVATION.VALUE_QUANTITY.isNull()))
                                .and(OBSERVATION.VALUE_CATEGORY.isNull())
                                .and(OBSERVATION.VALUE_TEXT.isNull())
                                .and(OBSERVATION.VALUE_BOOLEAN.isNull());

                    } else if (String.class.isAssignableFrom(propertyValue.getDataType().getType())) {
                        Condition categoryCondition = handleDirectStringPropertyFilter(
                                OBSERVATION.VALUE_CATEGORY,
                                propertyValue,
                                operator,
                                false);

                        Condition textCondition = handleDirectStringPropertyFilter(
                                OBSERVATION.VALUE_TEXT,
                                propertyValue,
                                operator,
                                false);

                        /*
                        Condition boolCondition = handleDirectStringPropertyFilter(
                                OBSERVATION.VALUE_BOOLEAN,
                                propertyValue,
                                operator,
                                false);
                        */

                        // Check for category, text, boolean as those represented by String in query
                        // Do not return observations with numeric result type as we are filtering on string
                        return categoryCondition.or(OBSERVATION.VALUE_CATEGORY.isNull())
                                        .and(textCondition.or(OBSERVATION.VALUE_TEXT.isNull()))
                                        // .and(boolCondition.or(OBSERVATION.VALUE_BOOLEAN.isNull())
                                        .and(OBSERVATION.VALUE_COUNT.isNull())
                                        .and(OBSERVATION.VALUE_QUANTITY.isNull());
                    } else {
                        throw new STAInvalidFilterExpressionException("Value type not supported!");
                    }
                case StaConstants.PROP_RESULT_TIME:
                    return this.handleDirectDateTimePropertyFilter(
                            OBSERVATION.RESULT_TIME,
                            propertyValue,
                            operator);
                case StaConstants.PROP_PHENOMENON_TIME:
                    switch (operator) {
                        case PropertyIsLessThan:
                        case PropertyIsLessThanOrEqualTo:
                            return handleDirectDateTimePropertyFilter(
                                    OBSERVATION.SAMPLING_TIME_END,
                                    propertyValue,
                                    operator);
                        case PropertyIsGreaterThan:
                        case PropertyIsGreaterThanOrEqualTo:
                            return handleDirectDateTimePropertyFilter(
                                    OBSERVATION.SAMPLING_TIME_START,
                                    propertyValue,
                                    operator);
                        case PropertyIsEqualTo:
                            Condition eqStart = handleDirectDateTimePropertyFilter(
                                    OBSERVATION.SAMPLING_TIME_START,
                                    propertyValue,
                                    operator);
                            Condition eqEnd = handleDirectDateTimePropertyFilter(
                                    OBSERVATION.SAMPLING_TIME_END,
                                    propertyValue,
                                    operator);
                            return eqStart.and(eqEnd);
                        case PropertyIsNotEqualTo:
                            Condition neStart = handleDirectDateTimePropertyFilter(
                                    OBSERVATION.SAMPLING_TIME_START,
                                    propertyValue,
                                    operator);
                            Condition neEnd = handleDirectDateTimePropertyFilter(
                                    OBSERVATION.SAMPLING_TIME_END,
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
                                OBSERVATION_PARAMETERS.FK_OBSERVATION_ID,
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
        SelectConditionStep<Record1<Long>> subquery;
        try {
            if (STAEntityDefinition.DATASTREAM.equals(propertyName)) {
                subquery = ctx
                        .select(DATASTREAM.DATASET_ID)
                        .from(DATASTREAM)
                        .where(propertyValue);

                return OBSERVATION.FK_DATASET_ID.in(subquery);

            } else if (STAEntityDefinition.FEATURE_OF_INTEREST.equals(propertyName)) {
                SelectConditionStep<Record1<Long>> subSubquery;

                subSubquery = ctx
                        .select(FEATURE_OF_INTEREST.FEATURE_ID)
                        .from(FEATURE_OF_INTEREST)
                        .where(propertyValue);

                subquery = ctx
                        .select(DATASTREAM.DATASET_ID)
                        .from(DATASTREAM)
                        .where(DATASTREAM.FK_FEATURE_ID.in(subSubquery));

                return OBSERVATION.FK_DATASET_ID.in(subquery);

            } else if (StaConstants.PROP_PARAMETERS.equals(propertyName)) {
                subquery = ctx
                        .select(OBSERVATION_PARAMETERS.FK_OBSERVATION_ID)
                        .from(OBSERVATION_PARAMETERS)
                        .where(propertyValue);

                return OBSERVATION.OBSERVATION_ID.in(subquery);

            } else {
                throw new STAInvalidFilterExpressionException("Could not find related property: " + propertyName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Field checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ID:
                return OBSERVATION.STA_IDENTIFIER;

            case StaConstants.PROP_PHENOMENON_TIME:
                // TODO: proper ISO8601 comparison
                return OBSERVATION.SAMPLING_TIME_END;

            /* TODO: This is handled separately as result is split up over multiple columns */
            case StaConstants.PROP_RESULT:
                return null;
            case StaConstants.PROP_RESULT_TIME:
                return OBSERVATION.RESULT_TIME;
            case StaConstants.PROP_VALID_TIME:
                return OBSERVATION.VALID_TIME_START;
            case StaConstants.PROP_PARAMETERS:
                // TODO:
                return null;
            default:
                return null;
        }
    }
}
