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
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.data.cloudnative.condition.utils.GeospatialFunctions;
import org.n52.sta.data.cloudnative.schema.tables.Dataset;
import org.n52.sta.data.cloudnative.schema.tables.Feature;
import org.n52.sta.data.cloudnative.schema.tables.FeatureParameter;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author <a href="mailto:humaid.kidwai@ucalgary.ca">Humaid Kidwai</a>
 */
@Component
public class FeatureOfInterestQueryConditions extends EntityQueryConditions implements SpatialQueryConditions {

    public static Feature StaEntity = FEATURE_OF_INTEREST;

    public Condition withObservationStaIdentifier(final String observationIdentifier) {

        return DSL.exists(
                ctx.select(OBSERVATION.OBSERVATION_ID)
                        .from(FEATURE_OF_INTEREST)
                        .join(DATASTREAM)
                        .onKey()
                        .join(OBSERVATION)
                        .onKey()
                        .where(OBSERVATION.STA_IDENTIFIER.eq(observationIdentifier))
        );
    }

    @Override
    public Condition withStaIdentifier(String staIdentifier) {
        return StaEntity.STA_IDENTIFIER.eq(staIdentifier);
    }

    public Condition withName(String name) {
        return StaEntity.NAME.eq(name);
    }

    @Override
    public Condition withStaIdentifier(List<String> identifiers) {
        return StaEntity.STA_IDENTIFIER.in(identifiers);
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
                            FEATURE_OF_INTEREST.STA_IDENTIFIER,
                            propertyValue,
                            operator,
                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(
                            FEATURE_OF_INTEREST.NAME,
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            FEATURE_OF_INTEREST.DESCRIPTION,
                            propertyValue,
                            operator,
                            switched);
                case StaConstants.PROP_ENCODINGTYPE:
                case "featureType":
                    if (operator.equals(FilterConstants.ComparisonOperator.PropertyIsEqualTo)) {
                        SelectConditionStep<Record1<Long>> subquery;
                        subquery = ctx
                                .select(FEATURE_OF_INTEREST.FK_FORMAT_ID)
                                .from(FEATURE_OF_INTEREST)
                                .join(FORMAT)
                                .onKey()
                                .where(FORMAT.DEFINITION.eq("application/vnd.geo+json")
                                        .or(FORMAT.DEFINITION.eq("application/vnd.geo json")));
                        return FEATURE_OF_INTEREST.FK_FORMAT_ID.in(subquery);
                    }
                    return FEATURE_OF_INTEREST.IDENTIFIER.isNotNull();
                default:
                    // We are filtering on variable keys on properties
                    if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                        return handleProperties(
                                propertyName,
                                propertyValue,
                                operator,
                                switched,
                                FEATURE_PROPERTIES.FK_FEATURE_ID,
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
        if (STAEntityDefinition.OBSERVATIONS.equals(propertyName)) {

            SelectConditionStep<Record1<Long>> subSubquery = ctx
                    .select(OBSERVATION.FK_DATASET_ID)
                    .from(OBSERVATION)
                    .where(propertyValue);

            SelectConditionStep<Record1<Long>> subquery = ctx
                    .select(DATASTREAM.FK_FEATURE_ID)
                    .from(DATASTREAM)
                    .where(DATASTREAM.DATASET_ID.in(subSubquery));

            return FEATURE_OF_INTEREST.FEATURE_ID.in(subquery);
        } else {
            throw new RuntimeException("Could not find related property: " + propertyName);
        }
    }

    @Override
    public Field<Double> handleGeospatial(GeoValueExpr expr, String spatialFunctionName, String argument) {

        if (StaConstants.PROP_FEATURE.equals(expr.getGeometry())) {
            switch (spatialFunctionName) {
                case ODataConstants.GeoFunctions.GEO_DISTANCE:
                    return GeospatialFunctions.st_distance(FEATURE_OF_INTEREST.GEOM, argument);
                case ODataConstants.GeoFunctions.GEO_LENGTH:
                    return GeospatialFunctions.st_length(FEATURE_OF_INTEREST.GEOM);
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

        if (!StaConstants.PROP_FEATURE.equals(propertyName)) {
            throw new RuntimeException("Could not find property: " + propertyName);
        }

        switch (spatialFunctionName) {
            case ODataConstants.SpatialFunctions.ST_EQUALS:
                return GeospatialFunctions.st_equals(
                        FEATURE_OF_INTEREST.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_DISJOINT:
                return GeospatialFunctions.st_disjoint(
                        FEATURE_OF_INTEREST.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_TOUCHES:
                return GeospatialFunctions.st_touches(
                        FEATURE_OF_INTEREST.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_WITHIN:
                return GeospatialFunctions.st_within(
                        FEATURE_OF_INTEREST.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_OVERLAPS:
                return GeospatialFunctions.st_overlaps(
                        FEATURE_OF_INTEREST.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_CROSSES:
                return GeospatialFunctions.st_crosses(
                        FEATURE_OF_INTEREST.GEOM,
                        arguments[0]);
            case ODataConstants.GeoFunctions.GEO_INTERSECTS:
                //fallthru
            case ODataConstants.SpatialFunctions.ST_INTERSECTS:
                return GeospatialFunctions.st_intersects(
                        FEATURE_OF_INTEREST.GEOM,
                        arguments[0]);
            case ODataConstants.SpatialFunctions.ST_CONTAINS:
                return GeospatialFunctions.st_contains(
                        FEATURE_OF_INTEREST.GEOM,
                        arguments[0]);
            default:
                throw new RuntimeException("Could not find function: " + spatialFunctionName);
        }
    }

    @Override
    public Field checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_ID:
                return FEATURE_OF_INTEREST.STA_IDENTIFIER;
            case StaConstants.PROP_NAME:
                return FEATURE_OF_INTEREST.NAME;
            case StaConstants.PROP_DESCRIPTION:
                return FEATURE_OF_INTEREST.DESCRIPTION;
            case StaConstants.PROP_FEATURE:
                return FEATURE_OF_INTEREST.GEOM;
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
