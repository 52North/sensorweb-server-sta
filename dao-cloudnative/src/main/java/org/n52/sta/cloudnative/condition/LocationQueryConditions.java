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
public class LocationQueryConditions extends EntityQueryConditions implements SpatialQueryConditions {

    public Condition withHistoricalLocationStaIdentifier(String historicalLocationIdentifier) {
        // Join and condition
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(LOCATION_HISTORICAL_LOCATION_TABLE))
                        .innerJoin(DSL.table(HISTORICAL_LOCATION_TABLE))
                        .onKey()
                        .innerJoin(DSL.table(LOCATION_TABLE))
                        .onKey()
                        .where(DSL.field(DSL.name(HISTORICAL_LOCATION_TABLE, STA_IDENTIFIER_FIELD))
                                .eq(historicalLocationIdentifier))
        );
    }

    public Condition withThingStaIdentifier(final String thingIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(DSL.table(LOCATION_TABLE))
                        .innerJoin(DSL.table(THING_LOCATION_TABLE))
                        .onKey()
                        .innerJoin(DSL.table(THING_TABLE))
                        .onKey()
                        .where(DSL.field(DSL.name(THING_TABLE, STA_IDENTIFIER_FIELD))
                                .eq(thingIdentifier))
        );
    }


    /**
     * Copies the arguments provided in arguments to the database function call. If too many arguments are provided
     * they are discarded silently.
     *
     * @param spatialFunctionName name of the function to be called
     * @param arguments           arguments of the function
     * @return Field that represents the database function call
     */
    @Override
    public Condition handleGeoSpatialPropertyFilter(
            String propertyName,
            String spatialFunctionName,
            String... arguments) {

        if (!StaConstants.PROP_LOCATION.equals(propertyName)) {
            throw new RuntimeException("Could not find property: " + propertyName);
        }

        switch (spatialFunctionName) {
            case ODataConstants.SpatialFunctions.ST_EQUALS:
                return GeospatialFunctions.st_equals(
                        DSL.field(LOCATION_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_DISJOINT:
                return GeospatialFunctions.st_disjoint(
                        DSL.field(LOCATION_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_TOUCHES:
                return GeospatialFunctions.st_touches(
                        DSL.field(LOCATION_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_WITHIN:
                return GeospatialFunctions.st_within(
                        DSL.field(LOCATION_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_OVERLAPS:
                return GeospatialFunctions.st_overlaps(
                        DSL.field(LOCATION_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_CROSSES:
                return GeospatialFunctions.st_crosses(
                        DSL.field(LOCATION_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.GeoFunctions.GEO_INTERSECTS:
                //fallthru
            case ODataConstants.SpatialFunctions.ST_INTERSECTS:
                return GeospatialFunctions.st_intersects(
                        DSL.field(LOCATION_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_CONTAINS:
                return GeospatialFunctions.st_contains(
                        DSL.field(LOCATION_GEOM_FIELD, Geometry.class),
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_RELATE:
                return GeospatialFunctions.st_relate(
                        DSL.field(LOCATION_GEOM_FIELD, Geometry.class),
                        arguments[0],
                        arguments[1]);
            default:
                throw new RuntimeException("Could not find function: " + spatialFunctionName);
        }
    }


    @Override
    public Field<Double> handleGeospatial(GeoValueExpr expr,
                                          String spatialFunctionName,
                                          String argument) {

        if (StaConstants.PROP_LOCATION.equals(expr.getGeometry())) {
            Field<Geometry> geomField = DSL.field(LOCATION_GEOM_FIELD, Geometry.class);
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
                    Condition subCondition = handleDirectStringPropertyFilter(
                            DSL.field(DSL.name(FORMAT_TABLE, STA_DEFINITION_FIELD), String.class),
                            propertyValue,
                            operator,
                            switched);
                    SelectConditionStep<Record1<Object>> subquery = ctx
                            .select(DSL.field(DSL.name(LOCATION_TABLE, LOCATION_ID_FIELD)))
                            .from(DSL.table(LOCATION_TABLE))
                            .innerJoin(DSL.table(FORMAT_TABLE))
                            .onKey()
                            .where(subCondition);
                    return DSL.field(LOCATION_ID_FIELD).in(subquery);
                default:
                    // We are filtering on variable keys on properties
                    if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                        return handleProperties(
                                propertyName,
                                propertyValue,
                                operator,
                                switched,
                                FK_LOCATION_ID_FIELD,
                                ParameterFactory.EntityType.LOCATION);
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
        if (THINGS.equals(propertyName)) {
            SelectConditionStep<Record1<Object>> subSubquery = ctx.
                    select(DSL.field(THING_ID_FIELD))
                    .from(DSL.table(THING_TABLE))
                    .where(propertyValue);
            SelectConditionStep<Record1<Object>> subquery = ctx.
                    select(DSL.field(FK_LOCATION_ID_FIELD))
                    .from(DSL.table(THING_LOCATION_TABLE))
                    .where(DSL.field(FK_THING_ID_FIELD).in(subSubquery));
            return DSL.field(LOCATION_ID_FIELD).in(subquery);
        } else if (HISTORICAL_LOCATIONS.equals(propertyName)) {
            SelectConditionStep<Record1<Object>> subSubquery = ctx
                    .select(DSL.field(HISTORICAL_LOCATION_ID_FIELD))
                    .from(DSL.table(HISTORICAL_LOCATION_TABLE))
                    .where(propertyValue);
            SelectConditionStep<Record1<Object>> subquery = ctx
                    .select(DSL.field(FK_LOCATION_ID_FIELD))
                    .from(DSL.table(LOCATION_HISTORICAL_LOCATION_TABLE))
                    .where(DSL.field(FK_HISTORICAL_LOCATION_ID_FIELD).in(subSubquery));
            return DSL.field(LOCATION_ID_FIELD).in(subquery);
        } else {
            throw new RuntimeException("Could not find related property: " + propertyName);
        }

    }

    @Override
    public String checkPropertyName(String property) {
        if (property.equals(StaConstants.PROP_ENCODINGTYPE)) {
            return FK_FORMAT_ID_FIELD;
        } else if (property.equals(StaConstants.PROP_LOCATION)) {
            return "name desc";
        } else {
            return property;
        }
    }
}
