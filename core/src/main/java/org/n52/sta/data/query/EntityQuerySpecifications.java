/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.query;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Date;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */

public abstract class EntityQuerySpecifications<T> {

    protected static final String SENSOR = "Sensor";
    protected static final String OBSERVED_PROPERTY = "ObservedProperty";
    protected static final String THING = "Thing";
    protected static final String THINGS = "Things";
    protected static final String DATASTREAM = "Datastream";
    protected static final String DATASTREAMS = "Datastreams";
    protected static final String FEATUREOFINTEREST = "FeatureOfInterest";
    protected static final String LOCATIONS = "Locations";
    protected static final String HISTORICAL_LOCATIONS = "HistoricalLocations";
    protected static final String OBSERVATIONS = "Observations";

    private static final String ERROR_TEMPLATE = "Operator \"%s\" is not supported for given arguments.";

    /**
     * Gets Subquery returning the IDs of the Entities
     *
     * @param filter BooleanExpression filtering the Entites whose IDs are returned
     * @return Specification Subquery
     */
    public abstract Specification<String> getIdSubqueryWithFilter(Specification filter);

    /**
     * Gets Entity-specific Filter for property with given name. Filters may not accept all BinaryOperators,
     * as they may not be defined for the datatype of the property.
     *
     * @param propertyName  Name of the property to be filtered on
     * @param propertyValue supposed Value of the property
     * @param operator      Operator to be used for comparing propertyValue and actual Value
     * @param switched      true if Expression adheres to template: value, operator, name. False otherwise (Template
     *                      name, operator, value)
     * @return Specification evaluating to true if Entity is not to be filtered out
     * @throws STAInvalidFilterExpressionException if an error occurs
     */
    public abstract Specification<T> getFilterForProperty(String propertyName,
                                                          Object propertyValue,
                                                          FilterConstants.ComparisonOperator operator,
                                                          boolean switched)
            throws STAInvalidFilterExpressionException;

    public Specification<T> withName(final String name) {
        return (root, query, builder) -> {
            return builder.equal(root.get(DescribableEntity.PROPERTY_NAME), name);
        };
    }

    public Specification<T> withIdentifier(final String name) {
        return (root, query, builder) -> {
            return builder.equal(root.get(DescribableEntity.PROPERTY_IDENTIFIER), name);
        };
    }

    protected Specification<String> toSubquery(Class<?> clazz, String property, Specification filter) {
        return (root, query, builder) -> {
            Subquery<?> sq = query.subquery(clazz);
            Root<?> from = sq.from(clazz);
            sq.select(from.get(property)).where(filter.toPredicate(root, query, builder));
            return builder.in(root.get(DescribableEntity.PROPERTY_IDENTIFIER)).value(sq);
        };
    }

    // Wrapper
    protected Predicate handleDirectStringPropertyFilter(Path<String> stringPath,
                                                         String propertyValue,
                                                         FilterConstants.ComparisonOperator operator,
                                                         CriteriaBuilder builder,
                                                         boolean switched)
            throws STAInvalidFilterExpressionException {
        return this.handleStringFilter(stringPath, propertyValue, operator, builder, switched);
    }

    protected Predicate handleDirectNumberPropertyFilter(Path<Double> numberPath,
                                                         Double propertyValue,
                                                         FilterConstants.ComparisonOperator operator,
                                                         CriteriaBuilder builder)
            throws STAInvalidFilterExpressionException {
        return this.handleNumberFilter(numberPath, propertyValue, operator, builder);
    }

    protected Predicate handleDirectNumberPropertyFilter(Path<Long> numberPath,
                                                         Long propertyValue,
                                                         FilterConstants.ComparisonOperator operator,
                                                         CriteriaBuilder builder)
            throws STAInvalidFilterExpressionException {
        return this.handleNumberFilter(numberPath, propertyValue, operator, builder);
    }

    protected Predicate handleDirectNumberPropertyFilter(Path<Integer> numberPath,
                                                         Integer propertyValue,
                                                         FilterConstants.ComparisonOperator operator,
                                                         CriteriaBuilder builder)
            throws STAInvalidFilterExpressionException {
        return this.handleNumberFilter(numberPath, propertyValue, operator, builder);
    }

    protected Predicate handleDirectDateTimePropertyFilter(Path<Date> time,
                                                           Date propertyValue,
                                                           FilterConstants.ComparisonOperator operator,
                                                           CriteriaBuilder builder)
            throws STAInvalidFilterExpressionException {
        return this.handleDateFilter(time, propertyValue, operator, builder);
    }

