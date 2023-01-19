/*
 * Copyright (C) 2018-2023 52°North Initiative for Geospatial Open Source
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
import org.n52.series.db.beans.HibernateRelations;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Date;
import java.util.List;

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
    protected static final String COULD_NOT_FIND_RELATED_PROPERTY = "Could not find related property: ";
    protected static final String ERROR_GETTING_FILTER_NO_PROP = "Error getting filter for Property: '%s'. No such " +
        "property in Entity.";
    protected static final String ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE =
        "Error getting filter for Property: '%s'. No such property with type %s in Entity.";

    private static final String ERROR_TEMPLATE = "Operator \"%s\" is not supported for given arguments.";
    private static final String INVALID_DATATYPE_CANNOT_CAST = "Invalid Datatypes found. Cannot cast ";

    /**
     * Gets Entity-specific Filter for relation with given name.
     *
     * @param propertyName  Name of the relation to be filtered on
     * @param propertyValue supposed Value of the property
     * @return Specification evaluating to true if Entity is not to be filtered out
     * @throws STAInvalidFilterExpressionException if an error occurs
     */
    public Specification<T> getFilterForRelation(String propertyName,
                                                 Specification<?> propertyValue)
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
     * @return Specification evaluating to true if Entity is not to be filtered out
     * @throws STAInvalidFilterExpressionException if an error occurs
     */
    public Specification<T> getFilterForProperty(String propertyName,
                                                 Expression<?> propertyValue,
                                                 FilterConstants.ComparisonOperator operator,
                                                 boolean switched)
        throws STAInvalidFilterExpressionException {
        return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
    }

    public Specification<T> withName(final String name) {
        return (root, query, builder) -> builder.equal(root.get(DescribableEntity.PROPERTY_NAME), name);
    }

    public Specification<T> withStaIdentifier(final String name) {
        return (root, query, builder) -> builder.equal(root.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), name);
    }

    public Specification<T> withStaIdentifier(final List<String> identifiers) {
        return (root, query, builder) -> builder.in(root.get(DescribableEntity.PROPERTY_STA_IDENTIFIER))
            .value(identifiers);
    }

    // Wrapper
    @SuppressWarnings("unchecked")
    protected Predicate handleDirectStringPropertyFilter(Path<String> stringPath,
                                                         Expression<?> propertyValue,
                                                         FilterConstants.ComparisonOperator operator,
                                                         CriteriaBuilder builder,
                                                         boolean switched)
        throws STAInvalidFilterExpressionException {
        if (propertyValue.getJavaType().equals(String.class)) {
            return this.handleStringFilter(stringPath, (Expression<String>) propertyValue, operator, builder, switched);
        } else {
            throw new STAInvalidFilterExpressionException(
                INVALID_DATATYPE_CANNOT_CAST + propertyValue.getJavaType() + " to String.class");
        }
    }

    @SuppressWarnings("unchecked")
    protected <K extends Comparable<? super K>> Predicate handleDirectNumberPropertyFilter(
        Path<K> numberPath,
        Expression<?> propertyValue,
        FilterConstants.ComparisonOperator operator,
        CriteriaBuilder builder)
        throws STAInvalidFilterExpressionException {
        return this.handleComparableFilter(numberPath, (Expression<K>) propertyValue, operator, builder);
    }

    @SuppressWarnings("unchecked")
    protected Predicate handleDirectDateTimePropertyFilter(Path<Date> time,
                                                           Expression<?> propertyValue,
                                                           FilterConstants.ComparisonOperator operator,
                                                           CriteriaBuilder builder)
        throws STAInvalidFilterExpressionException {
        if (propertyValue.getJavaType().equals(Date.class)) {
            return this.handleComparableFilter(time, (Expression<Date>) propertyValue, operator, builder);
        } else {
            throw new STAInvalidFilterExpressionException(
                INVALID_DATATYPE_CANNOT_CAST + propertyValue.getJavaType() + " to Date.class");
        }
    }

    private Predicate handleStringFilter(Path<String> left,
                                         Expression<String> right,
                                         FilterConstants.ComparisonOperator operatorKind,
                                         CriteriaBuilder builder,
                                         boolean switched)
        throws STAInvalidFilterExpressionException {
        FilterConstants.ComparisonOperator operator = switched ? reverseOperator(operatorKind) : operatorKind;
        return handleComparableFilter(left, right, operator, builder);
    }

    private <K extends Comparable<? super K>> Predicate handleComparableFilter(
        Expression<K> left,
        Expression<K> right,
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

    protected abstract Specification<T> handleRelatedPropertyFilter(String propertyName,
                                                                    Specification<?> propertyValue);

    protected abstract Specification<T> handleDirectPropertyFilter(String propertyName,
                                                                   Expression<?> propertyValue,
                                                                   FilterConstants.ComparisonOperator operator,
                                                                   boolean switched);

    protected Predicate handleProperties(Root<?> root,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder builder,
                                         String propertyName,
                                         Expression<?> propertyValue,
                                         FilterConstants.ComparisonOperator operator,
                                         boolean switched,
                                         String referenceName,
                                         ParameterFactory.EntityType entityType)
        throws STAInvalidFilterExpressionException {
        String key = propertyName.substring(11);
        if (propertyValue.getJavaType().isAssignableFrom(String.class)) {
            Class<? extends ParameterEntity> clazz =
                ParameterFactory.from(entityType, ParameterFactory.ValueType.TEXT).getClass();
            Subquery<?> subquery = query.subquery(clazz);
            Root<?> param = subquery.from(clazz);
            subquery.select(param.get(referenceName))
                .where(builder.and(
                    builder.equal(param.get(ParameterEntity.NAME), key),
                    handleDirectStringPropertyFilter(param.get(HibernateRelations.HasValue.VALUE),
                                                     propertyValue,
                                                     operator,
                                                     builder,
                                                     switched))
                );
            return builder.in(root.get(DescribableEntity.PROPERTY_ID)).value(subquery);
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
