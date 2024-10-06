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

import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;

import org.jooq.*;
import org.jooq.impl.DSL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */

@Configurable
public abstract class EntityQueryConditions implements StaEntity {

    String COULD_NOT_FIND_RELATED_PROPERTY = "Could not find related property: ";
    String ERROR_GETTING_FILTER_NO_PROP = "Error getting filter for Property: '%s'. No such " +
            "property in Entity.";
    String ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE =
            "Error getting filter for Property: '%s'. No such property with type %s in Entity.";
    String ERROR_TEMPLATE = "Operator \"%s\" is not supported for given arguments.";
    String INVALID_DATATYPE_CANNOT_CAST = "Invalid Datatypes found. Cannot cast ";
    String ERROR_INVALID_PARAMETER_ENTITY_TYPE = "Error getting entity from '%s'. No such parameter entity found";

    @Autowired
    protected DSLContext ctx;
    /**
     * Used for testing
     *
     * @param ctx jOOQ DSLContext object
     */
    public void setDslContext(DSLContext ctx) {
        this.ctx = ctx;
    }

    public abstract Condition withStaIdentifier(final String staIdentifier);

    public abstract Condition withStaIdentifier(final List<String> identifiers);


    /**
     * Gets Entity-specific Filter for relation with given name.
     *
     * @param propertyName  Name of the relation to be filtered on
     * @param propertyValue supposed Value of the property
     * @return Condition evaluating to true if Entity is not to be filtered out
     * @throws STAInvalidFilterExpressionException if an error occurs
     */
    public Condition getFilterForRelation(String propertyName,
                                                 Condition propertyValue)
            throws STAInvalidFilterExpressionException {
        return handleRelatedPropertyFilter(propertyName, propertyValue);
    }

    /**
     * Gets Entity-specific Filter for property with given name. Filters may not accept all BinaryOperators,
     * as they may not be defined for the datatype of the property.
     *
     * @param propertyName  Name of the property to be filtered on
     * @param propertyValue supposed Value of the property
     * @param operator      Operator to be used for comparing propertyValue and actual Value
     * @param switched      true if Expression adheres to template: value, operator, name. False otherwise (Template
     *                      name, operator, value)
     * @return Condition evaluating to true if Entity is not to be filtered out
     * @throws STAInvalidFilterExpressionException if an error occurs
     */
    public <T extends Comparable<? super T>> Condition getFilterForProperty(String propertyName,
                                         Field<T> propertyValue,
                                         FilterConstants.ComparisonOperator operator,
                                         boolean switched)
            throws STAInvalidFilterExpressionException {
        return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
    }

    protected abstract <T extends Comparable<? super T>> Condition handleDirectPropertyFilter(String propertyName,
                                                            Field<T> propertyValue,
                                                            FilterConstants.ComparisonOperator operator,
                                                            boolean switched);

    protected abstract Condition handleRelatedPropertyFilter(String propertyName, Condition propertyValue);

    protected <T extends Comparable<? super T>> Condition handleDirectStringPropertyFilter(
            Field<String> stringField,
            Field <?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched)
            throws STAInvalidFilterExpressionException {
        if (propertyValue.getDataType().getType().equals(String.class)) {
            return this.handleStringFilter(
                    stringField,
                    (Field<String>) propertyValue,
                    operator,
                    switched);
        } else {
            throw new STAInvalidFilterExpressionException(
                    INVALID_DATATYPE_CANNOT_CAST + propertyValue.getDataType().getType() + " to String.class");
        }
    }


