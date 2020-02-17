/*
 * Copyright 2015-2020 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.n52.sta.data.service;

import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

/**
 * Visitor visiting svalbard.odata.Expr and parsing it into javax.expression to be used in database access.
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
final class FilterExprVisitor implements ExprVisitor<Expression<?>, STAInvalidQueryException> {

    private CriteriaBuilder builder;

    FilterExprVisitor(CriteriaBuilder builder) {
        this.builder = builder;
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
        //TODO: check if these typecasts are even possible
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
            throw new STAInvalidQueryException("Could not evaluate Methodcall to :" + expr.getName());
        }
    }

    private Expression<?> visitMethodCallNullary(MethodCallExpr expr) throws STAInvalidQueryException {
        switch (expr.getName()) {
        case ODataConstants.DateAndTimeFunctions.NOW:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.MINDATETIME:
            // fallthru
        case ODataConstants.DateAndTimeFunctions.MAXDATETIME:
            throw new STAInvalidQueryException("not implemented yet!");
        default:
            throw new STAInvalidQueryException("Could not evaluate Methodcall to :" + expr.getName());
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
            throw new STAInvalidQueryException("not implemented yet!");
            // Math Functions
        case ODataConstants.ArithmeticFunctions.ROUND:
            return builder.function("ROUND", Integer.class, param);
        case ODataConstants.ArithmeticFunctions.FLOOR:
            return builder.function("FLOOR", Integer.class, param);
        case ODataConstants.ArithmeticFunctions.CEILING:
            return builder.function("CEIL", Integer.class, param);
        default:
            throw new STAInvalidQueryException("Could not evaluate Methodcall to :" + expr.getName());
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
            throw new STAInvalidQueryException("not implemented yet!");
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
            throw new STAInvalidQueryException("not implemented yet!");
        default:
            throw new STAInvalidQueryException("Could not evaluate Methodcall to :" + expr.getName());
        }
    }

    private Expression<?> visitMethodCallTernary(MethodCallExpr expr) throws STAInvalidQueryException {
        switch (expr.getName()) {
        case ODataConstants.SpatialFunctions.ST_RELATE:
            throw new STAInvalidQueryException("not implemented yet!");
        default:
            throw new STAInvalidQueryException("Could not evaluate Methodcall to :" + expr.getName());
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
    @Override public Expression<?> visitTime(TimeValueExpr expr) throws STAInvalidQueryException {
        return null;
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
