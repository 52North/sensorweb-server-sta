package org.n52.sta.data.service.hereon;

import java.util.function.Supplier;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.OperatorExportToJson;
import com.esri.core.geometry.OperatorImportFromWkt;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.WktImportFlags;
import org.n52.sensorweb.server.helgoland.adapters.connector.HereonConstants;
import org.n52.sensorweb.server.helgoland.adapters.connector.mapping.Observation;
import org.n52.shetland.oasis.odata.ODataConstants;
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

public final class FilterExprVisitor<T> implements
        ExprVisitor<FilterExprVisitor.FeatureQuery, STAInvalidQueryException> {

    private static final String ERROR_NOT_AVAILABLE = "this $filter is not available for Hereon backend";
    private final Observation mapping;

    public FilterExprVisitor(Observation mapping) {
        this.mapping = mapping;
    }

    @Override
    public FeatureQuery visitBooleanBinary(BooleanBinaryExpr expr) throws STAInvalidQueryException {
        FeatureQuery left = expr.getLeft().accept(this);
        FeatureQuery right = expr.getRight().accept(this);

        if (expr.getOperator().equals(FilterConstants.BinaryLogicOperator.And)) {
            return left.and(right);
        } else {
            return left.or(right);
        }
    }

    @Override
    public FeatureQuery visitBooleanUnary(BooleanUnaryExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    @Override
    public FeatureQuery visitComparison(ComparisonExpr expr) throws STAInvalidQueryException {
        String operator;
        String left;
        String right;

        if (expr.getLeft().isMember()) {
            // map to hereon property
            left = mapToFeatureProperty(expr.getLeft().asMember().get().getValue());
            right = expr.getRight().accept(this).where;
        } else {
            left = expr.getLeft().accept(this).where;
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
        return new FeatureQuery(left + operator + right);
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
    public FeatureQuery visitMethodCall(MethodCallExpr expr) throws STAInvalidQueryException {
        switch (expr.getParameters().size()) {
            case 2:
                return visitMethodCallBinary(expr);
            default:
                throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE + ": " + expr.getName());
        }
    }

    /**
     * Currently only supports spatial `ST_`-methods
     *
     * @param expr Expression to be parsed
     * @return parsed expression
     * @throws STAInvalidQueryException if an error occurred
     */
    private FeatureQuery visitMethodCallBinary(MethodCallExpr expr) throws STAInvalidQueryException {
        String param = expr.getParameters().get(0).asGeometry().get().getGeometry();

        if (!param.equals("parameters/geometry")) {
            throw new STAInvalidQueryException("can only apply spatial filters on 'parameters/geometry'");
        }

        String spatialRel;
        FeatureQuery geom = expr.getParameters().get(1).accept(this);
        switch (expr.getName()) {
            case ODataConstants.GeoFunctions.GEO_INTERSECTS:
                // fallthru
            case ODataConstants.SpatialFunctions.ST_INTERSECTS:
                spatialRel = "esriSpatialRelIntersects";
                break;
            case ODataConstants.SpatialFunctions.ST_TOUCHES:
                spatialRel = "esriSpatialRelTouches";
                break;
            case ODataConstants.SpatialFunctions.ST_WITHIN:
                spatialRel = "esriSpatialRelWithin";
                break;
            case ODataConstants.SpatialFunctions.ST_OVERLAPS:
                spatialRel = "esriSpatialRelOverlaps";
                break;
            case ODataConstants.SpatialFunctions.ST_CROSSES:
                spatialRel = "esriSpatialRelCrosses";
                break;
            case ODataConstants.SpatialFunctions.ST_CONTAINS:
                spatialRel = "esriSpatialRelContains";
                break;
            default:
                throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE + expr.getName());
        }
        return geom.and(new FeatureQuery(null, null, spatialRel));
    }

    @Override
    public FeatureQuery visitGeometry(GeoValueExpr expr) throws STAInvalidQueryException {
        String wkt = expr.getGeometry().substring(10, expr.getGeometry().length() - 1);

        OperatorImportFromWkt parser = OperatorImportFromWkt.local();
        Geometry geom = parser.execute(WktImportFlags.wktImportDefaults, Geometry.Type.Unknown, wkt, null);
        String geometryJson = OperatorExportToJson.local().execute(SpatialReference.create(4326), geom);
        String type = "";
        switch (geom.getType()) {
            case Point:
                type = "esriGeometryPoint";
                break;
            case Line:
                type = "esriGeometryLine";
                break;
            case Envelope:
                type = "esriGeometryEnvelope";
                break;
            case MultiPoint:
                type = "esriGeometryMultipoint";
                break;
            case Polyline:
                type = "esriGeometryPolyline";
                break;
            case Polygon:
                type = "esriGeometryPolygon";
                break;
            default:
                throw new STAInvalidQueryException("cannot parse geometry. Unknown type");
        }
        return new FeatureQuery(geometryJson, type, null);
    }

    @Override
    public FeatureQuery visitTime(TimeValueExpr expr) throws STAInvalidQueryException {
        if (expr.getTime() instanceof TimeInstant) {
            return new FeatureQuery(String.valueOf(((TimeInstant) expr.getTime()).getValue().toInstant().getMillis()));
        } else {
            throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
        }
    }

    @Override
    public FeatureQuery visitMember(MemberExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    @Override
    public FeatureQuery visitString(StringValueExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    @Override
    public FeatureQuery visitSimpleArithmetic(SimpleArithmeticExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    @Override
    public FeatureQuery visitNumeric(NumericValueExpr expr) throws STAInvalidQueryException {
        throw new STAInvalidQueryException(ERROR_NOT_AVAILABLE);
    }

    public static class FeatureQuery {

        private String where;
        private String geometry;
        private String geometryType;
        private String spatialRel;

        FeatureQuery(String where) {
            this.where = where;
        }

        FeatureQuery(String geometry, String geometryType, String spatialRel) {
            this.geometry = geometry;
            this.geometryType = geometryType;
            this.spatialRel = spatialRel;
        }

        FeatureQuery and(FeatureQuery other) {
            if (this.where != null) {
                this.where = String.format("(%s AND %s)", this.where, other.where);
            } else {
                this.where = other.getWhere();
            }
            combineGeometry(other);
            return this;
        }

        FeatureQuery or(FeatureQuery other) {
            if (this.where != null) {
                this.where = String.format("(%s OR %s)", this.where, other.where);
            } else {
                this.where = other.getWhere();
            }
            combineGeometry(other);
            return this;
        }

        private void combineGeometry(FeatureQuery other) {
            if (this.geometry == null) {
                this.geometry = other.geometry;
            }
            if (this.spatialRel == null) {
                this.spatialRel = other.spatialRel;
            }
            if (this.geometryType == null) {
                this.geometryType = other.geometryType;
            }
        }

        public String getWhere() {
            return where;
        }

        public String getGeometry() {
            return geometry;
        }

        public String getGeometryType() {
            return geometryType;
        }

        public String getSpatialRel() {
            return spatialRel;
        }

    }

}


























