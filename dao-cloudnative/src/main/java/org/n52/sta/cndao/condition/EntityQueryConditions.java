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
package org.n52.sta.data.cndao.condition;
import org.jooq.impl.SQLDataType;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.dataset.DatasetParameterEntity;
import org.n52.series.db.beans.parameter.feature.FeatureParameterEntity;
import org.n52.series.db.beans.parameter.location.LocationParameterEntity;
import org.n52.series.db.beans.parameter.observation.ObservationParameterEntity;
import org.n52.series.db.beans.parameter.phenomenon.PhenomenonParameterEntity;
import org.n52.series.db.beans.parameter.platform.PlatformParameterEntity;
import org.n52.series.db.beans.parameter.procedure.ProcedureParameterEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.jooq.*;
import org.jooq.impl.DSL;
import java.util.Date;
import java.util.List;

public abstract class EntityQueryConditions implements EntityQueryConstants {

    protected DSLContext dsl;
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
    public <K extends Comparable<? super K>> Condition getFilterForProperty(String propertyName,
                                         K propertyValue,
                                         FilterConstants.ComparisonOperator operator,
                                         boolean switched)
            throws STAInvalidFilterExpressionException {
        return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
    }

    protected abstract <K extends Comparable<? super K>> Condition handleDirectPropertyFilter(String propertyName,
                                                            K propertyValue,
                                                            FilterConstants.ComparisonOperator operator,
                                                            boolean switched);

    protected abstract Condition handleRelatedPropertyFilter(String propertyName,
                                                                    Condition propertyValue)
            throws STAInvalidFilterExpressionException;

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
    protected <K extends Comparable<? super K>> Condition handleDirectStringPropertyFilter(Field<String> tableField,
                                                         K propertyValue,
                                                         FilterConstants.ComparisonOperator operator,
                                                         boolean switched)
            throws STAInvalidFilterExpressionException {
        if (propertyValue.getClass().equals(String.class)) {
            return this.handleStringFilter(tableField, (String) propertyValue, operator, switched);
        } else {
            throw new STAInvalidFilterExpressionException(
                    INVALID_DATATYPE_CANNOT_CAST + propertyValue.getClass() + " to String.class");
        }
    }


    @SuppressWarnings("unchecked")
    protected <K extends Comparable<? super K>> Condition handleDirectNumberPropertyFilter(
            Field<K> numberField,
            K propertyValue,
            FilterConstants.ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {

        if (Number.class.isAssignableFrom(propertyValue.getClass()) &&
                Number.class.isAssignableFrom(numberField.getDataType().getType())) {
            return this.handleComparableFilter(numberField, propertyValue, operator);
        } else {
            throw new STAInvalidFilterExpressionException(
                    INVALID_DATATYPE_CANNOT_CAST + propertyValue.getClass() + " to Number.class");
        }
    }


    @SuppressWarnings("unchecked")
    protected <K extends Comparable<? super K>> Condition handleDirectDateTimePropertyFilter(Field<Date> time,
                                                           K propertyValue,
                                                           FilterConstants.ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {

        if (propertyValue.getClass().equals(Date.class)) {
            return this.handleComparableFilter(time, (Date) propertyValue, operator);
        } else {
            throw new STAInvalidFilterExpressionException(
                    INVALID_DATATYPE_CANNOT_CAST + propertyValue.getClass() + " to Date.class");
        }
    }

    private Condition handleStringFilter(Field<String> left,
                                         String right,
                                         FilterConstants.ComparisonOperator operatorKind,
                                         boolean switched)
            throws STAInvalidFilterExpressionException {
        FilterConstants.ComparisonOperator operator = switched ? reverseOperator(operatorKind) : operatorKind;
        return handleComparableFilter(left, right, operator);
    }

    private <K extends Comparable<? super K>> Condition handleComparableFilter(
            Field<K> left,
            K right,
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


    protected <K extends Comparable<? super K>> Condition handleProperties(String propertyName,
                                         K propertyValue,
                                         FilterConstants.ComparisonOperator operator,
                                         boolean switched,
                                         String referenceName,
                                         ParameterFactory.EntityType entityType)
            throws STAInvalidFilterExpressionException {
        String key = propertyName.substring(11);
        if (propertyValue.getClass().equals(String.class)) {
            Class<? extends ParameterEntity> paramsEntityClass =
                    ParameterFactory.from(entityType, ParameterFactory.ValueType.TEXT).getClass();

            String tableName = getTableName(paramsEntityClass);
            if (tableName == null) {
                // handle exception
                throw new STAInvalidFilterExpressionException(
                        String.format(ERROR_INVALID_PARAMETER_ENTITY_TYPE, entityType));
            }
            // Define the table and fields
            Table<?> table = DSL.table(DSL.name(tableName)); // unqualified table name; fix it
            // type depends on referenceField type
            Field<Long> referenceField = DSL.field(DSL.name(referenceName), SQLDataType.BIGINT);
            // type depends on nameField type
            Field<String> nameField = DSL.field(DSL.name(ParameterEntity.NAME), SQLDataType.VARCHAR);
            // value is ambiguous here: value_json,xml,category,etc.
            Field<String> valueField = DSL.field(DSL.name(HibernateRelations.HasValue.VALUE), SQLDataType.VARCHAR);
            // cross check if PROPERTY_ID is the correct field name
            Field<Long> idField = DSL.field(DSL.name(DescribableEntity.PROPERTY_ID), SQLDataType.BIGINT);

            // Build the subquery condition
            Condition subqueryCondition = nameField.eq(key)
                    .and(handleDirectStringPropertyFilter(valueField, (String) propertyValue, operator, switched));

            // Build the subquery
            Select<?> subquery = DSL.select(referenceField)
                    .from(table)
                    .where(subqueryCondition);

            // Main query condition
            return idField.in((Select<? extends Record1<Long>>) subquery);

        } else {
            throw new STAInvalidFilterExpressionException(
                    String.format(ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE, key, "String"));
        }
    }

    private String getTableName(Class<? extends ParameterEntity> paramsEntityClass) {
        if ((PlatformParameterEntity.class).isAssignableFrom(paramsEntityClass)) {
            return "platform_parameter";
        }
        else if ((ProcedureParameterEntity.class).isAssignableFrom(paramsEntityClass)) {
            return "procedure_parameter";
        }
        else if ((ObservationParameterEntity.class).isAssignableFrom(paramsEntityClass)) {
            return "observation_parameter";
        }
        else if ((LocationParameterEntity.class).isAssignableFrom(paramsEntityClass)) {
            return "location_parameter";
        }
        else if ((PhenomenonParameterEntity.class).isAssignableFrom(paramsEntityClass)) {
            return "phenomenon_parameter";
        }
        else if ((FeatureParameterEntity.class).isAssignableFrom(paramsEntityClass)) {
            return "feature_parameter";
        }
        else if ((DatasetParameterEntity.class).isAssignableFrom(paramsEntityClass)) {
            return "dataset_parameter";
        }
        return null;
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
