/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.DescribableEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public abstract class EntityQuerySpecifications<T> {
    final static DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    final static ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();

//    final static QDatastreamEntity qdatastream = QDatastreamEntity.datastreamEntity;
//    final static QLocationEntity qlocation = QLocationEntity.locationEntity;
//    final static QHistoricalLocationEntity qhistoricallocation = QHistoricalLocationEntity.historicalLocationEntity;
//    final static QProcedureEntity qsensor = QProcedureEntity.procedureEntity;
//    final static QDataEntity qobservation = QDataEntity.dataEntity;
//    final static QAbstractFeatureEntity qfeature = QAbstractFeatureEntity.abstractFeatureEntity;
//    final static QDatasetEntity qdataset = QDatasetEntity.datasetEntity;
//    // aka Thing
//    final static QPlatformEntity qPlatform = QPlatformEntity.platformEntity;
//    final static QPhenomenonEntity qobservedproperty = QPhenomenonEntity.phenomenonEntity;

    /**
     * Gets Subquery returning the IDs of the Entities
     * 
     * @param filter
     *        BooleanExpression filtering the Entites whose IDs are returned
     * @return JPQLQuery<Long> Subquery
     */
    public abstract Subquery<Long> getIdSubqueryWithFilter(Expression<Boolean> filter);

    /**
     * Gets Entity-specific Filter for property with given name. Filters may not accept all BinaryOperators,
     * as they may not be defined for the datatype of the property.
     * 
     * @param propertyName
     *        Name of the property to be filtered on
     * @param propertyValue
     *        supposed Value of the property
     * @param operator
     *        Operator to be used for comparing propertyValue and actual Value
     * @param switched
     *        true if Expression adheres to template: <value> <operator> <name>. False otherwise (Template
     *        <name> <operator> <value>)
     * @return BooleanExpression evaluating to true if Entity is not to be filtered out
     */
    public abstract Object getFilterForProperty(String propertyName,
                                                Object propertyValue,
                                                BinaryOperatorKind operator,
                                                boolean switched)
            throws ExpressionVisitException;
    
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
    
    public Specification<T> withId(final Long id) {
        return (root, query, builder) -> {
            return builder.equal(root.get(DescribableEntity.PROPERTY_ID), id);
        };
    }

    // TODO:JavaDoc
//    protected Subquery<Long> toSubquery(Path<T> relation, Expression<Long> selector, Expression<Boolean> filter) {
//        return JPAExpressions.selectFrom(relation)
//                             .select(selector)
//                             .where(filter);
//    }
    
    private Subquery<Long> toSubquery(Class<?> clazz, String propertyId, Expression<Boolean> filter) {
        // TODO Auto-generated method stub
        return null;
    }

    protected String toStringExpression(Object expr) throws ExpressionVisitException {
        if (expr instanceof String) {
            return (String) expr;
        } else 
        if (expr instanceof Expression) {
            return (String) expr;
        } else {
            throw new ExpressionVisitException("Error converting Argument to Expression<String>");
        }
    }

    @SuppressWarnings("unchecked")
    protected Expression<Number> toNumberExpression(Object expr) throws ExpressionVisitException {
//        if (expr instanceof String) {
//            return Expressions.asNumber((Double) expr);
//        } else 
        if (expr instanceof Expression<?>) {
            return ((Expression) expr).as(Number.class);
        } else {
            throw new ExpressionVisitException("Error converting Argument to NumberExpression");
        }
    }

    @SuppressWarnings("unchecked")
    protected Expression<Date> toDateTimeExpression(Object expr) throws ExpressionVisitException {
//        if (expr instanceof String) {
//            // TODO: check if this fails
//            return Expressions.asDateTime((Date) expr);
//        } else 
        if (expr instanceof Expression<?>) {
            return (Expression<Date>) expr;
        } else {
            throw new ExpressionVisitException("Error converting Argument to NumberExpression");
        }
    }

    // Wrapper
    protected Predicate handleDirectStringPropertyFilter(Path<String> stringPath,
                                                      Object propertyValue,
                                                      BinaryOperatorKind operator,
                                                      CriteriaBuilder builder,
                                                      boolean switched)
            throws ExpressionVisitException {
        return this.handleStringFilter(stringPath, toStringExpression(propertyValue), operator, builder, switched);
    }

    protected Object handleDirectNumberPropertyFilter(Path<Number> numberPath,
                                                      Object propertyValue,
                                                      BinaryOperatorKind operator,
                                                      CriteriaBuilder builder,
                                                      boolean switched)
            throws ExpressionVisitException {
        return this.handleNumberFilter(numberPath, toNumberExpression(propertyValue), operator, builder, switched);
    }

    protected Predicate handleDirectDateTimePropertyFilter(Path<Date> time,
                                                        Object propertyValue,
                                                        BinaryOperatorKind operator,
                                                        CriteriaBuilder builder,
                                                        boolean switched)
            throws ExpressionVisitException {
        return this.handleDateFilter(time, toDateTimeExpression(propertyValue), operator, builder, switched);
    }

    public Predicate handleStringFilter(Path<String> left,
                                     String right,
                                     BinaryOperatorKind operator,
                                     CriteriaBuilder builder,
                                     boolean switched)
            throws ExpressionVisitException {
        operator = (switched) ? reverseOperator(operator) : operator;

        switch (operator) {
        case EQ:
            return builder.equal(left, right);
        case NE:
            return builder.notEqual(left, right);
        case LT:
            return builder.lessThan(left, right);
        case LE:
            return builder.lessThanOrEqualTo(left, right);
        case GT:
            return builder.greaterThan(left, right);
        case GE:
            return builder.greaterThanOrEqualTo(left, right);
        default:
            throw new ExpressionVisitException("Error getting filter. Invalid Operator");
        }
    }

    public Object handleNumberFilter(Expression<Number> left,
                                     Expression<Number> right,
                                     BinaryOperatorKind operator,
                                     CriteriaBuilder builder,
                                     boolean switched)
            throws ExpressionVisitException {
        Expression<Number> leftExpr;
        Expression<Number> rightExpr;
        if (switched) {
            leftExpr = right;
            rightExpr = left;
        } else {
            leftExpr = left;
            rightExpr = right;
        }

        switch (operator) {
        case EQ:
            return builder.equal(leftExpr, right);
        case NE:
            return builder.notEqual(leftExpr, rightExpr);
        case LT:
            return builder.lessThan(leftExpr.as(Comparable.class), rightExpr.as(Comparable.class));
        case LE:
            return builder.lessThanOrEqualTo(leftExpr.as(Comparable.class), rightExpr.as(Comparable.class));
        case GT:
            return builder.greaterThan(leftExpr.as(Comparable.class), rightExpr.as(Comparable.class));
        case GE:
            return builder.greaterThanOrEqualTo(leftExpr.as(Comparable.class), rightExpr.as(Comparable.class));
        case ADD:
            return builder.sum(leftExpr, rightExpr);
        case DIV:
            return builder.quot(leftExpr, rightExpr);
        case MOD:
            return builder.mod(leftExpr.as(Integer.class), rightExpr.as(Integer.class));
        case MUL:
            return builder.prod(leftExpr, rightExpr);
        case SUB:
            return builder.diff(leftExpr, rightExpr);
        default:
            throw new ExpressionVisitException("Operator \"" + operator.toString()
                    + "\" is not supported for given arguments.");
        }
    }

    public Predicate handleDateFilter(Expression<Date> left,
                                   Expression<Date> right,
                                   BinaryOperatorKind operator,
                                   CriteriaBuilder builder,
                                   boolean switched)
            throws ExpressionVisitException {
        Expression<Date> leftExpr;
        Expression<Date> rightExpr;
        if (switched) {
            leftExpr = right;
            rightExpr = left;
        } else {
            leftExpr = left;
            rightExpr = right;
        }

        switch (operator) {
        case EQ:
            return builder.equal(leftExpr, rightExpr);
        case NE:
            return builder.notEqual(leftExpr, rightExpr);
        case LT:
            return builder.lessThan(leftExpr, rightExpr);
        case LE:
            return builder.lessThanOrEqualTo(leftExpr, rightExpr);
        case GT:
            return builder.greaterThan(leftExpr, rightExpr);
        case GE:
            return builder.greaterThanOrEqualTo(leftExpr, rightExpr);
        default:
            throw new ExpressionVisitException("Operator \"" + operator.toString()
                    + "\" is not supported for DateTimeExpression");
        }
    }

    /**
     * Reverses an operator if this operator is easily revertible.
     * 
     * @param operator
     *        to be reversed
     * @return String representation of reversed Operator
     */
    private BinaryOperatorKind reverseOperator(BinaryOperatorKind operator) {
        switch (operator) {
        case LT:
            return BinaryOperatorKind.GE;
        case LE:
            return BinaryOperatorKind.GT;
        case GT:
            return BinaryOperatorKind.LE;
        case GE:
            return BinaryOperatorKind.LT;
        default:
            return operator;
        }
    }
}
