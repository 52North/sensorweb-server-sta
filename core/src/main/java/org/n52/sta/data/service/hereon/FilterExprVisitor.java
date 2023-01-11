package org.n52.sta.data.service.hereon;

import java.util.function.Supplier;

import org.n52.sensorweb.server.helgoland.adapters.connector.HereonConstants;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Observation;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidQueryException;
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

public final class FilterExprVisitor<T> implements ExprVisitor<String, STAInvalidQueryException> {

    private static final String ERROR_NOT_AVAILABLE = "this $filter is not available for Hereon backend!";
    private final Observation mapping;

    public FilterExprVisitor(Observation mapping) {
        this.mapping = mapping;
    }

    @Override
    public String visitBooleanBinary(BooleanBinaryExpr expr) throws STAInvalidQueryException {
        String left = expr.getLeft().accept(this);
        String right = expr.getRight().accept(this);

        if (expr.getOperator().equals(FilterConstants.BinaryLogicOperator.And)) {
            return left + " AND " + right;
        } else {
            return left + " OR " + right;
        }
    }

    @Override
    public String visitBooleanUnary(BooleanUnaryExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    @Override
    public String visitComparison(ComparisonExpr expr) throws STAInvalidQueryException {
        String operator, left, right;

        if (expr.getLeft().isMember()) {
            // map to hereon property
            left = mapToFeatureProperty(expr.getLeft().asMember().get().getValue());
            right = expr.getRight().accept(this);
        } else {
            left = expr.getLeft().accept(this);
            right = mapToFeatureProperty(expr.getRight().asMember().get().getValue());
        }

        switch (expr.getOperator()) {
            case PropertyIsEqualTo:
                operator = " = ";
                break;
            case PropertyIsNotEqualTo:
                operator = " != ";
                break;
            case PropertyIsLessThan:
                operator = " < ";
                break;
            case PropertyIsGreaterThan:
                operator = " > ";
                break;
            case PropertyIsLessThanOrEqualTo:
                operator = " <= ";
                break;
            case PropertyIsGreaterThanOrEqualTo:
                operator = " >= ";
                break;
            case PropertyIsLike:
                operator = " LIKE ";
                break;
            default:
                throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
        }
        return left + operator + right;
    }

    private String mapToFeatureProperty(String staProperty) throws STAInvalidQueryException {
        // Map STA property to Feature Service property
        switch (staProperty) {
            case StaConstants.PROP_RESULT:
                return getOrError(mapping::getResult, staProperty);
            case StaConstants.PROP_PHENOMENON_TIME:
                return getOrError(mapping::getPhenomenonTime, staProperty);
            case StaConstants.PROP_RESULT_TIME:
                return getOrError(mapping::getResultTime, staProperty);
            case StaConstants.PROP_VALID_TIME:
                return getOrError(mapping::getValidTime, staProperty);
            case StaConstants.PROP_ID:
                return HereonConstants.DataFields.GLOBAL_ID;
            case StaConstants.PROP_RESULT_QUALITY:
                return getOrError(mapping::getResultQuality, staProperty);
            default:
                throw new STAInvalidQueryException(String.format("cannot filter by %s. No such property!",
                                                                 staProperty));
        }
    }

    public static String getOrError(Supplier<String> supplier, String property) throws STAInvalidQueryException {
        String value = supplier.get();
        if (value == null || value.equals("")) {
            throw new STAInvalidQueryException(String.format("cannot order by %s. property is not mapped!", property));
        } else {
            return value;
        }
    }

    @Override
    public String visitMethodCall(MethodCallExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    @Override
    public String visitMember(MemberExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    @Override
    public String visitString(StringValueExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    @Override
    public String visitSimpleArithmetic(SimpleArithmeticExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    @Override
    public String visitTime(TimeValueExpr expr) throws STAInvalidQueryException {
        if (expr.getTime() instanceof TimeInstant) {
            return String.valueOf(((TimeInstant) expr.getTime()).getValue().toInstant().getMillis());
        } else {
            throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
        }
    }

    @Override
    public String visitGeometry(GeoValueExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    @Override
    public String visitNumeric(NumericValueExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }
}
