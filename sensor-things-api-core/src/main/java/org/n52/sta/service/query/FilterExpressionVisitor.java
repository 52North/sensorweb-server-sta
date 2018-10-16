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

package org.n52.sta.service.query;

import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmBoolean;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
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
import org.n52.sta.data.service.AbstractSensorThingsEntityService;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class FilterExpressionVisitor<T> implements ExpressionVisitor<Object> {

    private Class<T> sourceType;
    
    private AbstractSensorThingsEntityService service;

    public FilterExpressionVisitor(Class<T> sourceType, AbstractSensorThingsEntityService service) {
        this.sourceType = sourceType;
        this.service = service;
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

    private Object evaluateArithmeticOperation(BinaryOperatorKind operator, Object left, Object right)
            throws ODataApplicationException {

        // Check values
        com.querydsl.core.types.dsl.NumberExpression< ? > leftExpr = convertToArithmeticExpression(left);
        com.querydsl.core.types.dsl.NumberExpression< ? > rightExpr = convertToArithmeticExpression(right);

        // Check operands and get Operand Values
        switch (operator) {
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
            // this should never happen. Trying Arithmetics without Arithmetic operator
            return null;

        }
    }

    private Object evaluateComparisonOperation(BinaryOperatorKind operator, Object left, Object right)
            throws ODataApplicationException {
        
        try {
            // Assume Numbers are compared
            com.querydsl.core.types.dsl.NumberExpression< ? extends Comparable<?>> leftExpr = convertToArithmeticExpression(left);                 
            com.querydsl.core.types.dsl.NumberExpression< ? extends Comparable<?> > rightExpr = convertToArithmeticExpression(right);

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
                return ((ComparableExpressionBase)leftExpr).eq(rightExpr);
            case NE:
                return ((ComparableExpressionBase)leftExpr).ne(rightExpr);
            default:
                // This should never happen. There are no other operations on Numbers
                return null;
            }
        } catch (ODataApplicationException e) {
            try {
                // Fallback to String comparison
                StringExpression leftExpr = convertToStringExpression(left);
                StringExpression rightExpr = convertToStringExpression(right);
                switch (operator) {
                case EQ:
                    return leftExpr.eq(rightExpr);
                case NE:
                    return leftExpr.ne(rightExpr);
                default:
                    // This should never happen. There are no other operations on Strings
                    return null;
                }
            } catch (ODataApplicationException f) {
                throw new ODataApplicationException("Could not convert Expression to ComparableExpression.",
                                                    HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                    Locale.ENGLISH);
            }
        }
    }

    private Object evaluateBooleanOperation(BinaryOperatorKind operator, Object left, Object right)
            throws ODataApplicationException {
        // Check operands and get Operand Values
        BooleanExpression leftExpr = convertToBooleanExpression(left);
        BooleanExpression rightExpr = convertToBooleanExpression(right);

        BooleanBuilder builder = new BooleanBuilder();

        if (operator == BinaryOperatorKind.AND) {
            return builder.and(rightExpr).and(leftExpr).getValue();
        } else if (operator == BinaryOperatorKind.OR) {
            return builder.or(rightExpr).or(leftExpr).getValue();
        } else {
            throw new ODataApplicationException("Could not convert " + operator.toString() + " to BooleanOperation",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ENGLISH);
        }
    }

    private BooleanExpression convertToBooleanExpression(Object expr) throws ODataApplicationException {
        if (expr instanceof BooleanExpression) {
            return (BooleanExpression) expr;
        } else {
            throw new ODataApplicationException("Could not convert " + expr.toString() + "to BooleanExpression",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ENGLISH);
        }
    }

    private StringExpression convertToStringExpression(Object expr) throws ODataApplicationException {
        StringExpression result;
        if (expr instanceof StringPath) {
            result = (StringPath) expr;
        } else if (expr instanceof StringExpression) {
            result = (StringExpression) expr;
        } else {
            throw new ODataApplicationException("Could not convert " + expr.toString() + " to BooleanExpressions",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ENGLISH);
        }
        return result;
    }

    private com.querydsl.core.types.dsl.NumberExpression< ? extends Comparable<?>> convertToArithmeticExpression(Object expr)
            throws ODataApplicationException {
        NumberExpression< ? > result;
        if (expr instanceof Number) {
            // Raw Number
            result = Expressions.asNumber((double) expr);
        } else if (expr instanceof String) {
            // Reference to Property
            result = new PathBuilder<T>(sourceType, "entity").getNumber(service.checkPropertyForSorting((String) expr), Double.class);
        } else if (expr instanceof NumberExpression< ? >) {
            // SubExpression
            result = (com.querydsl.core.types.dsl.NumberExpression< ? >) expr;
        } else {
            throw new ODataApplicationException("Could not convert " + expr.toString() + " to BooleanExpression",
                                                HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                Locale.ENGLISH);
        }
        return result;
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

        if (operator == UnaryOperatorKind.NOT && operand instanceof Boolean) {
            // 1.) boolean negation
            return !(Boolean) operand;
        } else if (operator == UnaryOperatorKind.MINUS && operand instanceof Integer) {
            // 2.) arithmetic minus
            return -(Integer) operand;
        }

        // Operation not processed, throw an exception
        throw new ODataApplicationException("Invalid type for unary operator",
                                            HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                            Locale.ENGLISH);
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
        // TODO Auto-generated method stub
        return null;
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
        String literalAsString = literal.getText();
        if (literal.getType() instanceof EdmString) {
            String stringLiteral = "";
            if (literal.getText().length() > 2) {
                stringLiteral = literalAsString.substring(1, literalAsString.length() - 1);
            }

            return Expressions.asString(stringLiteral);
        } else if (literal.getType() instanceof EdmBoolean) {
            // TODO: Check if boolean literals are actually supported
            // return (Boolean.valueOf(literal.getText()))? Expressions.TRUE: Expressions.FALSE;
            throw new ODataApplicationException("Boolean Literals are currently not implemented",
                                                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                                                Locale.ENGLISH);
        } else {
            // Try to convert the literal into an Java
            try {
                return Expressions.asNumber(Double.parseDouble(literalAsString));
            } catch (NumberFormatException e) {
                throw new ODataApplicationException("Could not parse Literal to Double",
                                                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                                                    Locale.ENGLISH);
            }
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

        // Make sure that the resource path of the property contains only a single segment and a
        // primitive property has been addressed. We can be sure, that the property exists because
        // the UriParser checks if the property has been defined in service metadata document.

        if (uriResourceParts.size() == 1 && uriResourceParts.get(0) instanceof UriResourcePrimitiveProperty) {
            UriResourcePrimitiveProperty uriResourceProperty = (UriResourcePrimitiveProperty) uriResourceParts.get(0);
            
            //TODO: Add Paths for Numbers etc.
            //TODO: Replace fragile simpleName (with lowercase first letter) with better alternative (e.g. <QType>.getRoot())
            String sourcePath = sourceType.getSimpleName();
            return new PathBuilder<T>(sourceType, Character.toLowerCase(sourcePath.charAt(0)) + sourcePath.substring(1)).getString(service.checkPropertyForSorting(uriResourceProperty.getProperty().getName()));
        } else {
            // The OData specification allows in addition complex properties and navigation
            // properties with a target cardinality 0..1 or 1.
            // This means any combination can occur e.g. Supplier/Address/City
            // -> Navigation properties Supplier
            // -> Complex Property Address
            // -> Primitive Property City
            // For such cases the resource path returns a list of UriResourceParts
            throw new ODataApplicationException("Only primitive properties are implemented in filter expressions",
                                                HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                                                Locale.ENGLISH);
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