    protected <T extends Comparable<? super T>> Condition handleDirectNumberPropertyFilter(
            Field<T> numericField,
            Field <?> propertyValue,
            FilterConstants.ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {

        if (Number.class.isAssignableFrom(numericField.getDataType().getType()) &&
                Number.class.isAssignableFrom(propertyValue.getDataType().getType())) {
            return this.handleComparableFilter(
                    numericField,
                    (Field<T>) propertyValue,
                    operator);
        } else {
            throw new STAInvalidFilterExpressionException(
                    INVALID_DATATYPE_CANNOT_CAST + propertyValue.getDataType().getType() + " to Number.class");
        }
    }


    protected <T extends Comparable<? super T>> Condition handleDirectDateTimePropertyFilter(
            Field<T> timeField,
            Field <?> propertyValue,
            FilterConstants.ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {

        if (Date.class.isAssignableFrom(propertyValue.getDataType().getType())) {
            return this.handleComparableFilter(
                    timeField,
                    (Field<T>) propertyValue,
                    operator
            );
        } else {
            throw new STAInvalidFilterExpressionException(
                    INVALID_DATATYPE_CANNOT_CAST + propertyValue.getDataType().getType() + " to Date.class");
        }
    }

    private Condition handleStringFilter(Field<String> left,
                                         Field<String> right,
                                         FilterConstants.ComparisonOperator operatorKind,
                                         boolean switched)
            throws STAInvalidFilterExpressionException {
        FilterConstants.ComparisonOperator operator = switched ? reverseOperator(operatorKind) : operatorKind;
        return handleComparableFilter(left, right, operator);
    }

    private <T extends Comparable<? super T>> Condition handleComparableFilter(
            Field <T> left,
            Field <T> right,
            FilterConstants.ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {

        switch (operator) {
            case PropertyIsEqualTo:
                return left.eq(right);
            case PropertyIsNotEqualTo:
                return left.ne(right);
            case PropertyIsLessThan:
                return left.lt(right);
            case PropertyIsLessThanOrEqualTo:
                return left.le(right);
            case PropertyIsGreaterThan:
                return left.gt(right);
            case PropertyIsGreaterThanOrEqualTo:
                return left.ge(right);
            default:
                throw new STAInvalidFilterExpressionException(
                        String.format(ERROR_TEMPLATE, operator));
        }
    }
    /**
     * Reverses an operator if this operator is easily revertible.
     *
     * @param operator to be reversed
     * @return String representation of reversed Operator
     */

    private FilterConstants.ComparisonOperator reverseOperator(FilterConstants.ComparisonOperator operator) {
        switch (operator) {
            case PropertyIsLessThan:
                return FilterConstants.ComparisonOperator.PropertyIsGreaterThanOrEqualTo;
            case PropertyIsLessThanOrEqualTo:
                return FilterConstants.ComparisonOperator.PropertyIsGreaterThan;
            case PropertyIsGreaterThan:
                return FilterConstants.ComparisonOperator.PropertyIsLessThanOrEqualTo;
            case PropertyIsGreaterThanOrEqualTo:
                return FilterConstants.ComparisonOperator.PropertyIsLessThan;
            default:
                return operator;
        }
    }


    protected <T extends Comparable<? super T>> Condition handleProperties(
            String propertyName,
            Field<T> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched,
            Field<Long> referenceField,
            ParameterFactory.EntityType entityType)
            throws STAInvalidFilterExpressionException {

        String key = propertyName.substring(StaConstants.PROP_PROPERTIES.length() + 1);
        if (propertyValue.getDataType().getType().equals(String.class)) {

            Table<?> table = getParameterTable(entityType);
            Field<Long> entityId = getEntityId(entityType);

            if (table == null || entityId == null) {
                // handle exception
                throw new STAInvalidFilterExpressionException(
                        String.format(ERROR_INVALID_PARAMETER_ENTITY_TYPE, entityType));
            }

            // value could also be: value_json,value_xml,value_category,value_text, value_count, value_quantity,etc
            Field<String> valueField = DSL.field(DSL.name(table.getName(), "VALUE_TEXT"), String.class);

            // Build the subquery condition
            Condition subqueryCondition = DSL.field(DSL.name(table.getName(), "NAME")).eq(DSL.val(key))
                    .and(handleDirectStringPropertyFilter(valueField, propertyValue, operator, switched));

            // Build the subquery
            SelectConditionStep<? extends Record1<Long>> subquery = ctx
                    .select(referenceField)
                    .from(table)
                    .where(subqueryCondition);

            // Main query condition
            return entityId.in(subquery);

        } else {
            throw new STAInvalidFilterExpressionException(
                    String.format(ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE, key, "String"));
        }
    }
    /**
     * Translate STA property name to Database property name
     *
     * @param property name of the property in STA
     * @return name of the property in database
     */

    public abstract Field<?> checkPropertyName(String property);

    private Field<Long> getEntityId(ParameterFactory.EntityType entityType) {
        switch (entityType) {
            case PHENOMENON:
                return OBSERVED_PROPERTY.PHENOMENON_ID;
            case PROCEDURE:
                return SENSOR.PROCEDURE_ID;
            case PLATFORM:
                return THING.PLATFORM_ID;
            case DATASET:
                return DATASTREAM.DATASET_ID;
            case FEATURE:
                return FEATURE_OF_INTEREST.FEATURE_ID;
            case OBSERVATION:
                return OBSERVATION.OBSERVATION_ID;
            case LOCATION:
                return LOCATION.LOCATION_ID;
            default:
                return null;
        }
    }
    Table<?> getParameterTable(ParameterFactory.EntityType entityType) {
        switch (entityType) {
            case PHENOMENON:
                return OBSERVED_PROPERTY_PROPERTIES;
            case PROCEDURE:
                return SENSOR_PROPERTIES;
            case PLATFORM:
                return THING_PROPERTIES;
            case DATASET:
                return DATASTREAM_PROPERTIES;
            case FEATURE:
                return FEATURE_PROPERTIES;
            case OBSERVATION:
                return OBSERVATION_PARAMETERS;
            case LOCATION:
                return LOCATION_PROPERTIES;
            default:
                return null;
        }
    }
}
