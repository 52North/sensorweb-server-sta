/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.service.query;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmBoolean;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeography;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyLineString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyMultiLineString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyMultiPoint;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyMultiPolygon;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyPoint;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyPolygon;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometry;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryLineString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryMultiLineString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryMultiPoint;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryMultiPolygon;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryPoint;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryPolygon;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmTimespan;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.jts.JTS;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.expression.function.TrimFunction;
import org.hibernate.spatial.criterion.SpatialRestrictions;
import org.joda.time.DateTime;
import org.n52.sta.data.query.EntityQuerySpecifications;
import org.n52.sta.data.query.QuerySpecificationRepository;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Bucket;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties.Build;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class FilterExpressionVisitor implements ExpressionVisitor<Object> {

    private Class sourceType;

    private String sourcePath;

    private EntityQuerySpecifications< ? > rootQS;

    private AbstractSensorThingsEntityService service;

    private CriteriaBuilder criteriaBuilder;

    private Root<?> root;

    public FilterExpressionVisitor(Class sourceType, AbstractSensorThingsEntityService service, CriteriaBuilder criteriaBuilder, Root<?> root)
            throws ODataApplicationException {
        this.sourceType = sourceType;
        this.service = service;
        this.rootQS = QuerySpecificationRepository.getSpecification(sourceType.getSimpleName());
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;

        // TODO: Replace fragile simpleName (with lowercase first letter) with better alternative (e.g.
        // <QType>.getRoot())
        this.sourcePath = Character.toLowerCase(sourceType.getSimpleName().charAt(0))
                + sourceType.getSimpleName().substring(1);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor#visitBinaryOperator(org.
     * apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public Object visitBinaryOperator(BinaryOperatorKind operator, Object left, Object right)
            throws ExpressionVisitException,
            ODataApplicationException {
        if (operator == BinaryOperatorKind.ADD
                || operator == BinaryOperatorKind.MOD
                || operator == BinaryOperatorKind.MUL
                || operator == BinaryOperatorKind.DIV
                || operator == BinaryOperatorKind.SUB) {
            return evaluateArithmeticOperation(operator, left, right);
        } else if (operator == BinaryOperatorKind.EQ
                || operator == BinaryOperatorKind.NE
                || operator == BinaryOperatorKind.GE
                || operator == BinaryOperatorKind.GT
                || operator == BinaryOperatorKind.LE
                || operator == BinaryOperatorKind.LT) {
            return evaluateComparisonOperation(operator, left, right);
        } else if (operator == BinaryOperatorKind.AND
                || operator == BinaryOperatorKind.OR) {
            return evaluateBooleanOperation(operator, left, right);
        } else {
            throw new ODataApplicationException("Binary operation " + operator.name() + " is not implemented",
                                                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                                                Locale.ENGLISH);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor#visitUnaryOperator(org.apache
     * .olingo.server.api.uri.queryoption.expression.UnaryOperatorKind, java.lang.Object)
     */
    @Override
    public Object visitUnaryOperator(UnaryOperatorKind operator, Object operand) throws ExpressionVisitException,
    ODataApplicationException {

        if (operator == UnaryOperatorKind.NOT && operand instanceof Expression) {
            // 1.) boolean negation
            return criteriaBuilder.not((javax.persistence.criteria.Expression<Boolean>) operand);
//            return ((BooleanExpression) operand).not();
        } else if (operator == UnaryOperatorKind.MINUS && operand instanceof Number) {
            // 2.) arithmetic minus
            return -(Double) operand;
        } else if (operator == UnaryOperatorKind.MINUS && operand instanceof Expression) {
            // 2.) arithmetic minus
            return criteriaBuilder.neg((javax.persistence.criteria.Expression<Number>) operand);
//            return ((NumberExpression) operand).negate();
        }

        // Operation not processed, throw an exception
        throw new ODataApplicationException("Invalid type for unary operator",
                                            HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                            Locale.ENGLISH);
    }

    private Object evaluateArithmeticOperation(BinaryOperatorKind operator, Object left, Object right)
            throws ExpressionVisitException,
            ODataApplicationException {
        javax.persistence.criteria.Expression<? extends Comparable< ? >> leftExpr = convertToArithmeticExpression(left);
        javax.persistence.criteria.Expression<? extends Comparable< ? >> rightExpr = convertToArithmeticExpression(right);

//        return rootQS.handleNumberFilter(leftExpr, rightExpr, operator, criteriaBuilder, false);
        return null;
    }

    /**
     * Evaluates Comparison operation for various Types. Comparison is attempted in the following order:
     *
     * Number Comparison > String Comparison > Date Comparison > Timespan Comparison.
     *
     * If parameters can not be converted into comparable Datatypes or all Comparisons fail an error is
     * thrown.
     *
     * @param operator
     *        Operator to be used for comparison
     * @param left
     *        left operand
     * @param right
     *        right operand
     * @return BooleanExpression evaluating to true if comparison evaluated to true
     * @throws ODataApplicationException
     *         if invalid operator was encountered or Expression is not comparable
     * @throws ExpressionVisitException
     *         if invalid operator was encountered
     */
    private Object evaluateComparisonOperation(BinaryOperatorKind operator, Object left, Object right)
            throws ODataApplicationException,
            ExpressionVisitException {

        // Let QuerySpecifications handle Expressions with properties
        // as they can ensure proper Datatypes etc.
        if (left instanceof String || right instanceof String) {
            if (left instanceof String) {
                return rootQS.getFilterForProperty((String) left, right, operator, false);
            } else {
                return rootQS.getFilterForProperty((String) right, left, operator, true);
            }
        } else {
            // Handle Literals + Combined Expressions
            // Assume Numbers are compared
            try {

                javax.persistence.criteria.Expression< ? extends Comparable< ? >> leftExpr = convertToArithmeticExpression(left);
                javax.persistence.criteria.Expression< ? extends Comparable< ? >> rightExpr = convertToArithmeticExpression(right);
//                return rootQS.handleNumberFilter(leftExpr, rightExpr, operator, criteriaBuilder, false);
                return null;
            } catch (ODataApplicationException e) {
            }

            // Fallback to String comparison
            try {
                // Handle literal values + inherent properties
                if (! (left instanceof List< ? > || right instanceof List< ? >)) {

                    // Fallback to String comparison
                    Path<String> leftExpr = root.get((String) left);
                    String rightExpr = convertToString(right);
                    return rootQS.handleStringFilter(leftExpr, rightExpr, operator, criteriaBuilder, false);
                } else {
                    // Handle foreign properties
                    if (left instanceof List< ? > && right instanceof List< ? >) {
                        // TODO: implement
                        throw new ODataApplicationException("Comparison of two foreign properties is currently not implemented",
                                                            HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                            Locale.ENGLISH);
                    } else if (left instanceof List< ? >) {
                        return convertToForeignExpression((List<UriResource>) left, right, operator);
                    } else {
                        return convertToForeignExpression((List<UriResource>) right, left, operator);
                    }

                }
            } catch (ODataApplicationException f) {
            }

            // Fallback to Date comparison
            try {
                javax.persistence.criteria.Expression<Date> leftExpr = root.<Date> get((String) left);
                Date rightExpr = convertToDateTimeExpression(right);
                return rootQS.handleDateFilter(leftExpr, rightExpr, operator, criteriaBuilder);
            } catch (ODataApplicationException e) {
            }

            // Fallback to Timespan comparison
            try {
                javax.persistence.criteria.Expression<Date>[] leftExpr = convertToTimespanExpression(left);
                javax.persistence.criteria.Expression<Date>[] rightExpr = convertToTimespanExpression(right);

                switch (operator) {
                case EQ: {
                    return criteriaBuilder.and(criteriaBuilder.equal(leftExpr[0], rightExpr[0]),
                            criteriaBuilder.equal(leftExpr[1], rightExpr[1]));
                }
                case NE: {
                    return criteriaBuilder.and(criteriaBuilder.notEqual(leftExpr[0], rightExpr[0]),
                            criteriaBuilder.notEqual(leftExpr[1], rightExpr[1]));
                }
                default: {
                    throw new ODataApplicationException("Comparison of Timespans is currently implemented for EQ and NE operators.",
                                                        HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                        Locale.ENGLISH);
                }
                }
            } catch (ODataApplicationException e) {
            }

            // Fallback to Error
            throw new ODataApplicationException("Could not parse Parameters to Filter Expression.",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ENGLISH);
        }
    }

    private Object evaluateBooleanOperation(BinaryOperatorKind operator, Object left, Object right)
            throws ODataApplicationException {
        javax.persistence.criteria.Expression<Boolean> leftExpr = (javax.persistence.criteria.Expression<Boolean>) left;
        javax.persistence.criteria.Expression<Boolean> rightExpr = (javax.persistence.criteria.Expression<Boolean>) right;

        if (operator == BinaryOperatorKind.AND) {
            return criteriaBuilder.and(rightExpr, leftExpr);
        } else if (operator == BinaryOperatorKind.OR) {
            return criteriaBuilder.or(rightExpr, leftExpr);
        } else {
            throw new ODataApplicationException("Could not convert " + operator.toString() + " to BooleanOperation",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ENGLISH);
        }
    }

    private Date convertToDateTimeExpression(Object expr) throws ODataApplicationException {
        if (expr instanceof Date) {
            // Literal
            return (Date) expr;
        } else if (expr instanceof String) {
            // Property
            return new DateTime(expr).toDate();
        }
        throw new ODataApplicationException("Could not convert " + expr.toString() + "to BooleanExpression",
                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
    }

    private javax.persistence.criteria.Expression<Date>[] convertToTimespanExpression(Object expr) throws ODataApplicationException {
        javax.persistence.criteria.Expression<Date>[] result = new javax.persistence.criteria.Expression[2];

//        if (expr instanceof Date[]) {
//            // Literal
//            final Constructor<?> c = Expression.class.getConstructor(Date.class);
//            result[0] = c.newInstance(((Date[])expr)[0]);
//            result[1] = c.newInstance(((Date[])expr)[0]);
//            return result;
//        } else
            throw new ODataApplicationException("Could not convert " + expr.toString() + "to DateTimeExpression",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ENGLISH);
    }

    /**
     *
     * Constructs a Subquery based on given Path to property of related Entity to evaluate Filters on those
     * properties. Returned Expression evaluates to true if Entity should be included. TODO: Expand to support
     * deeper nested properties
     *
     * @param uriResources
     *        Path to foreign property
     * @param value
     *        supposed value of foreign property
     * @param operator
     *        operator to be used to compare value and actual value
     * @return BooleanExpression evaluating to true if filter on related entity was successful.
     * @throws ExpressionVisitException
     *         If the subquery could not be build.
     * @throws ODataApplicationException
     *         If no QuerySpecification for given related Entity was found.
     */
    private Specification<?> convertToForeignExpression(List<UriResource> uriResources,
                                                         Object value,
                                                         BinaryOperatorKind operator)
                                                                 throws ExpressionVisitException,
                                                                 ODataApplicationException {
        Specification idQuery = null;
        int uriLength = uriResources.size();
        String lastResource = uriResources.get(uriLength - 2).toString();
        // Get filter on Entity
        EntityQuerySpecifications stepQS = QuerySpecificationRepository.getSpecification(lastResource);
        Specification<?> filter =
                stepQS.getFilterForProperty(uriResources.get(uriLength - 1).toString(), value, operator, false);
        idQuery = stepQS.getIdSubqueryWithFilter(filter);
        for (int i = uriLength - 3; i > 0; i--) {
            // Get QuerySpecifications for subQuery
            stepQS = QuerySpecificationRepository.getSpecification(uriResources.get(i).toString());
            // Get new IdQuery based on Filter
            Specification<?> expr =
                    stepQS.getFilterForProperty(uriResources.get(i + 1).toString(), idQuery, null, false);
            idQuery = stepQS.getIdSubqueryWithFilter(expr);
        }
        // Filter by Id on main Query
        // TODO check if this cast is legit
        return rootQS.getFilterForProperty(uriResources.get(0).toString(), idQuery, null, false);
    }

    /**
     * Converts an Object into a computable StringExpression. Throws an Exception if Conversion fails.
     *
     * @param expr
     *        Object to be coerced into StringExpression
     * @return StringExpression equivalent to expr
     * @throws ODataApplicationException
     *         if Object cannot be converted to StringExpression
     */
    private javax.persistence.criteria.Expression<String> convertToStringExpression(Object expr) throws ODataApplicationException {
        if (expr instanceof javax.persistence.criteria.Expression) {
            // SubExpression
            return (javax.persistence.criteria.Expression<String>) expr;
        } else {
            throw new ODataApplicationException("Could not convert " + expr.toString() + " to StringExpression",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ENGLISH);
        }
    }

    private String convertToString(Object expr) throws ODataApplicationException {
        if (expr instanceof String) {
            return (String) expr;
        } else {
            throw new ODataApplicationException("Could not convert " + expr.toString() + " to StringExpression",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(),
                    Locale.ENGLISH);
        }
    }

    /**
     * Converts an Object into a computable NumberExpression. Throws an Exception if Conversion fails.
     *
     * @param expr
     *        Object to be coerced into NumberExpression
     * @return NumberExpression equivalent to expr
     * @throws ODataApplicationException
     *         if Object cannot be converted to NumberExpression
     */
    private javax.persistence.criteria.Expression<? extends Comparable<?>> convertToArithmeticExpression(Object expr)
            throws ODataApplicationException {
//        if (expr instanceof Number) {
//            // Raw Number
////            return Expressions.asNumber((double) expr);
//            final Class<?> defaultType = type.getDefaultType();
//            final Constructor<?> c = Double.class.getConstructor(Double.class);
//            return c.newInstance((Number) expr);
//            criteriaBuilder.p
//        } else if (expr instanceof javax.persistence.criteria.Expression< ? >) {
//            // SubExpression
//            return (javax.persistence.criteria.Expression< ? >) expr;
//        }
        throw new ODataApplicationException("Could not convert " + expr.toString() + " to NumberExpression",
                                            HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                            Locale.ENGLISH);
    }

    /**
     * Converts an Object into a GeometryExpression. Throws an Exception if Conversion fails.
     *
     * @param expr
     *        Object to be coerced into GeometryExpression
     * @return GeometryExpression equivalent to expr
     * @throws ODataApplicationException
     *         if Object cannot be converted to GeometryExpression
     */
    private javax.persistence.criteria.Expression<Geometry> convertToGeometryExpression(Object expr) throws ODataApplicationException {
        if (expr instanceof javax.persistence.criteria.Expression) {
            // SubExpression
            return (javax.persistence.criteria.Expression<Geometry>) expr;
        } else {
            throw new ODataApplicationException("Could not convert " + expr.toString() + " to StringExpression",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ENGLISH);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor#visitMethodCall(org.apache.
     * olingo.server.api.uri.queryoption.expression.MethodKind, java.util.List)
     */
    @Override
    public Object visitMethodCall(MethodKind methodCall, List<Object> parameters) throws ExpressionVisitException,
    ODataApplicationException {
        Object arg1 = parameters.get(0);
        Object arg2 = parameters.get(1);

        switch (methodCall) {
        // String Functions
        case CONTAINS:
        case SUBSTRINGOF:
            return criteriaBuilder.function(
                    "CONTAINS", Boolean.class,
                    root.<String>get((String) arg1),
                    criteriaBuilder.parameter(String.class, (String) arg2));
//            return criteriaBuilder.function(name, type, args)(x)convertToStringExpression(arg1).contains(convertToStringExpression(arg2));
        case ENDSWITH:
            return criteriaBuilder.like(convertToStringExpression(arg1), "%" + arg2);
//            return convertToStringExpression(arg1).endsWith(convertToStringExpression(arg2));
        case STARTSWITH:
            return criteriaBuilder.like(convertToStringExpression(arg1), arg2 + "%");
        case LENGTH:
            criteriaBuilder.length(convertToStringExpression(arg1));
        case INDEXOF:
            return criteriaBuilder.locate(convertToStringExpression(arg1), convertToStringExpression(arg2));
        case SUBSTRING:
            javax.persistence.criteria.Expression<String> string = convertToStringExpression(arg1);
            return criteriaBuilder.substring(string, (Integer) arg2);
        case TOLOWER:
            return criteriaBuilder.lower(convertToStringExpression(arg1));
        case TOUPPER:
            return criteriaBuilder.upper(convertToStringExpression(arg1));
        case TRIM:
            return criteriaBuilder.trim(convertToStringExpression(arg1));
        case CONCAT:
            return criteriaBuilder.concat(convertToStringExpression(arg1), convertToStringExpression(arg2));

            // Math Functions
        case ROUND:
            return criteriaBuilder.function(
                    "ROOUD", Boolean.class,
                    root.<String>get((String) arg1));
//            return convertToArithmeticExpression(arg1).round();
        case FLOOR:
            return criteriaBuilder.function(
                    "FLOOR", Boolean.class,
                    root.<String>get((String) arg1));
//            return convertToArithmeticExpression(arg1).floor();
        case CEILING:
            return criteriaBuilder.function(
                    "CEIL", Boolean.class,
                    root.<String>get((String) arg1));
//            return convertToArithmeticExpression(arg1).ceil();

            // Date Functions
//        case YEAR:
//            return convertToDateTimeExpression(arg1).year();
//        case MONTH:
//            return convertToDateTimeExpression(arg1).month();
//        case DAY:
//            return convertToDateTimeExpression(arg1).dayOfMonth();
//        case HOUR:
//            return convertToDateTimeExpression(arg1).hour();
//        case MINUTE:
//            return convertToDateTimeExpression(arg1).minute();
//        case SECOND:
//            return convertToDateTimeExpression(arg1).second();
//        case FRACTIONALSECONDS:
//            cri
//            return convertToDateTimeExpression(arg1).milliSecond();
//        case NOW:
//            return criteriaBuilder.currentTimestamp();
//        case MINDATETIME:
//            return Expressions.asDate(Date.from(Instant.MIN));
//        case MAXDATETIME:
//            return Expressions.asDate(Date.from(Instant.MAX));
        case DATE:
            // TODO: Implement
            break;
        case TIME:
            // TODO: Implement
            break;
        case TOTALOFFSETMINUTES:
            // TODO: Implement
            break;

            // Geospatial Functions
        case GEODISTANCE:
            return SpatialRestrictions.distanceWithin((String) arg1, JTS.to((Geometry) arg2), (Double) parameters.get(2));
        case GEOLENGTH:
            // TODO: Implement
            break;
        case GEOINTERSECTS:
            return SpatialRestrictions.intersects((String) arg1, JTS.to((Geometry) arg2));
            // Spatial Relationship Functions
        case ST_CONTAINS:
            return SpatialRestrictions.contains((String) arg1, JTS.to((Geometry) arg2));
        case ST_CROSSES:
            return SpatialRestrictions.crosses((String) arg1, JTS.to((Geometry) arg2));
        case ST_DISJOINT:
            return SpatialRestrictions.disjoint((String) arg1, JTS.to((Geometry) arg2));
        case ST_EQUALS:
            return SpatialRestrictions.eq((String) arg1, JTS.to((Geometry) arg2));
        case ST_INTERSECTS:
            return SpatialRestrictions.intersects((String) arg1, JTS.to((Geometry) arg2));
        case ST_OVERLAPS:
            return SpatialRestrictions.overlaps((String) arg1, JTS.to((Geometry) arg2));
        case ST_RELATE:
            break;
//            return convertToGeometryExpression(arg1).(convertToGeometryExpression(arg2),
//                                                            parameters.get(2).toString());
        case ST_TOUCHES:
            return SpatialRestrictions.touches((String) arg1, JTS.to((Geometry) arg2));
        case ST_WITHIN:
            return SpatialRestrictions.within((String) arg1, JTS.to((Geometry) arg2));
        default:
            break;
        }
        // Fallback to Error in case of ODATA-conform but not STA-conform Method or unimplemented method
        throw new ODataApplicationException("Invalid Method: " + methodCall.name()
        + " is not included in STA Specification.",
        HttpStatusCode.BAD_REQUEST.getStatusCode(),
        Locale.ENGLISH);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor#visitLambdaExpression(java.
     * lang.String, java.lang.String, org.apache.olingo.server.api.uri.queryoption.expression.Expression)
     */
    @Override
    public Object visitLambdaExpression(String lambdaFunction, String lambdaVariable, Expression expression)
            throws ExpressionVisitException,
            ODataApplicationException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor#visitLiteral(org.apache.
     * olingo.server.api.uri.queryoption.expression.Literal)
     */
    @Override
    public Object visitLiteral(Literal literal) throws ExpressionVisitException, ODataApplicationException {
        // String literals start and end with an single quotation mark
        EdmPrimitiveType type = (EdmPrimitiveType) literal.getType();
        String literalAsString = literal.getText();
        try {
            if (type instanceof EdmString) {
                String stringLiteral = "";
                if (literal.getText().length() > 2) {
                    stringLiteral = literalAsString.substring(1, literalAsString.length() - 1);
                }
                return stringLiteral;
    //            return Expressions.asString(stringLiteral);
            } else if (type instanceof EdmBoolean) {
                // TODO: Check if boolean literals are actually supported by STA Spec
                // return (Boolean.valueOf(literal.getText()))? Expressions.TRUE: Expressions.FALSE;
                throw new ODataApplicationException("Boolean Literals are currently not implemented",
                                                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                                                    Locale.ENGLISH);
            } else if (type instanceof EdmDateTimeOffset) {
                return new DateTime(literal.getText());
            } else if (type instanceof EdmGeography || type instanceof EdmGeometry
                    || type instanceof EdmGeographyPoint || type instanceof EdmGeometryPoint
                    || type instanceof EdmGeographyMultiPoint || type instanceof EdmGeometryMultiPoint
                    || type instanceof EdmGeographyLineString || type instanceof EdmGeometryLineString
                    || type instanceof EdmGeographyMultiLineString || type instanceof EdmGeometryMultiLineString
                    || type instanceof EdmGeographyPolygon || type instanceof EdmGeometryPolygon
                    || type instanceof EdmGeographyMultiPolygon || type instanceof EdmGeometryMultiPolygon) {
                String wkt = literalAsString.substring(literalAsString.indexOf("\'") + 1, literalAsString.length() - 1);
                if (!wkt.startsWith("SRID")) {
                    wkt = "SRID=4326;" + wkt;
                }
                final Class<?> defaultType = type.getDefaultType();
                final Constructor<?> c = defaultType.getConstructor(Geometry.class);
                return c.newInstance(Wkt.fromWkt(wkt));
    //            return GeometryExpressions.asGeometry(Wkt.fromWkt(wkt));
            } else if (type instanceof EdmTimespan) {
                Date[] timespan = new Date[2];

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                String[] split = literalAsString.split("/");
                try {
                    timespan[0] = format.parse(split[0]);
                    timespan[1] = format.parse(split[1]);
                } catch (ParseException e) {
                    throw new ODataApplicationException("Could not parse Date. Error was: "
                            + e.getMessage(),
                            HttpStatusCode.BAD_REQUEST.getStatusCode(),
                            Locale.ENGLISH);
                }
                return timespan;
            } else {
                // Coerce literal numbers into Double
                try {
                    return Double.parseDouble(literalAsString);
                } catch (NumberFormatException e) {
                    throw new ODataApplicationException("Could not parse literal Numeric Value to Double. Error was: "
                            + e.getMessage(),
                            HttpStatusCode.BAD_REQUEST.getStatusCode(),
                            Locale.ENGLISH);
                }
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new ODataApplicationException("Could not parse literal. Error was: " + e.getMessage(),
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor#visitMember(org.apache.olingo
     * .server.api.uri.queryoption.expression.Member)
     */
    @Override
    public Object visitMember(Member member) throws ExpressionVisitException, ODataApplicationException {
        final List<UriResource> uriResourceParts = member.getResourcePath().getUriResourceParts();
        if (uriResourceParts.size() == 1 && uriResourceParts.get(0) instanceof UriResourcePrimitiveProperty) {
            UriResourcePrimitiveProperty uriResourceProperty = (UriResourcePrimitiveProperty) uriResourceParts.get(0);

            String name = uriResourceProperty.getProperty().getName();
            // Workaround for Properties that can not be ordered but filtered by
            if (name.equals("encodingType") || name.equals("metadata")) {
                return name;
            } else {
                return service.checkPropertyName(name);
            }
        } else {
            return uriResourceParts;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor#visitAlias(java.lang.String)
     */
    @Override
    public Object visitAlias(String aliasName) throws ExpressionVisitException, ODataApplicationException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor#visitTypeLiteral(org.apache.
     * olingo.commons.api.edm.EdmType)
     */
    @Override
    public Object visitTypeLiteral(EdmType type) throws ExpressionVisitException, ODataApplicationException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor#visitLambdaReference(java.
     * lang.String)
     */
    @Override
    public Object visitLambdaReference(String variableName) throws ExpressionVisitException, ODataApplicationException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor#visitEnum(org.apache.olingo.
     * commons.api.edm.EdmEnumType, java.util.List)
     */
    @Override
    public Object visitEnum(EdmEnumType type, List<String> enumValues) throws ExpressionVisitException,
    ODataApplicationException {
        // TODO Auto-generated method stub
        return null;
    }

}
