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
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
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
public abstract class EntityQueryConditions implements EntityQueryConstants {

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

    protected abstract Condition handleRelatedPropertyFilter(String propertyName,
                                                                    Condition propertyValue);

    public Condition withName(final String name) {
        return DSL.field(STA_NAME_FIELD).equal(name);
    }

    public Condition withStaIdentifier(final String name) {
        return DSL.field(STA_IDENTIFIER_FIELD).equal(name);
    }

    public Condition withStaIdentifier(final List<String> identifiers) {
        return DSL.field(STA_IDENTIFIER_FIELD).in(identifiers);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Comparable<? super T>> Condition handleDirectStringPropertyFilter(
            Field<String> stringField,
            Field <T> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched)
            throws STAInvalidFilterExpressionException {
        if (propertyValue.getDataType().getType().equals(String.class)) {
            return this.handleStringFilter(stringField,
                    propertyValue.cast(String.class),
                    operator,
                    switched);
        } else {
            throw new STAInvalidFilterExpressionException(
                    INVALID_DATATYPE_CANNOT_CAST + propertyValue.getDataType().getType() + " to String.class");
        }
    }


    @SuppressWarnings("unchecked")
    protected <T extends Comparable<? super T>> Condition handleDirectNumberPropertyFilter(
            Field<Double> numberField,
            Field <T> propertyValue,
            FilterConstants.ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {

        if (Number.class.isAssignableFrom(numberField.getDataType().getType()) &&
                Number.class.isAssignableFrom(propertyValue.getDataType().getType())) {
            return this.handleComparableFilter(numberField,
                    propertyValue.cast(Double.class),
                    operator);
        } else {
            throw new STAInvalidFilterExpressionException(
                    INVALID_DATATYPE_CANNOT_CAST + propertyValue.getDataType().getType() + " to Number.class");
        }
    }


    @SuppressWarnings("unchecked")
    protected <T extends Comparable<? super T>> Condition handleDirectDateTimePropertyFilter(
            Field<Date> timeField,
            Field <T> propertyValue,
            FilterConstants.ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {

        if (Date.class.isAssignableFrom(propertyValue.getDataType().getType())) {
            return this.handleComparableFilter(timeField,
                    propertyValue.cast(Date.class),
                    operator);
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
                        String.format(ERROR_TEMPLATE, operator.toString()));
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
            String referenceField,
            ParameterFactory.EntityType entityType)
            throws STAInvalidFilterExpressionException {

        String key = propertyName.substring(11);
        if (propertyValue.getDataType().getType().equals(String.class)) {

            String tableName = getParameterTableName(entityType);
            String entityId = getEntityId(entityType);

            if (tableName == null || entityId == null) {
                // handle exception
                throw new STAInvalidFilterExpressionException(
                        String.format(ERROR_INVALID_PARAMETER_ENTITY_TYPE, entityType));
            }

            // value could also be: value_json,value_xml,value_category,value_text, value_count, value_quantity,etc
            Field<String> valueField = DSL.field(PARAMETER_VALUE_TEXT, String.class);

            // Build the subquery condition
            Condition subqueryCondition = DSL.field(STA_NAME_FIELD).eq(DSL.val(key))
                    .and(handleDirectStringPropertyFilter(valueField, propertyValue, operator, switched));

            // Build the subquery
            SelectConditionStep<Record1<Object>> subquery = DSL.select(DSL.field(referenceField))
                    .from(DSL.table(tableName))
                    .where(subqueryCondition);

            // Main query condition
            return DSL.field(entityId).in(subquery);

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

    public String checkPropertyName(String property) {
        return property;
    }
}
