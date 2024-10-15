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
package org.n52.sta.data.cloudnative.condition;

import org.jooq.*;
import org.jooq.impl.DSL;

import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.cloudnative.condition.utils.GeospatialFunctions;
import org.n52.sta.data.cloudnative.schema.tables.LocationParameter;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class LocationQueryConditions extends EntityQueryConditions implements SpatialQueryConditions {

    public Condition withHistoricalLocationStaIdentifier(String historicalLocationIdentifier) {
        // Join and condition
        return DSL.exists(
                ctx.selectOne()
                        .from(LOCATION_HISTORICAL_LOCATION)
                        .join(HISTORICAL_LOCATION)
                        .onKey()
                        .where(HISTORICAL_LOCATION.STA_IDENTIFIER.eq(historicalLocationIdentifier))
                        .and(LOCATION.LOCATION_ID.eq(LOCATION_HISTORICAL_LOCATION.FK_LOCATION_ID))
        );
    }

    public Condition withThingStaIdentifier(final String thingIdentifier) {
        return DSL.exists(
                ctx.selectOne()
                        .from(THING_LOCATION)
                        .join(THING)
                        .onKey()
                        .where(THING.STA_IDENTIFIER.eq(thingIdentifier))
                        .and(LOCATION.LOCATION_ID.eq(THING_LOCATION.FK_LOCATION_ID))
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
                        LOCATION.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_DISJOINT:
                return GeospatialFunctions.st_disjoint(
                        LOCATION.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_TOUCHES:
                return GeospatialFunctions.st_touches(
                        LOCATION.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_WITHIN:
                return GeospatialFunctions.st_within(
                        LOCATION.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_OVERLAPS:
                return GeospatialFunctions.st_overlaps(
                        LOCATION.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_CROSSES:
                return GeospatialFunctions.st_crosses(
                        LOCATION.GEOM,
                        arguments[0]);
            case ODataConstants.GeoFunctions.GEO_INTERSECTS:
                //fallthru
            case ODataConstants.SpatialFunctions.ST_INTERSECTS:
                return GeospatialFunctions.st_intersects(
                        LOCATION.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_CONTAINS:
                return GeospatialFunctions.st_contains(
                        LOCATION.GEOM,
                        arguments[0]);
            default:
                throw new RuntimeException("Could not find function: " + spatialFunctionName);
        }
    }


    @Override
    public Field<Double> handleGeospatial(GeoValueExpr expr,
                                          String spatialFunctionName,
                                          String argument) {

        if (StaConstants.PROP_LOCATION.equals(expr.getGeometry())) {
            switch (spatialFunctionName) {
                case ODataConstants.GeoFunctions.GEO_DISTANCE:
                    return GeospatialFunctions.st_distance(LOCATION.GEOM, argument);
                case ODataConstants.GeoFunctions.GEO_LENGTH:
                    return GeospatialFunctions.st_length(LOCATION.GEOM);
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
    public Condition withStaIdentifier(String staIdentifier) {
        return LOCATION.STA_IDENTIFIER.eq(staIdentifier);
    }

    @Override
    public Condition withStaIdentifier(List<String> identifiers) {
        return LOCATION.STA_IDENTIFIER.in(identifiers);
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
                            LOCATION.STA_IDENTIFIER,
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(
                            LOCATION.NAME,
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            LOCATION.DESCRIPTION,
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_ENCODINGTYPE:
                    Condition subCondition = handleDirectStringPropertyFilter(
                            FORMAT.DEFINITION,
                            propertyValue,
                            operator,
                            switched);
                    return LOCATION.LOCATION_ID.in(
                            ctx
                            .select(LOCATION.LOCATION_ID)
                            .from(LOCATION)
                            .join(FORMAT)
                            .onKey()
                            .where(subCondition)
                    );
                default:
                    // We are filtering on variable keys on properties
                    if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                        return handleProperties(
                                propertyName,
                                propertyValue,
                                operator,
                                switched,
                                LocationParameter.LOCATION_PARAMETER.FK_LOCATION_ID,
                                ParameterFactory.EntityType.LOCATION);
                    } else {
                        throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP,
                                propertyName));
                    }
            }
        } catch (STAInvalidFilterExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Condition handleRelatedPropertyFilter(String propertyName, Condition propertyValue) {
        if (STAEntityDefinition.THINGS.equals(propertyName)) {
            SelectConditionStep<Record1<Long>> subSubquery = ctx.
                    select(THING.PLATFORM_ID)
                    .from(THING)
                    .where(propertyValue);
            SelectConditionStep<Record1<Long>> subquery = ctx.
                    select(THING_LOCATION.FK_LOCATION_ID)
                    .from(THING_LOCATION)
                    .where(THING_LOCATION.FK_PLATFORM_ID.in(subSubquery));
            return LOCATION.LOCATION_ID.in(subquery);
        } else if (STAEntityDefinition.HISTORICAL_LOCATIONS.equals(propertyName)) {
            SelectConditionStep<Record1<Long>> subSubquery = ctx
                    .select(HISTORICAL_LOCATION.HISTORICAL_LOCATION_ID)
                    .from(HISTORICAL_LOCATION)
                    .where(propertyValue);
            SelectConditionStep<Record1<Long>> subquery = ctx
                    .select(LOCATION_HISTORICAL_LOCATION.FK_LOCATION_ID)
                    .from(LOCATION_HISTORICAL_LOCATION)
                    .where(LOCATION_HISTORICAL_LOCATION.FK_HISTORICAL_LOCATION_ID.in(subSubquery));
            return LOCATION.LOCATION_ID.in(subquery);
        } else {
            throw new RuntimeException("Could not find related property: " + propertyName);
        }
    }

    @Override
    public Field<?> checkAliasedPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ID:
                return StaEntity.alias(LOCATION, LOCATION.STA_IDENTIFIER);
            case StaConstants.PROP_NAME:
                return StaEntity.alias(LOCATION, LOCATION.NAME);
            case StaConstants.PROP_DESCRIPTION:
                return StaEntity.alias(LOCATION, LOCATION.DESCRIPTION);
            case StaConstants.PROP_LOCATION:
                return StaEntity.alias(LOCATION, LOCATION.GEOM);
            case StaConstants.PROP_ENCODINGTYPE:
                return FORMAT.DEFINITION;
            case StaConstants.PROP_PROPERTIES:
                // TODO
                return null;
            default:
                return null;
        }
    }

    @Override
    public Field<?> checkOriginalPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ID:
                return LOCATION.STA_IDENTIFIER;
            case StaConstants.PROP_NAME:
                return LOCATION.NAME;
            case StaConstants.PROP_DESCRIPTION:
                return LOCATION.DESCRIPTION;
            case StaConstants.PROP_LOCATION:
                return LOCATION.GEOM;
            case StaConstants.PROP_ENCODINGTYPE:
                return FORMAT.DEFINITION;
            case StaConstants.PROP_PROPERTIES:
                // TODO
                return null;
            default:
                return null;
        }
    }
}
