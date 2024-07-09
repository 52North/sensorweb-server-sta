/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.cloudnative.condition;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.cloudnative.condition.utils.GeospatialFunctions;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
public class FeatureOfInterestQueryConditions extends EntityQueryConditions implements SpatialQueryConditions {

    public Condition withObservationStaIdentifier(final String observationIdentifier) {

        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(FEATURE_TABLE))
                        .innerJoin(DSL.table(DATASTREAM_TABLE))
                        .onKey()
                        .innerJoin(DSL.table(OBSERVATION_TABLE))
                        .onKey()
                        .where(DSL.field(DSL.name(OBSERVATION_ID_FIELD, STA_IDENTIFIER_FIELD))
                                .eq(observationIdentifier))
        );
    }

    @Override
    protected <T extends Comparable<? super T>> Condition
    handleDirectPropertyFilter(String propertyName,
                               Field<T> propertyValue,
                               FilterConstants.ComparisonOperator operator,
                               boolean switched) {
        try {
            switch (propertyName) {
                case StaConstants.PROP_ID:
                    return handleDirectStringPropertyFilter(
                            DSL.field(STA_IDENTIFIER_FIELD, String.class),
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(
                            DSL.field(STA_NAME_FIELD, String.class),
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            DSL.field(STA_DESCRIPTION_FIELD, String.class),
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_ENCODINGTYPE:
                case "featureType":
                    if (operator.equals(FilterConstants.ComparisonOperator.PropertyIsEqualTo)) {
                        SelectConditionStep<Record1<Object>> subquery;
                        subquery = ctx
                                .select(DSL.field(DSL.name(FEATURE_TABLE, FK_FORMAT_ID_FIELD)))
                                .from(DSL.table(FEATURE_TABLE))
                                .innerJoin(DSL.table(FORMAT_TABLE))
                                .onKey()
                                .where(
                                        DSL.field(DSL.name(FORMAT_TABLE, STA_DEFINITION_FIELD))
                                                .eq("application/vnd.geo+json")
                                        .or(
                                                DSL.field(DSL.name(FORMAT_TABLE, STA_DEFINITION_FIELD))
                                                        .eq("application/vnd.geo json"))
                                );
                        return DSL.field(FK_FORMAT_ID_FIELD).in(subquery);
                    }
                    return DSL.field(DescribableEntity.PROPERTY_IDENTIFIER).isNotNull();
                default:
                    // We are filtering on variable keys on properties
                    if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                        return handleProperties(
                                propertyName,
                                propertyValue,
                                operator,
                                switched,
                                FK_FEATURE_ID_FIELD,
                                ParameterFactory.EntityType.FEATURE);
                    } else {
                        throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP, propertyName));
                    }
            }
        } catch (STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Condition handleRelatedPropertyFilter(String propertyName, Condition propertyValue) {
        if (OBSERVATIONS.equals(propertyName)) {

            SelectConditionStep<Record1<Object>> subSubquery = ctx.select(DSL.field(FK_DATASTREAM_ID_FIELD))
                    .from(DSL.table(OBSERVATION_TABLE))
                    .where(propertyValue);

            SelectConditionStep<Record1<Object>> subquery = ctx.select(DSL.field(FK_FEATURE_ID_FIELD))
                    .from(DSL.table(DATASTREAM_TABLE))
                    .where(DSL.field(DATASTREAM_ID_FIELD).in(subSubquery));

            return DSL.field(FEATURE_ID_FIELD).in(subquery);
        } else {
            throw new RuntimeException("Could not find related property: " + propertyName);
        }
    }

    @Override
    public Field<Double> handleGeospatial(GeoValueExpr expr, String spatialFunctionName, String argument) {

        if (StaConstants.PROP_FEATURE.equals(expr.getGeometry())) {
            Field<Geometry> geomField = DSL.field(FEATURE_GEOM_FIELD, Geometry.class);
            switch (spatialFunctionName) {
                case ODataConstants.GeoFunctions.GEO_DISTANCE:
                    return GeospatialFunctions.st_distance(geomField, argument);
                case ODataConstants.GeoFunctions.GEO_LENGTH:
                    return GeospatialFunctions.st_length(geomField);
                default:
                    break;

            }
        } else {
            switch (spatialFunctionName) {
                case ODataConstants.GeoFunctions.GEO_DISTANCE:
                    return GeospatialFunctions.st_distance(expr.getGeometry(), argument);
                case ODataConstants.GeoFunctions.GEO_LENGTH:
                    return GeospatialFunctions.st_length(expr.getGeometry());
                default:
                    break;
            }

        }
        throw new RuntimeException("Could not find spatial function: " + spatialFunctionName);
    }

    @Override
    public Condition handleGeoSpatialPropertyFilter(String propertyName,
                                                    String spatialFunctionName,
                                                    String... arguments) {

        if (!StaConstants.PROP_LOCATION.equals(propertyName)) {
            throw new RuntimeException("Could not find property: " + propertyName);
        }

        switch (spatialFunctionName) {
            case ODataConstants.SpatialFunctions.ST_EQUALS:
                return GeospatialFunctions.st_equals(
                        DSL.field(FEATURE_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_DISJOINT:
                return GeospatialFunctions.st_disjoint(
                        DSL.field(FEATURE_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_TOUCHES:
                return GeospatialFunctions.st_touches(
                        DSL.field(FEATURE_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_WITHIN:
                return GeospatialFunctions.st_within(
                        DSL.field(FEATURE_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_OVERLAPS:
                return GeospatialFunctions.st_overlaps(
                        DSL.field(FEATURE_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_CROSSES:
                return GeospatialFunctions.st_crosses(
                        DSL.field(FEATURE_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.GeoFunctions.GEO_INTERSECTS:
                //fallthru
            case ODataConstants.SpatialFunctions.ST_INTERSECTS:
                return GeospatialFunctions.st_intersects(
                        DSL.field(FEATURE_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_CONTAINS:
                return GeospatialFunctions.st_contains(
                        DSL.field(FEATURE_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_RELATE:
                return GeospatialFunctions.st_relate(
                        DSL.field(FEATURE_GEOM_FIELD, Geometry.class),
                        arguments[0],
                        arguments[1]);
            default:
                throw new RuntimeException("Could not find function: " + spatialFunctionName);
        }
    }

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
            // encodingType defined in definition field of format table
            case StaConstants.PROP_ENCODINGTYPE:
                return FK_FORMAT_ID_FIELD;
            default:
                return property;
        }
    }
}
