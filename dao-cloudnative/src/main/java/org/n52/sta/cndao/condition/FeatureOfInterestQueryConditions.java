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
package org.n52.sta.cndao.condition;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.cndao.condition.utils.GeospatialFunctions;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;


import static org.jooq.impl.DSL.*;

public class FeatureOfInterestConditions extends EntityQueryConditions implements SpatialQueryConditions{

    public Condition withObservationStaIdentifier(final String observationIdentifier) {

        return DSL.exists(
                dsl.selectOne()
                        .from(table(FEATURE_TABLE))
                        .innerJoin(table(DATASTREAM_TABLE))
                        .onKey()
                        .innerJoin(table(OBSERVATION_TABLE))
                        .onKey()
                        .where(field(name(OBSERVATION_ID_FIELD, STA_IDENTIFIER_FIELD))
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
                            field(STA_IDENTIFIER_FIELD, String.class),
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(
                            field(STA_NAME_FIELD, String.class),
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            field(STA_DESCRIPTION_FIELD, String.class),
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_ENCODINGTYPE:
                case "featureType":
                    if (operator.equals(FilterConstants.ComparisonOperator.PropertyIsEqualTo)) {
                        SelectConditionStep<Record1<Object>> subquery;
                        subquery = dsl
                                .select(field(name(FEATURE_TABLE, FK_FORMAT_ID_FIELD)))
                                .from(table(FEATURE_TABLE))
                                .innerJoin(table(FORMAT_TABLE))
                                .onKey()
                                .where(
                                        field(name(FORMAT_TABLE, STA_DEFINITION_FIELD))
                                                .eq("application/vnd.geo+json")
                                        .or(
                                                field(name(FORMAT_TABLE, STA_DEFINITION_FIELD))
                                                        .eq(("application/vnd.geo json")))
                                );
                        return field(FK_FORMAT_ID_FIELD).in(subquery);
                    }
                    return field(DescribableEntity.PROPERTY_IDENTIFIER).isNotNull();
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

            SelectConditionStep<Record1<Object>> subSubquery = dsl.select(field(FK_DATASTREAM_ID_FIELD))
                    .from(table(OBSERVATION_TABLE))
                    .where(propertyValue);

            SelectConditionStep<Record1<Object>> subquery = dsl.select(field(FK_FEATURE_ID_FIELD))
                    .from(table(DATASTREAM_TABLE))
                    .where(field(DATASTREAM_ID_FIELD).in(subSubquery));

            return field(FEATURE_ID_FIELD).in(subquery);
        } else {
            throw new RuntimeException("Could not find related property: " + propertyName);
        }
    }

    @Override
    public Field<Double> handleGeospatial(GeoValueExpr expr, String spatialFunctionName, String argument) {

        if (StaConstants.PROP_FEATURE.equals(expr.getGeometry())) {
            Field<?> geomField = field(FEATURE_GEOM_FIELD, Object.class); // Use Geometry.class?
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
    public Field<?> handleGeoSpatialPropertyFilter(String propertyName, String spatialFunctionName, String... arguments) {

        if (!StaConstants.PROP_LOCATION.equals(propertyName)) {
            throw new RuntimeException("Could not find property: " + propertyName);
        }

        switch (spatialFunctionName) {
            case ODataConstants.SpatialFunctions.ST_EQUALS:
                return GeospatialFunctions.st_equals(
                        field(FEATURE_GEOM_FIELD, Object.class), // Use Geometry.class?
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_DISJOINT:
                return GeospatialFunctions.st_disjoint(
                        field(FEATURE_GEOM_FIELD, Object.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_TOUCHES:
                return GeospatialFunctions.st_touches(
                        field(FEATURE_GEOM_FIELD, Object.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_WITHIN:
                return GeospatialFunctions.st_within(
                        field(FEATURE_GEOM_FIELD, Object.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_OVERLAPS:
                return GeospatialFunctions.st_overlaps(
                        field(FEATURE_GEOM_FIELD, Object.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_CROSSES:
                return GeospatialFunctions.st_crosses(
                        field(FEATURE_GEOM_FIELD, Object.class),
                        arguments[0]);
            case ODataConstants.GeoFunctions.GEO_INTERSECTS:
                //fallthru
            case ODataConstants.SpatialFunctions.ST_INTERSECTS:
                return GeospatialFunctions.st_intersects(
                        field(FEATURE_GEOM_FIELD, Object.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_CONTAINS:
                return GeospatialFunctions.st_contains(
                        field(FEATURE_GEOM_FIELD, Object.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_RELATE:
                return GeospatialFunctions.st_relate(
                        field(FEATURE_GEOM_FIELD, Object.class),
                        arguments[0],
                        arguments[1]);
            default:
                throw new RuntimeException("Could not find function: " + spatialFunctionName);
        }
    }

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ENCODINGTYPE:
                return FK_FORMAT_ID_FIELD; // encodingType defined in definition field of format table
            default:
                return property;
        }
    }
}
