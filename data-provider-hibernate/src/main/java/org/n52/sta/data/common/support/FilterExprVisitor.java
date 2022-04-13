/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.data.common.support;

import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.sta.data.common.query.EntityQuerySpecifications;
import org.n52.sta.data.common.query.QuerySpecificationRepository;
import org.n52.sta.data.common.query.SpatialQuerySpecifications;
import org.n52.svalbard.odata.core.expr.Expr;
import org.n52.svalbard.odata.core.expr.ExprVisitor;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;
import org.n52.svalbard.odata.core.expr.MemberExpr;
import org.n52.svalbard.odata.core.expr.MethodCallExpr;
import org.n52.svalbard.odata.core.expr.StringValueExpr;
import org.n52.svalbard.odata.core.expr.arithmetic.NumericValueExpr;
import org.n52.svalbard.odata.core.expr.arithmetic.SimpleArithmeticExpr;
import org.n52.svalbard.odata.core.expr.bool.BooleanBinaryExpr;
import org.n52.svalbard.odata.core.expr.bool.BooleanUnaryExpr;
import org.n52.svalbard.odata.core.expr.bool.ComparisonExpr;
import org.n52.svalbard.odata.core.expr.temporal.TimeValueExpr;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.function.BiFunction;

/**
 * Visitor visiting svalbard.odata.Expr and parsing it into javax.expression to be used in database access.
 * Not all methods return predicate (e.g. internal ones return concrete types) so abstract Expression&lt;?&gt; is used.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public final class FilterExprVisitor<T> implements ExprVisitor<Expression<?>, STAInvalidQueryException> {

    private static final String ERROR_NOT_IMPLEMENTED = "not implemented yet!";
    private static final String ERROR_NOT_EVALUABLE = "Could not evaluate Methodcall to: ";
    private static final String ERROR_NOT_SPATIAL = "Entity does not have spatial property!";

    private static final String DOLLAR = "%";
    private static final String SLASH = "/";

    private CriteriaBuilder builder;
    private EntityQuerySpecifications<T> rootQS;
    private Root root;
    private CriteriaQuery query;

    public FilterExprVisitor(Root root, CriteriaQuery query, CriteriaBuilder builder)
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
        return builder.not((Expression<Boolean>) expr.getOperand().accept(this));
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

    /**
     * Visit a member expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override public Expression<?> visitMember(MemberExpr expr) throws STAInvalidQueryException {
        // Add special handling for Observation as they have stored their "value" attribute distributed
        // over several different columns
        if (root.getJavaType().isAssignableFrom(DataEntity.class) &&
            expr.getValue().equals(StaConstants.PROP_RESULT)) {
            return null;
        } else {
            return root.get(expr.getValue());
        }
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

    /**
     * Visit a time expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override public Expression<Date> visitTime(TimeValueExpr expr) throws STAInvalidQueryException {
        // This is always literal as we handle member Expressions seperately
        if (expr.getTime() instanceof String) {
            return root.get(rootQS.checkPropertyName((String) expr.getTime()));
        } else {
            return builder.literal(((TimeInstant) expr.getTime()).getValue().toDate());
        }
    }

    /**
     * Visit a geometry expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override public Expression<String> visitGeometry(GeoValueExpr expr) throws STAInvalidQueryException {
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
                    throw new STAInvalidQueryException(
                        "Invalid Operator. Could not parse: " + expr.getOperator().name());
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
            String leftValue = expr.getLeft().asMember().get().getValue();
            if (leftValue.contains(SLASH)
                && !leftValue.startsWith(StaConstants.PROP_PROPERTIES)
                && !leftValue.startsWith(StaConstants.PROP_PARAMETERS)) {
                return convertToForeignExpression(leftValue,
                                                  expr.getRight().accept(this),
                                                  operator);
            } else {
                return rootQS.getFilterForProperty(leftValue,
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
        EntityQuerySpecifications<?> stepQS;
        if (lastResource.equals(StaConstants.PROP_PROPERTIES) || lastResource.equals(StaConstants.PROP_PARAMETERS)) {
            stepQS = rootQS;
        } else {
            stepQS = QuerySpecificationRepository.getSpecification(lastResource);
        }

        Specification<?> filter =
            stepQS.getFilterForProperty(resources[resources.length - 1], value, operator, false);
        return resolveForeignExpression(resources, filter);
    }

    /**
     * Resolves a filter on a spatial property of an related entity. Needs special handling as relation link is
     * nested inside the spatial function call.
     * e.g. /Things?$filter=st_equals(Locations/location, geography'POINT(52 52)')
     *
     * @param path         Path to the property of a related entity
     * @param functionName name of the function to be used
     * @param value        arguments of the function
     * @return Expression specifying the entity
     * @throws STAInvalidFilterExpressionException if the filter is invalid
     */
    private Predicate convertToForeignSpatialExpression(String path,
                                                        String functionName,
                                                        String... value)
        throws STAInvalidFilterExpressionException {
        String[] resources = path.split(SLASH);
        EntityQuerySpecifications<?> stepQS;
        // Get filter on Entity
        String lastResource = resources[resources.length - 2];
        stepQS = QuerySpecificationRepository.getSpecification(lastResource);

        Specification<?> filter;
        if (stepQS instanceof SpatialQuerySpecifications) {
            filter = ((SpatialQuerySpecifications) stepQS).handleGeoSpatialPropertyFilter(
                resources[resources.length - 1],
                functionName,
                value);
            return resolveForeignExpression(resources, filter);
        } else {
            throw new STAInvalidFilterExpressionException(
                ERROR_NOT_SPATIAL + resources[resources.length - 1]);
        }
    }

    private Predicate resolveForeignExpression(String[] resources, Specification<?> rawFilter)
        throws STAInvalidFilterExpressionException {
        EntityQuerySpecifications<?> stepQS;
        Specification<?> filter = rawFilter;
        for (int i = resources.length - 3; i >= 0; i--) {
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

    private Expression<?> visitMethodCallNullary(MethodCallExpr expr) throws STAInvalidQueryException {
        switch (expr.getName()) {
            case ODataConstants.DateAndTimeFunctions.NOW:
                return builder.currentTimestamp();
            case ODataConstants.DateAndTimeFunctions.MINDATETIME:
                return builder.literal(new Date(0L));
            case ODataConstants.DateAndTimeFunctions.MAXDATETIME:
                return builder.literal(new Date(Long.MAX_VALUE));
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
                return builder.function("YEAR", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.MONTH:
                return builder.function("MONTH", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.DAY:
                return builder.function("DAY", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.HOUR:
                return builder.function("HOUR", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.MINUTE:
                return builder.function("MINUTE", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.SECOND:
                return builder.function("SECOND", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.FRACTIONALSECONDS:
                return builder.function("DATEPART",
                                        Integer.class,
                                        builder.literal("millisecond"),
                                        param);
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
            case ODataConstants.GeoFunctions.GEO_LENGTH:
                if (rootQS instanceof SpatialQuerySpecifications) {
                    return ((SpatialQuerySpecifications) rootQS).handleGeospatial(
                        expr.getParameters().get(0).asGeometry().get(),
                        expr.getName(),
                        null,
                        (HibernateSpatialCriteriaBuilder) builder,
                        root);
                } else {
                    throw new STAInvalidQueryException(ERROR_NOT_SPATIAL);
                }
            default:
                throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }

    /**
     * wraps function evaluation to handle function on Observation->value or Observation->parameters->value as the
     * value is split over multiple columns
     *
     * @param firstParam  first argument, description to fkt
     * @param secondParam second argument to fkt
     * @param fkt         function to be used for check
     * @return evaluated Predicate
     */
    private <P, Q, S extends Expression<?>> S evalFuncOnMember(Expression<P> firstParam,
                                                               Expression<Q> secondParam,
                                                               BiFunction<Expression<P>, Expression<Q>, S> fkt)
        throws STAInvalidQueryException {
        if (firstParam != null) {
            return fkt.apply(firstParam, secondParam);
        } else {
            if (secondParam.getJavaType().isAssignableFrom(String.class)) {
                // We could not resolve firstParam to a value, so we are filtering on Observation->result
                return (S) builder.concat(
                    fkt.apply(root.get(DataEntity.PROPERTY_VALUE_CATEGORY), secondParam),
                    fkt.apply(root.get(DataEntity.PROPERTY_VALUE_TEXT), secondParam)
                );
            } else if (secondParam.getJavaType().isAssignableFrom(Double.class)) {
                return (S) fkt.apply(root.get(DataEntity.PROPERTY_VALUE_QUANTITY), secondParam);
            } else {
                throw new STAInvalidQueryException("Could not evaluate function call on Observation->result. Result " +
                                                       "type not recognized.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Expression<?> visitMethodCallBinary(MethodCallExpr expr) throws STAInvalidQueryException {
        Expression<?> secondParam;
        switch (expr.getName()) {
            // String Functions
            case ODataConstants.StringFunctions.ENDSWITH:
                String rawSecondPar = expr.getParameters().get(1).toString();
                return this.<String, String, Expression<Boolean>>evalFuncOnMember(
                    (Expression<String>) expr.getParameters().get(0).accept(this),
                    builder.literal(DOLLAR + rawSecondPar.substring(1, rawSecondPar.length() - 1)),
                    builder::like);
            case ODataConstants.StringFunctions.STARTSWITH:
                String rawSecondParam = expr.getParameters().get(1).toString();
                return this.<String, String, Expression<Boolean>>evalFuncOnMember(
                    (Expression<String>) expr.getParameters().get(0).accept(this),
                    builder.literal(rawSecondParam.substring(1, rawSecondParam.length() - 1) + DOLLAR),
                    builder::like);
            case ODataConstants.StringFunctions.SUBSTRINGOF:
                String rawFirstP = expr.getParameters().get(0).toString();
                return this.<String, String, Expression<Boolean>>evalFuncOnMember(
                    (Expression<String>) expr.getParameters().get(1).accept(this),
                    builder.literal(DOLLAR + rawFirstP.substring(1, rawFirstP.length() - 1) + DOLLAR),
                    builder::like);
            case ODataConstants.StringFunctions.INDEXOF:
                secondParam = expr.getParameters().get(1).accept(this);
                return this.<String, String, Expression<Integer>>evalFuncOnMember(
                    (Expression<String>) expr.getParameters().get(0).accept(this),
                    (Expression<String>) secondParam,
                    builder::locate
                );
            case ODataConstants.StringFunctions.SUBSTRING:
                secondParam = expr.getParameters().get(1).accept(this);
                return this.<String, Integer, Expression<String>>evalFuncOnMember(
                    (Expression<String>) expr.getParameters().get(0).accept(this),
                    (Expression<Integer>) secondParam,
                    builder::substring
                );
            case ODataConstants.StringFunctions.CONCAT:
                secondParam = expr.getParameters().get(1).accept(this);
                return this.<String, String, Expression<String>>evalFuncOnMember(
                    (Expression<String>) expr.getParameters().get(0).accept(this),
                    (Expression<String>) secondParam,
                    builder::concat);
            // Geospatial Functions + Spatial Relationship Functions
            case ODataConstants.GeoFunctions.GEO_DISTANCE:
                if (rootQS instanceof SpatialQuerySpecifications) {
                    return ((SpatialQuerySpecifications) rootQS).handleGeospatial(
                        expr.getParameters().get(0).asGeometry().get(),
                        expr.getName(),
                        expr.getParameters().get(1).asGeometry().get().getGeometry(),
                        (HibernateSpatialCriteriaBuilder) builder,
                        root);
                } else {
                    throw new STAInvalidQueryException(ERROR_NOT_SPATIAL);
                }
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
                if (expr.getParameters().get(0).asGeometry().get().getGeometry().contains(SLASH)) {
                    return convertToForeignSpatialExpression(
                        expr.getParameters().get(0).asGeometry().get().getGeometry(),
                        expr.getName(),
                        expr.getParameters().get(1).asGeometry().get().getGeometry());
                } else {
                    if (rootQS instanceof SpatialQuerySpecifications) {
                        return ((SpatialQuerySpecifications) rootQS).handleGeoSpatialPropertyFilter(
                            expr.getParameters().get(0).asGeometry().get().getGeometry(),
                            expr.getName(),
                            expr.getParameters().get(1).asGeometry().get().getGeometry())
                            .toPredicate(root, query, builder);
                    } else {
                        throw new STAInvalidQueryException(ERROR_NOT_SPATIAL);
                    }
                }
            default:
                throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }

    private Expression<?> visitMethodCallTernary(MethodCallExpr expr) throws STAInvalidQueryException {
        switch (expr.getName()) {
            case ODataConstants.SpatialFunctions.ST_RELATE:
                if (expr.getParameters().get(0).asGeometry().get().getGeometry().contains(SLASH)) {
                    return convertToForeignSpatialExpression(
                        expr.getParameters().get(0).asGeometry().get().getGeometry(),
                        expr.getName(),
                        expr.getParameters().get(1).asGeometry().get().getGeometry(),
                        expr.getParameters().get(2).asGeometry().get().getGeometry());
                } else {
                    if (rootQS instanceof SpatialQuerySpecifications) {
                        return ((SpatialQuerySpecifications) rootQS).handleGeoSpatialPropertyFilter(
                            expr.getParameters().get(0).asGeometry().get().getGeometry(),
                            expr.getName(),
                            expr.getParameters().get(1).asGeometry().get().getGeometry(),
                            expr.getParameters().get(2).asGeometry().get().getGeometry())
                            .toPredicate(root, query, builder);
                    } else {
                        throw new STAInvalidQueryException(ERROR_NOT_SPATIAL);
                    }
                }
            default:
                throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }

    private Expression<? extends Number> visitNumericExpr(Expr expr) throws STAInvalidQueryException {
        return (Expression<? extends Number>) expr.accept(this);
    }
}
