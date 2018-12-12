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

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.QAbstractFeatureEntity;
import org.n52.series.db.beans.QDataEntity;
import org.n52.series.db.beans.QDatasetEntity;
import org.n52.series.db.beans.QPhenomenonEntity;
import org.n52.series.db.beans.QProcedureEntity;
import org.n52.series.db.beans.sta.QDatastreamEntity;
import org.n52.series.db.beans.sta.QHistoricalLocationEntity;
import org.n52.series.db.beans.sta.QLocationEntity;
import org.n52.series.db.beans.sta.QThingEntity;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public abstract class EntityQuerySpecifications<T> {
    final static DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    final static ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();

    final static QDatastreamEntity qdatastream = QDatastreamEntity.datastreamEntity;
    final static QLocationEntity qlocation = QLocationEntity.locationEntity;
    final static QHistoricalLocationEntity qhistoricallocation = QHistoricalLocationEntity.historicalLocationEntity;
    final static QProcedureEntity qsensor = QProcedureEntity.procedureEntity;
    final static QDataEntity qobservation = QDataEntity.dataEntity;
    final static QAbstractFeatureEntity qfeature = QAbstractFeatureEntity.abstractFeatureEntity;
    final static QDatasetEntity qdataset = QDatasetEntity.datasetEntity;
    final static QThingEntity qthing = QThingEntity.thingEntity;
    final static QPhenomenonEntity qobservedproperty = QPhenomenonEntity.phenomenonEntity;

    /**
     * Gets Subquery returning the IDs of the Entities
     * 
     * @param filter
     *        BooleanExpression filtering the Entites whose IDs are returned
     * @return JPQLQuery<Long> Subquery
     */
    public abstract JPQLQuery<Long> getIdSubqueryWithFilter(Expression<Boolean> filter);

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

    // TODO:JavaDoc
    protected JPQLQuery<Long> toSubquery(EntityPath<T> relation, Expression<Long> selector, Expression<Boolean> filter) {
        return JPAExpressions.selectFrom(relation)
                             .select(selector)
                             .where((Predicate)filter);
    }

    protected StringExpression toStringExpression(Object expr) throws ExpressionVisitException {
        if (expr instanceof String) {
            return Expressions.asString((String) expr);
        } else if (expr instanceof StringExpression) {
            return (StringExpression) expr;
        } else {
            throw new ExpressionVisitException("Error converting Argument to NumberExpression");
        }
    }

    @SuppressWarnings("unchecked")
    protected NumberExpression<Double> toNumberExpression(Object expr) throws ExpressionVisitException {
        if (expr instanceof String) {
            return Expressions.asNumber((Double) expr);
        } else if (expr instanceof NumberExpression) {
            return (NumberExpression<Double>) expr;
        } else if (expr instanceof StringExpression) {
            return ((StringExpression) expr).castToNum(Double.class);
        } else {
            throw new ExpressionVisitException("Error converting Argument to NumberExpression");
        }
    }

    @SuppressWarnings("unchecked")
    protected DateTimeExpression<Date> toDateTimeExpression(Object expr) throws ExpressionVisitException {
        if (expr instanceof String) {
            // TODO: check if this fails
            return Expressions.asDateTime((Date) expr);
        } else if (expr instanceof DateTimeExpression) {
            return (DateTimeExpression<Date>) expr;
        } else {
            throw new ExpressionVisitException("Error converting Argument to NumberExpression");
        }
    }

    // Wrapper
    protected Object handleDirectStringPropertyFilter(StringPath stringPath,
                                                      Object propertyValue,
                                                      BinaryOperatorKind operator,
                                                      boolean switched)
            throws ExpressionVisitException {
        return this.handleStringFilter(stringPath, toStringExpression(propertyValue), operator, switched);
    }

    protected Object handleDirectNumberPropertyFilter(NumberPath< ? > numberPath,
                                                      Object propertyValue,
                                                      BinaryOperatorKind operator,
                                                      boolean switched)
            throws ExpressionVisitException {
        return this.handleNumberFilter(numberPath, toNumberExpression(propertyValue), operator, switched);
    }

    protected Object handleDirectDateTimePropertyFilter(DateTimePath<Date> time,
                                                        Object propertyValue,
                                                        BinaryOperatorKind operator,
                                                        boolean switched)
            throws ExpressionVisitException {
        return this.handleDateFilter(time, toDateTimeExpression(propertyValue), operator, switched);
    }

    public Object handleStringFilter(StringExpression left,
                                     StringExpression right,
                                     BinaryOperatorKind operator,
                                     boolean switched)
            throws ExpressionVisitException {
        operator = (switched) ? reverseOperator(operator) : operator;

        switch (operator) {
        case EQ:
            return left.eq(right);
        case NE:
            return left.ne(right);
        case LT:
            return left.lt(right);
        case LE:
            return left.loe(right);
        case GT:
            return left.gt(right);
        case GE:
            return left.goe(right);
        default:
            throw new ExpressionVisitException("Error getting filter. Invalid Operator");
        }
    }

    public Object handleNumberFilter(NumberExpression< ? > left,
                                     NumberExpression< ? > right,
                                     BinaryOperatorKind operator,
                                     boolean switched)
            throws ExpressionVisitException {
        NumberExpression< ? > leftExpr;
        NumberExpression< ? > rightExpr;
        if (switched) {
            leftExpr = right;
            rightExpr = left;
        } else {
            leftExpr = left;
            rightExpr = right;
        }

        switch (operator) {
        case GE:
            return leftExpr.goe(rightExpr);
        case GT:
            return leftExpr.gt(rightExpr);
        case LE:
            return leftExpr.loe(rightExpr);
        case LT:
            return leftExpr.lt(rightExpr);
        case EQ:
            return ((ComparableExpressionBase) leftExpr).eq(rightExpr);
        case NE:
            return ((ComparableExpressionBase) leftExpr).ne(rightExpr);
        case ADD:
            return leftExpr.add(rightExpr);
        case DIV:
            return leftExpr.divide(rightExpr);
        case MOD:
            return leftExpr.castToNum(Integer.class).mod(rightExpr.castToNum(Integer.class));
        case MUL:
            return leftExpr.multiply(rightExpr);
        case SUB:
            return leftExpr.subtract(rightExpr);
        default:
            throw new ExpressionVisitException("Operator \"" + operator.toString()
                    + "\" is not supported for given arguments.");
        }
    }

    public Object handleDateFilter(DateTimeExpression<Date> left,
                                   DateTimeExpression<Date> right,
                                   BinaryOperatorKind operator,
                                   boolean switched)
            throws ExpressionVisitException {
        DateTimeExpression<Date> leftExpr;
        DateTimeExpression<Date> rightExpr;
        if (switched) {
            leftExpr = right;
            rightExpr = left;
        } else {
            leftExpr = left;
            rightExpr = right;
        }

        switch (operator) {
        case GE:
            return leftExpr.goe(rightExpr);
        case GT:
            return leftExpr.gt(rightExpr);
        case LE:
            return leftExpr.loe(rightExpr);
        case LT:
            return leftExpr.lt(rightExpr);
        case EQ:
            return leftExpr.eq(rightExpr);
        case NE:
            return leftExpr.ne(rightExpr);
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
