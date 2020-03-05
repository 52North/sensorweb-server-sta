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

package org.n52.sta.data.service;

import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.data.query.EntityQuerySpecifications;
import org.n52.sta.data.query.QuerySpecificationRepository;
import org.n52.svalbard.odata.expr.Expr;
import org.n52.svalbard.odata.expr.ExprVisitor;
import org.n52.svalbard.odata.expr.GeoValueExpr;
import org.n52.svalbard.odata.expr.MemberExpr;
import org.n52.svalbard.odata.expr.MethodCallExpr;
import org.n52.svalbard.odata.expr.StringValueExpr;
import org.n52.svalbard.odata.expr.arithmetic.NumericValueExpr;
import org.n52.svalbard.odata.expr.arithmetic.SimpleArithmeticExpr;
import org.n52.svalbard.odata.expr.binary.BooleanBinaryExpr;
import org.n52.svalbard.odata.expr.binary.BooleanUnaryExpr;
import org.n52.svalbard.odata.expr.binary.ComparisonExpr;
import org.n52.svalbard.odata.expr.temporal.TimeValueExpr;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

/**
 * Visitor visiting svalbard.odata.Expr and parsing it into javax.expression to be used in database access.
 * Not all methods return predicate (e.g. internal ones return concrete types) so abstract Expression&lt;?&gt; is used.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
final class FilterExprVisitor<T> implements ExprVisitor<Expression<?>, STAInvalidQueryException> {

    private static final String ERROR_NOT_IMPLEMENTED = "not implemented yet!";
    private static final String ERROR_NOT_EVALUABLE = "Could not evaluate Methodcall to :";

    private static final String SLASH = "/";

    private CriteriaBuilder builder;
    private EntityQuerySpecifications<T> rootQS;
    private Root root;
    private CriteriaQuery query;

    FilterExprVisitor(Root root, CriteriaQuery query, CriteriaBuilder builder)
            throws STAInvalidFilterExpressionException {
        this.builder = builder;
        this.query = query;
        this.root = root;
        this.rootQS = QuerySpecificationRepository.getSpecification(root.getJavaType().getSimpleName());
    }

    /**
     * Visit a boolean binary expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @SuppressWarnings("unchecked")
    @Override public Predicate visitBooleanBinary(BooleanBinaryExpr expr) throws STAInvalidQueryException {
        Expression<Boolean> left = (Expression<Boolean>) expr.getLeft().accept(this);
        Expression<Boolean> right = (Expression<Boolean>) expr.getRight().accept(this);

        if (expr.getOperator().equals(FilterConstants.BinaryLogicOperator.And)) {
            return builder.and(left, right);
        } else {
            return builder.or(left, right);
        }
    }

    /**
     * Visit a boolean unary expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @SuppressWarnings("unchecked")
    @Override public Predicate visitBooleanUnary(BooleanUnaryExpr expr) throws STAInvalidQueryException {
        // Only 'not' exists as unary boolean expression
        return builder.not((Expression<Boolean>) expr.accept(this));
    }

    /**
     * Visit a comparison expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override public Predicate visitComparison(ComparisonExpr expr) throws STAInvalidQueryException {
        // Proxy to allow for java generics without interfering with Override
        return this.visitComparisonExpr(expr);
    }

    @SuppressWarnings("unchecked")
    private <Y extends Comparable<? super Y>> Predicate visitComparisonExpr(ComparisonExpr expr)
            throws STAInvalidQueryException {

        // Let Queryspecifications handle Expressions involving members
        if (expr.getRight().isMember() || expr.getLeft().isMember()) {
            return evaluateMemberComparison(expr, expr.getOperator());
        } else {
            // Handle abstract + literal expression (everything not involving members) ourselves
            Expression<? extends Y> left = (Expression<? extends Y>) expr.getLeft().accept(this);
            Expression<? extends Y> right = (Expression<? extends Y>) expr.getRight().accept(this);
            switch (expr.getOperator()) {
            case PropertyIsEqualTo:
                return builder.equal(left, right);
            case PropertyIsNotEqualTo:
                return builder.notEqual(left, right);
            case PropertyIsLessThan:
                return builder.lessThan(left, right);
            case PropertyIsGreaterThan:
                return builder.greaterThan(left, right);
            case PropertyIsLessThanOrEqualTo:
                return builder.lessThanOrEqualTo(left, right);
            case PropertyIsGreaterThanOrEqualTo:
                return builder.greaterThanOrEqualTo(left, right);
            default:
                throw new STAInvalidQueryException("Invalid Operator. Could not parse: " + expr.getOperator().name());
            }
        }
    }

    private Predicate evaluateMemberComparison(ComparisonExpr expr, FilterConstants.ComparisonOperator operator)
            throws STAInvalidQueryException {
        if (expr.getRight().isMember() && expr.getLeft().isMember()) {
            throw new STAInvalidQueryException("comparison of two member variables not implemented yet");
        } else if (expr.getRight().isMember()) {
            if (expr.getRight().asMember().get().getValue().contains(SLASH)) {
                return convertToForeignExpression(expr.getRight().asMember().get().getValue(),
                                                  expr.getLeft().accept(this),
                                                  operator);
            } else {
                return rootQS.getFilterForProperty(expr.getRight().asMember().get().getValue(),
                                                   expr.getLeft().accept(this),
                                                   operator,
                                                   false)
                             .toPredicate(root, query, builder);
            }
        } else if (expr.getLeft().isMember()) {
            if (expr.getLeft().asMember().get().getValue().contains(SLASH)) {
                return convertToForeignExpression(expr.getLeft().asMember().get().getValue(),
                                                  expr.getRight().accept(this),
                                                  operator);
            } else {
                return rootQS.getFilterForProperty(expr.getLeft().asMember().get().getValue(),
                                                   expr.getRight().accept(this),
                                                   operator,
                                                   false)
                             .toPredicate(root, query, builder);
            }
        } else {
            // This should never happen!
            throw new STAInvalidQueryException("[This should never happen!] Tried to evaluate member comparison " +
                                                       "without members being involved!");
        }
    }

    /**
     * Converts a Filter on related Properties to a chain of Expressions on nested Entities
     * e.g. Things/Datastreams/Sensor/id eq '52N'
     *
     * @param path     Path to the property of a related entity
     * @param value    value of the property
     * @param operator operator to be used
     * @return Expression specifying the entity
     * @throws STAInvalidFilterExpressionException if the filter is invalid
     */
    private Predicate convertToForeignExpression(String path,
                                                 Expression<?> value,
                                                 FilterConstants.ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {
        String[] resources = path.split(SLASH);
        String lastResource = resources[resources.length - 2];

        // Get filter on Entity
        EntityQuerySpecifications<?> stepQS = QuerySpecificationRepository.getSpecification(lastResource);

        Specification<?> filter =
                stepQS.getFilterForProperty(resources[resources.length - 1], value, operator, false);

        for (int i = resources.length - 3; i > 0; i--) {
            // Get QuerySpecifications for subQuery
            stepQS = QuerySpecificationRepository.getSpecification(resources[i]);
            // Get new IdQuery based on Filter
            Specification<?> expr =
                    stepQS.getFilterForRelation(resources[i + 1], filter);
            filter = expr;
        }

        // Filter by Id on main Query
        return rootQS.getFilterForRelation(resources[0], filter)
                     .toPredicate(root, query, builder);
    }

    /**
     * Visit a method call expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override public Expression<?> visitMethodCall(MethodCallExpr expr) throws STAInvalidQueryException {
        switch (expr.getParameters().size()) {
        case 0:
            return visitMethodCallNullary(expr);
        case 1:
            return visitMethodCallUnary(expr);
        case 2:
            return visitMethodCallBinary(expr);
        case 3:
            return visitMethodCallTernary(expr);
        default:
            throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }

    private Expression<?> visitMethodCallNullary(MethodCallExpr expr) throws STAInvalidQueryException {
        switch (expr.getName()) {
        case ODataConstants.DateAndTimeFunctions.NOW:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.MINDATETIME:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.MAXDATETIME:
            throw new STAInvalidQueryException(ERROR_NOT_IMPLEMENTED);
        default:
            throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private Expression<?> visitMethodCallUnary(MethodCallExpr expr) throws STAInvalidQueryException {
        Expression<?> param = expr.getParameters().get(0).accept(this);
        switch (expr.getName()) {
        // String Functions
        case ODataConstants.StringFunctions.LENGTH:
            return builder.length((Expression<String>) param);
        case ODataConstants.StringFunctions.TOLOWER:
            return builder.lower((Expression<String>) param);
        case ODataConstants.StringFunctions.TOUPPER:
            return builder.upper((Expression<String>) param);
        case ODataConstants.StringFunctions.TRIM:
            return builder.trim((Expression<String>) param);
        // DateTime Functions
        case ODataConstants.DateAndTimeFunctions.YEAR:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.MONTH:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.DAY:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.HOUR:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.MINUTE:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.SECOND:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.FRACTIONALSECONDS:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.DATE:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.TIME:
            throw new STAInvalidQueryException(ERROR_NOT_IMPLEMENTED);
            // Math Functions
        case ODataConstants.ArithmeticFunctions.ROUND:
            return builder.function("ROUND", Integer.class, param);
        case ODataConstants.ArithmeticFunctions.FLOOR:
            return builder.function("FLOOR", Integer.class, param);
        case ODataConstants.ArithmeticFunctions.CEILING:
            return builder.function("CEIL", Integer.class, param);
        default:
            throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private Expression<?> visitMethodCallBinary(MethodCallExpr expr) throws STAInvalidQueryException {
        Expression<?> firstParam = expr.getParameters().get(0).accept(this);
        Expression<?> secondParam = expr.getParameters().get(1).accept(this);
        switch (expr.getName()) {

        // String Functions
        case ODataConstants.StringFunctions.ENDSWITH:
        case ODataConstants.StringFunctions.STARTSWITH:
            //TODO: Check how we can append/prepend the DOLLAR to the second param
            // return builder.like((Expression<String>) firstParam, "%" + secondParam);
            throw new STAInvalidQueryException(ERROR_NOT_IMPLEMENTED);
        case ODataConstants.StringFunctions.SUBSTRINGOF:
            return builder.function(
                    "CONTAINS",
                    Boolean.class,
                    firstParam,
                    secondParam
            );
        case ODataConstants.StringFunctions.INDEXOF:
            return builder.locate((Expression<String>) firstParam,
                                  (Expression<String>) secondParam);
        case ODataConstants.StringFunctions.SUBSTRING:
            return builder.substring((Expression<String>) firstParam,
                                     (Expression<Integer>) secondParam);
        case ODataConstants.StringFunctions.CONCAT:
            return builder.concat((Expression<String>) firstParam,
                                  (Expression<String>) secondParam);

        // Geospatial Functions + Spatial Relationship Functions
        case ODataConstants.GeoFunctions.GEO_DISTANCE:
            // fallthru
        case ODataConstants.GeoFunctions.GEO_LENGTH:
            // fallthru
        case ODataConstants.GeoFunctions.GEO_INTERSECTS:
            // fallthru
        case ODataConstants.SpatialFunctions.ST_EQUALS:
            // fallthru
        case ODataConstants.SpatialFunctions.ST_DISJOINT:
            // fallthru
        case ODataConstants.SpatialFunctions.ST_TOUCHES:
            // fallthru
        case ODataConstants.SpatialFunctions.ST_WITHIN:
            // fallthru
        case ODataConstants.SpatialFunctions.ST_OVERLAPS:
            // fallthru
        case ODataConstants.SpatialFunctions.ST_CROSSES:
            // fallthru
        case ODataConstants.SpatialFunctions.ST_INTERSECTS:
            // fallthru
        case ODataConstants.SpatialFunctions.ST_CONTAINS:
            throw new STAInvalidQueryException(ERROR_NOT_IMPLEMENTED);
        default:
            throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }

    private Expression<?> visitMethodCallTernary(MethodCallExpr expr) throws STAInvalidQueryException {
        switch (expr.getName()) {
        case ODataConstants.SpatialFunctions.ST_RELATE:
            throw new STAInvalidQueryException(ERROR_NOT_IMPLEMENTED);
        default:
            throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }

    /**
     * Visit a member expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override public Expression<?> visitMember(MemberExpr expr) throws STAInvalidQueryException {
        return null;
    }

    /**
     * Visit a value expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override public Expression<String> visitString(StringValueExpr expr) throws STAInvalidQueryException {
        return builder.literal(expr.getValue());
    }

    /**
     * Visit a arithmetic expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @SuppressWarnings("unchecked")
    @Override public Expression<? extends Number> visitSimpleArithmetic(SimpleArithmeticExpr expr)
            throws STAInvalidQueryException {
        Expression<? extends Number> left = (Expression<? extends Number>) expr.getLeft().accept(this);
        Expression<? extends Number> right = (Expression<? extends Number>) expr.getRight().accept(this);
        switch (expr.getOperator()) {
        case Add:
            return builder.sum(left, right);
        case Sub:
            return builder.diff(left, right);
        case Mul:
            return builder.prod(left, right);
        case Div:
            return builder.quot(left, right);
        case Mod:
            return builder.mod((Expression<Integer>) left,
                               (Expression<Integer>) right);
        default:
            throw new STAInvalidQueryException(
                    "Could not parse ArithmeticExpr. Could not identify Operator:" + expr.getOperator().name());
        }
    }

    private Expression<? extends Number> visitNumericExpr(Expr expr) throws STAInvalidQueryException {
        return (Expression<? extends Number>) expr.accept(this);
    }

    /**
     * Visit a time expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override public Expression<Date> visitTime(TimeValueExpr expr) throws STAInvalidQueryException {
        // This is always literal as we handle member Expressions seperately
        return builder.literal(((TimeInstant) expr.getTime()).getValue().toDate());
    }

    /**
     * Visit a geometry expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override public Expression<?> visitGeometry(GeoValueExpr expr) throws STAInvalidQueryException {
        return null;
    }

    /**
     * Visit a number expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override public Expression<? extends Number> visitNumeric(NumericValueExpr expr) throws
            STAInvalidQueryException {
        return builder.literal(expr.getValue());
    }
}
