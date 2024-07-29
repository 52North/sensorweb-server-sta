package org.n52.sta.data.cloudnative.dao.util;

import org.jooq.Field;
import org.jooq.Condition;
import org.jooq.DatePart;
import org.jooq.impl.DSL;

import java.util.Date;
import java.util.function.BiFunction;

import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.data.cloudnative.condition.*;
import org.n52.svalbard.odata.core.expr.*;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
import org.n52.svalbard.odata.core.expr.arithmetic.NumericValueExpr;
import org.n52.svalbard.odata.core.expr.arithmetic.SimpleArithmeticExpr;
import org.n52.svalbard.odata.core.expr.bool.BooleanBinaryExpr;
import org.n52.svalbard.odata.core.expr.bool.BooleanUnaryExpr;
import org.n52.svalbard.odata.core.expr.bool.ComparisonExpr;
import org.n52.svalbard.odata.core.expr.temporal.TimeValueExpr;



public class FilterExprVisitor implements ExprVisitor<Field<?>, STAInvalidQueryException> {

    private static final String ERROR_NOT_IMPLEMENTED = "not implemented yet!";
    private static final String ERROR_NOT_EVALUABLE = "Could not evaluate Methodcall to: ";
    private static final String ERROR_NOT_SPATIAL = "Entity does not have spatial property!";

    private static final String DOLLAR = "%";
    private static final String SLASH = "/";

    private EntityQueryConditions rootQC;

    public FilterExprVisitor(String entity)
            throws STAInvalidFilterExpressionException {
        this.rootQC = QueryConditionRepository.getCondition(entity);
    }

    /**
     * Visit a boolean binary expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @SuppressWarnings("unchecked")
    @Override
    public Field<?> visitBooleanBinary(BooleanBinaryExpr expr) throws STAInvalidQueryException {
        Field<Boolean> left = (Field<Boolean>) expr.getLeft().accept(this);
        Field<Boolean> right = (Field<Boolean>) expr.getRight().accept(this);

        if (expr.getOperator().equals(FilterConstants.BinaryLogicOperator.And)) {
            return left.isTrue().and(right.isTrue());
        } else {
            return left.isTrue().or(right.isTrue());
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
    @Override
    public Field<?> visitBooleanUnary(BooleanUnaryExpr expr) throws STAInvalidQueryException {
        // Only 'not' exists as unary boolean expression
        return expr.getOperand().accept(this).isFalse();
    }

    /**
     * Visit a comparison expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override
    public Field<?> visitComparison(ComparisonExpr expr) throws STAInvalidQueryException {
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
    @Override
    public Field<?> visitMethodCall(MethodCallExpr expr) throws STAInvalidQueryException {
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
    @Override
    public Field<?> visitMember(MemberExpr expr) throws STAInvalidQueryException {
        // Add special handling for Observation as they have stored their "value" attribute distributed
        // over several different columns
        if (rootQC.getClass().equals(ObservationQueryConditions.class) &&
                expr.getValue().equals(StaConstants.PROP_RESULT)) {
            return null;
        } else {
            // TODO
            return DSL.field(rootQC.checkPropertyName(expr.getValue()));
        }
    }

    /**
     * Visit a value expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override
    public Field<?> visitString(StringValueExpr expr) throws STAInvalidQueryException {
        return DSL.val(expr.getValue());
    }

    /**
     * Visit an arithmetic expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @SuppressWarnings("unchecked")
    @Override
    public Field<?> visitSimpleArithmetic(SimpleArithmeticExpr expr) throws STAInvalidQueryException {
        Field<? extends Number> left = expr.getLeft().accept(this).cast(Number.class);
        Field<? extends Number> right = expr.getRight().accept(this).cast(Number.class);

        switch (expr.getOperator()) {
            case Add:
                return left.add(right);
            case Sub:
                return left.sub(right);
            case Mul:
                return left.mul(right);
            case Div:
                return left.div(right);
            case Mod:
                return left.cast(Integer.class).mod(right.cast(Integer.class));
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
    @Override
    public Field<?> visitTime(TimeValueExpr expr) throws STAInvalidQueryException {
        // This is always literal as we handle member Expressions seperately
        if (expr.getTime() instanceof String) {
            return DSL.field(rootQC.checkPropertyName((String) expr.getTime()));
        } else {
            return DSL.val(((TimeInstant) expr.getTime()).getValue().toDate());
        }
    }

    /**
     * Visit a geometry expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override
    public Field<?> visitGeometry(GeoValueExpr expr) throws STAInvalidQueryException {
        return null;
    }

    /**
     * Visit a number expression.
     *
     * @param expr the expression
     * @return the result of the visit
     * @throws STAInvalidQueryException if the visit fails
     */
    @Override
    public Field<?> visitNumeric(NumericValueExpr expr) throws STAInvalidQueryException {
        return DSL.val(expr.getValue());
    }