    public Predicate handleStringFilter(Path<String> left,
                                        String right,
                                        FilterConstants.ComparisonOperator operatorKind,
                                        CriteriaBuilder builder,
                                        boolean switched)
            throws STAInvalidFilterExpressionException {
        FilterConstants.ComparisonOperator operator = switched ? reverseOperator(operatorKind) : operatorKind;

        switch (operator) {
        case PropertyIsEqualTo:
            return builder.equal(left, right);
        case PropertyIsNotEqualTo:
            return builder.notEqual(left, right);
        case PropertyIsLessThan:
            return builder.lessThan(left, right);
        case PropertyIsLessThanOrEqualTo:
            return builder.lessThanOrEqualTo(left, right);
        case PropertyIsGreaterThan:
            return builder.greaterThan(left, right);
        case PropertyIsGreaterThanOrEqualTo:
            return builder.greaterThanOrEqualTo(left, right);
        default:
            throw new STAInvalidFilterExpressionException("Error getting filter. Invalid Operator");
        }
    }

    public Predicate handleNumberFilter(Expression<Double> left,
                                        Double right,
                                        FilterConstants.ComparisonOperator operator,
                                        CriteriaBuilder builder)
            throws STAInvalidFilterExpressionException {

        switch (operator) {
        case PropertyIsEqualTo:
            return builder.equal(left, right);
        case PropertyIsNotEqualTo:
            return builder.notEqual(left, right);
        case PropertyIsLessThan:
            return builder.lessThan(left, right);
        case PropertyIsLessThanOrEqualTo:
            return builder.lessThanOrEqualTo(left, right);
        case PropertyIsGreaterThan:
            return builder.greaterThan(left, right);
        case PropertyIsGreaterThanOrEqualTo:
            return builder.greaterThanOrEqualTo(left, right);
        // case ADD:
        //    return builder.sum(leftExpr, rightExpr);
        // case DIV:
        //    return builder.quot(leftExpr, rightExpr);
        // case MOD:
        //    return builder.mod(leftExpr.as(Integer.class), rightExpr.as(Integer.class));
        // case MUL:
        //    return builder.prod(leftExpr, rightExpr);
        // case SUB:
        //    return builder.diff(leftExpr, rightExpr);
        default:
            throw new STAInvalidFilterExpressionException(
                    String.format(ERROR_TEMPLATE, operator.toString()));
        }
    }

    public Predicate handleNumberFilter(Expression<Long> left,
                                        Long right,
                                        FilterConstants.ComparisonOperator operator,
                                        CriteriaBuilder builder)
            throws STAInvalidFilterExpressionException {

        switch (operator) {
        case PropertyIsEqualTo:
            return builder.equal(left, right);
        case PropertyIsNotEqualTo:
            return builder.notEqual(left, right);
        case PropertyIsLessThan:
            return builder.lessThan(left, right);
        case PropertyIsLessThanOrEqualTo:
            return builder.lessThanOrEqualTo(left, right);
        case PropertyIsGreaterThan:
            return builder.greaterThan(left, right);
        case PropertyIsGreaterThanOrEqualTo:
            return builder.greaterThanOrEqualTo(left, right);
        // case ADD:
        // return builder.sum(leftExpr, rightExpr);
        // case DIV:
        // return builder.quot(leftExpr, rightExpr);
        // case MOD:
        // return builder.mod(leftExpr.as(Integer.class),
        // rightExpr.as(Integer.class));
        // case MUL:
        // return builder.prod(leftExpr, rightExpr);
        // case SUB:
        // return builder.diff(leftExpr, rightExpr);
        default:
            throw new STAInvalidFilterExpressionException(
                    String.format(ERROR_TEMPLATE, operator.toString()));
        }
    }

    public Predicate handleNumberFilter(Expression<Integer> left,
                                        Integer right,
                                        FilterConstants.ComparisonOperator operator,
                                        CriteriaBuilder builder)
            throws STAInvalidFilterExpressionException {

        switch (operator) {
        case PropertyIsEqualTo:
            return builder.equal(left, right);
        case PropertyIsNotEqualTo:
            return builder.notEqual(left, right);
        case PropertyIsLessThan:
            return builder.lessThan(left, right);
        case PropertyIsLessThanOrEqualTo:
            return builder.lessThanOrEqualTo(left, right);
        case PropertyIsGreaterThan:
            return builder.greaterThan(left, right);
        case PropertyIsGreaterThanOrEqualTo:
            return builder.greaterThanOrEqualTo(left, right);
        default:
            throw new STAInvalidFilterExpressionException(
                    String.format(ERROR_TEMPLATE, operator.toString()));
        }
    }

    public Predicate handleDateFilter(Expression<Date> left,
                                      Date right,
                                      FilterConstants.ComparisonOperator operator,
                                      CriteriaBuilder builder)
            throws STAInvalidFilterExpressionException {

        switch (operator) {
        case PropertyIsEqualTo:
            return builder.equal(left, right);
        case PropertyIsNotEqualTo:
            return builder.notEqual(left, right);
        case PropertyIsLessThan:
            return builder.lessThan(left, right);
        case PropertyIsLessThanOrEqualTo:
            return builder.lessThanOrEqualTo(left, right);
        case PropertyIsGreaterThan:
            return builder.greaterThan(left, right);
        case PropertyIsGreaterThanOrEqualTo:
            return builder.greaterThanOrEqualTo(left, right);
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
}