    @SuppressWarnings("unchecked")
    private <Y extends Comparable<? super Y>> Condition visitComparisonExpr(ComparisonExpr expr)
            throws STAInvalidQueryException {

        // Let QueryConditions handle Expressions involving members
        if (expr.getRight().isMember() || expr.getLeft().isMember()) {
            return evaluateMemberComparison(expr, expr.getOperator());
        } else {
            // Handle abstract + literal expression (everything not involving members) ourselves
            Field<Y> left = (Field<Y>) expr.getLeft().accept(this);
            Field<Y> right = (Field<Y>) expr.getRight().accept(this);
            switch (expr.getOperator()) {
                case PropertyIsEqualTo:
                    return left.eq(right);
                case PropertyIsNotEqualTo:
                    return left.ne(right);
                case PropertyIsLessThan:
                    return left.lt(right);
                case PropertyIsGreaterThan:
                    return left.gt(right);
                case PropertyIsLessThanOrEqualTo:
                    return left.le(right);
                case PropertyIsGreaterThanOrEqualTo:
                    return left.ge(right);
                default:
                    throw new STAInvalidQueryException(
                            "Invalid Operator. Could not parse: " + expr.getOperator().name());
            }
        }
    }

    private <Y extends Comparable<? super Y>> Condition evaluateMemberComparison(
            ComparisonExpr expr,
            FilterConstants.ComparisonOperator operator)
            throws STAInvalidQueryException {
        if (expr.getRight().isMember() && expr.getLeft().isMember()) {
            throw new STAInvalidQueryException("comparison of two member variables not implemented yet");
        } else if (expr.getRight().isMember()) {
            String memberName = expr.getRight().asMember().get().getValue();
            Field<Y> memberValue = (Field<Y>) expr.getLeft().accept(this);
            if (memberName.contains(SLASH)) {
                return convertToForeignExpression(
                        memberName,
                        memberValue,
                        operator);
            } else {
                return rootQC.getFilterForProperty(
                        memberName,
                        memberValue,
                        operator,
                        false);
            }
        } else if (expr.getLeft().isMember()) {
            String memberName = expr.getLeft().asMember().get().getValue();
            Field<Y> memberValue = (Field<Y>) expr.getRight().accept(this);
            if (memberName.contains(SLASH)
                    && !memberName.startsWith(StaConstants.PROP_PROPERTIES)
                    && !memberName.startsWith(StaConstants.PROP_PARAMETERS)) {
                return convertToForeignExpression(
                        memberName,
                        memberValue,
                        operator);
            } else {
                return rootQC.getFilterForProperty(
                        memberName,
                        memberValue,
                        operator,
                        false);
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
     * @return Condition specifying the entity
     * @throws STAInvalidFilterExpressionException if the filter is invalid
     */
    private <Y extends Comparable<? super Y>> Condition convertToForeignExpression(
            String path,
            Field<Y> value,
            FilterConstants.ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {
        String[] resources = path.split(SLASH);
        String lastResource = resources[resources.length - 2];

        // Get filter on Entity
        EntityQueryConditions stepQC;
        if (lastResource.equals(StaConstants.PROP_PROPERTIES)
                || lastResource.equals(StaConstants.PROP_PARAMETERS)) {
            stepQC = rootQC;
        } else {
            stepQC = QueryConditionRepository.getCondition(lastResource);
        }

        Condition filter =
                stepQC.getFilterForProperty(resources[resources.length - 1],
                        value,
                        operator,
                        false);
        return resolveForeignExpression(resources, filter);
    }

    /**
     * Resolves a filter on a spatial property of a related entity. Needs special handling as relation link is
     * nested inside the spatial function call.
     * e.g. /Things?$filter=st_equals(Locations/location, geography'POINT(52 52)')
     *
     * @param path         Path to the property of a related entity
     * @param functionName name of the function to be used
     * @param value        arguments of the function
     * @return Condition specifying the entity
     * @throws STAInvalidFilterExpressionException if the filter is invalid
     */
    private Condition convertToForeignSpatialExpression(String path,
                                                        String functionName,
                                                        String... value)
            throws STAInvalidFilterExpressionException {
        String[] resources = path.split(SLASH);
        EntityQueryConditions stepQC;
        // Get filter on Entity
        String lastResource = resources[resources.length - 2];
        stepQC = QueryConditionRepository.getCondition(lastResource);

        Condition filter;
        if (stepQC instanceof SpatialQueryConditions) {
            filter = ((SpatialQueryConditions) stepQC).handleGeoSpatialPropertyFilter(
                    resources[resources.length - 1],
                    functionName,
                    value);
            return resolveForeignExpression(resources, filter);
        } else {
            throw new STAInvalidFilterExpressionException(
                    ERROR_NOT_SPATIAL + resources[resources.length - 1]);
        }
    }

    private Condition resolveForeignExpression(String[] resources, Condition rawFilter)
            throws STAInvalidFilterExpressionException {
        EntityQueryConditions stepQS;
        Condition filter = rawFilter;
        for (int i = resources.length - 3; i >= 0; i--) {
            // Get QuerySpecifications for subQuery
            stepQS = QueryConditionRepository.getCondition(resources[i]);
            // Get new IdQuery based on Filter
            Condition expr = stepQS.getFilterForRelation(resources[i + 1], filter);
            filter = expr;
        }

        // Filter by Id on main Query
        return rootQC.getFilterForRelation(resources[0], filter);
    }

    private Field<?> visitMethodCallNullary(MethodCallExpr expr) throws STAInvalidQueryException {
        switch (expr.getName()) {
            case ODataConstants.DateAndTimeFunctions.NOW:
                return DSL.currentTimestamp();
            case ODataConstants.DateAndTimeFunctions.MINDATETIME:
                return DSL.val(new Date(0L));
            case ODataConstants.DateAndTimeFunctions.MAXDATETIME:
                return DSL.val(new Date(Long.MAX_VALUE));
            default:
                throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private Field<?> visitMethodCallUnary(MethodCallExpr expr) throws STAInvalidQueryException {
        Field<?> param = expr.getParameters().get(0).accept(this);
        switch (expr.getName()) {
            // String Functions
            case ODataConstants.StringFunctions.LENGTH:
                return DSL.length(param.cast(String.class));
            case ODataConstants.StringFunctions.TOLOWER:
                return DSL.lower(param.cast(String.class));
            case ODataConstants.StringFunctions.TOUPPER:
                return DSL.upper(param.cast(String.class));
            case ODataConstants.StringFunctions.TRIM:
                return DSL.trim(param.cast(String.class));
            // DateTime Functions
            case ODataConstants.DateAndTimeFunctions.YEAR:
                return DSL.function("YEAR", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.MONTH:
                return DSL.function("MONTH", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.DAY:
                return DSL.function("DAY", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.HOUR:
                return DSL.function("HOUR", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.MINUTE:
                return DSL.function("MINUTE", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.SECOND:
                return DSL.function("SECOND", Integer.class, param);
            case ODataConstants.DateAndTimeFunctions.FRACTIONALSECONDS:
                return DSL.extract(param, DatePart.MILLISECOND);
            case ODataConstants.DateAndTimeFunctions.DATE:
                // fallthru
            case ODataConstants.DateAndTimeFunctions.TIME:
                throw new STAInvalidQueryException(ERROR_NOT_IMPLEMENTED);
                // Math Functions
            case ODataConstants.ArithmeticFunctions.ROUND:
                return DSL.function("ROUND", Integer.class, param);
            case ODataConstants.ArithmeticFunctions.FLOOR:
                return DSL.function("FLOOR", Integer.class, param);
            case ODataConstants.ArithmeticFunctions.CEILING:
                return DSL.function("CEIL", Integer.class, param);
            case ODataConstants.GeoFunctions.GEO_LENGTH:
                if (rootQC instanceof SpatialQueryConditions) {
                    return ((SpatialQueryConditions) rootQC).handleGeospatial(
                            expr.getParameters().get(0).asGeometry().get(),
                            expr.getName(),
                            null
                    );
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
    private <P, Q, S extends Field<?>> S evalFuncOnMember(Field<P> firstParam,
                                                          Field<Q> secondParam,
                                                          BiFunction<Field<P>, Field<Q>, S> fkt)
            throws STAInvalidQueryException {
        if (firstParam != null) {
            return fkt.apply(firstParam, secondParam);
        } else {
            if (secondParam.getDataType().getClass().isAssignableFrom(String.class)) {
                // We could not resolve firstParam to a value, so we are filtering on Observation->result
                return (S) DSL.concat(
                        fkt.apply((Field<P>) DSL.field(EntityQueryConstants.PARAMETER_VALUE_CATEGORY), secondParam),
                        fkt.apply((Field<P>) DSL.field(EntityQueryConstants.PARAMETER_VALUE_TEXT), secondParam)
                );
            } else if (secondParam.getDataType().getClass().isAssignableFrom(Double.class)) {
                return (S) fkt.apply((Field<P>) DSL.field(EntityQueryConstants.PARAMETER_VALUE_QUANTITY), secondParam);
            } else {
                throw new STAInvalidQueryException("Could not evaluate function call on Observation->result. Result "
                + "type not recognized.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Field<?> visitMethodCallBinary(MethodCallExpr expr) throws STAInvalidQueryException {
        Field<?> secondParam;
        switch (expr.getName()) {
            // String Functions
            case ODataConstants.StringFunctions.ENDSWITH:
                String rawSecondPar = expr.getParameters().get(1).toString();
                return this.<String, String, Field<Boolean>>evalFuncOnMember(
                        (Field<String>) expr.getParameters().get(0).accept(this),
                        DSL.val(DOLLAR + rawSecondPar.substring(1, rawSecondPar.length() - 1)),
                        Field::like);
            case ODataConstants.StringFunctions.STARTSWITH:
                String rawSecondParam = expr.getParameters().get(1).toString();
                return this.<String, String, Field<Boolean>>evalFuncOnMember(
                        (Field<String>) expr.getParameters().get(0).accept(this),
                        DSL.val(rawSecondParam.substring(1, rawSecondParam.length() - 1) + DOLLAR),
                        Field::like);
            case ODataConstants.StringFunctions.SUBSTRINGOF:
                String rawFirstP = expr.getParameters().get(0).toString();
                return this.<String, String, Field<Boolean>>evalFuncOnMember(
                        (Field<String>) expr.getParameters().get(1).accept(this),
                        DSL.val(DOLLAR + rawFirstP.substring(1, rawFirstP.length() - 1) + DOLLAR),
                        Field::like);
            case ODataConstants.StringFunctions.INDEXOF:
                secondParam = expr.getParameters().get(1).accept(this);
                return this.<String, String, Field<Integer>>evalFuncOnMember(
                        (Field<String>)expr.getParameters().get(0).accept(this),
                        (Field<String>)secondParam,
                        DSL::position
                );
            case ODataConstants.StringFunctions.SUBSTRING:
                secondParam = expr.getParameters().get(1).accept(this);
                return this.<String, Integer, Field<String>>evalFuncOnMember(
                        (Field<String>) expr.getParameters().get(0).accept(this),
                        (Field<Integer>) secondParam,
                        DSL::substring
                );
            case ODataConstants.StringFunctions.CONCAT:
                secondParam = expr.getParameters().get(1).accept(this);
                return this.<String, String, Field<String>>evalFuncOnMember(
                        (Field<String>)expr.getParameters().get(0).accept(this),
                        (Field<String>) secondParam,
                        DSL::concat);
            // Geospatial Functions + Spatial Relationship Functions
            case ODataConstants.GeoFunctions.GEO_DISTANCE:
                if (rootQC instanceof SpatialQueryConditions) {
                    return ((SpatialQueryConditions) rootQC).handleGeospatial(
                            expr.getParameters().get(0).asGeometry().get(),
                            expr.getName(),
                            expr.getParameters().get(1).asGeometry().get().getGeometry()
                    );
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
                    if (rootQC instanceof SpatialQueryConditions) {
                        return ((SpatialQueryConditions) rootQC).handleGeoSpatialPropertyFilter(
                                        expr.getParameters().get(0).asGeometry().get().getGeometry(),
                                        expr.getName(),
                                        expr.getParameters().get(1).asGeometry().get().getGeometry());
                    } else {
                        throw new STAInvalidQueryException(ERROR_NOT_SPATIAL);
                    }
                }
            default:
                throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }

    private Field<?> visitMethodCallTernary(MethodCallExpr expr) throws STAInvalidQueryException {
        switch (expr.getName()) {
            case ODataConstants.SpatialFunctions.ST_RELATE:
                if (expr.getParameters().get(0).asGeometry().get().getGeometry().contains(SLASH)) {
                    return convertToForeignSpatialExpression(
                            expr.getParameters().get(0).asGeometry().get().getGeometry(),
                            expr.getName(),
                            expr.getParameters().get(1).asGeometry().get().getGeometry(),
                            expr.getParameters().get(2).asGeometry().get().getGeometry());
                } else {
                    if (rootQC instanceof SpatialQueryConditions) {
                        return ((SpatialQueryConditions) rootQC).handleGeoSpatialPropertyFilter(
                                        expr.getParameters().get(0).asGeometry().get().getGeometry(),
                                        expr.getName(),
                                        expr.getParameters().get(1).asGeometry().get().getGeometry(),
                                        expr.getParameters().get(2).asGeometry().get().getGeometry());

                    } else {
                        throw new STAInvalidQueryException(ERROR_NOT_SPATIAL);
                    }
                }
            default:
                throw new STAInvalidQueryException(ERROR_NOT_EVALUABLE + expr.getName());
        }
    }
}
