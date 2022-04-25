/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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
package org.n52.sta.data.old.query;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.location.LocationParameterEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.data.old.util.HibernateSpatialCriteriaBuilder;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class LocationQuerySpecifications extends EntityQuerySpecifications<LocationEntity> implements
        SpatialQuerySpecifications {

    public Specification<LocationEntity> withHistoricalLocationStaIdentifier(String historicalLocationIdentifier) {
        return (root, query, builder) -> {
            Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
            Root<HistoricalLocationEntity> historicalLoc = sq.from(HistoricalLocationEntity.class);
            Join<HistoricalLocationEntity, LocationEntity> joinFeature = historicalLoc
                    .join(HistoricalLocationEntity.PROPERTY_LOCATIONS);
            sq.select(joinFeature)
                    .where(builder.equal(historicalLoc.get(DescribableEntity.PROPERTY_STA_IDENTIFIER),
                            historicalLocationIdentifier));
            return builder.in(root).value(sq);
        };
    }

    public Specification<LocationEntity> withThingStaIdentifier(final String thingIdentifier) {
        return (root, query, builder) -> {
            Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
            Root<PlatformEntity> platform = sq.from(PlatformEntity.class);
            Join<PlatformEntity, LocationEntity> joinFeature = platform.join(PlatformEntity.PROPERTY_LOCATIONS);
            sq.select(joinFeature)
                    .where(builder.equal(platform.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), thingIdentifier));
            return builder.in(root).value(sq);
        };
    }

    /**
     * Copies the arguments provided in arguments to the database function call. If
     * too many arguments are provided
     * they are discarded silently.
     *
     * @param spatialFunctionName name of the function to be called
     * @param arguments           arguments of the function
     * @return Specification of LocationEntity matching
     */
    @Override
    public Specification<LocationEntity> handleGeoSpatialPropertyFilter(
            String propertyName,
            String spatialFunctionName,
            String... arguments) {
        return (Specification<LocationEntity>) (root, query, builder) -> {
            if (!StaConstants.PROP_LOCATION.equals(propertyName)) {
                throw new RuntimeException("Could not find property: " + propertyName);
            }
            if (builder instanceof HibernateSpatialCriteriaBuilder) {
                switch (spatialFunctionName) {
                    case ODataConstants.SpatialFunctions.ST_EQUALS:
                        return ((HibernateSpatialCriteriaBuilder) builder).st_equals(
                                root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY),
                                arguments[0]);
                    case ODataConstants.SpatialFunctions.ST_DISJOINT:
                        return ((HibernateSpatialCriteriaBuilder) builder).st_disjoint(
                                root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY),
                                arguments[0]);
                    case ODataConstants.SpatialFunctions.ST_TOUCHES:
                        return ((HibernateSpatialCriteriaBuilder) builder).st_touches(
                                root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY),
                                arguments[0]);
                    case ODataConstants.SpatialFunctions.ST_WITHIN:
                        return ((HibernateSpatialCriteriaBuilder) builder).st_within(
                                root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY),
                                arguments[0]);
                    case ODataConstants.SpatialFunctions.ST_OVERLAPS:
                        return ((HibernateSpatialCriteriaBuilder) builder).st_overlaps(
                                root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY),
                                arguments[0]);
                    case ODataConstants.SpatialFunctions.ST_CROSSES:
                        return ((HibernateSpatialCriteriaBuilder) builder).st_crosses(
                                root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY),
                                arguments[0]);
                    case ODataConstants.GeoFunctions.GEO_INTERSECTS:
                        // fallthru
                    case ODataConstants.SpatialFunctions.ST_INTERSECTS:
                        return ((HibernateSpatialCriteriaBuilder) builder).st_intersects(
                                root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY),
                                arguments[0]);
                    case ODataConstants.SpatialFunctions.ST_CONTAINS:
                        return ((HibernateSpatialCriteriaBuilder) builder).st_contains(
                                root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY),
                                arguments[0]);
                    case ODataConstants.SpatialFunctions.ST_RELATE:
                        return ((HibernateSpatialCriteriaBuilder) builder).st_relate(
                                root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY),
                                arguments[0],
                                arguments[1]);
                    default:
                        throw new RuntimeException("Could not find function: " + spatialFunctionName);
                }
            } else {
                throw new RuntimeException("Invalid QuerySpecificationBuilder supplied! Spatial support not present!");
            }
        };
    }

    @Override
    public Expression<Float> handleGeospatial(GeoValueExpr expr,
            String spatialFunctionName,
            String argument,
            HibernateSpatialCriteriaBuilder builder,
            Root root) {
        if (StaConstants.PROP_LOCATION.equals(expr.getGeometry())) {
            switch (spatialFunctionName) {
                case ODataConstants.GeoFunctions.GEO_DISTANCE:
                    return builder.st_distance(
                            root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY),
                            argument);
                case ODataConstants.GeoFunctions.GEO_LENGTH:
                    return builder.st_length(root.get(LocationEntity.PROPERTY_GEOMETRY_ENTITY));
                default:
                    break;
            }
        } else {
            switch (spatialFunctionName) {
                case ODataConstants.GeoFunctions.GEO_DISTANCE:
                    return builder.st_distance(
                            expr.getGeometry(),
                            argument);
                case ODataConstants.GeoFunctions.GEO_LENGTH:
                    return builder.st_length(expr.getGeometry());
                default:
                    break;
            }
        }
        throw new RuntimeException("Could not find spatial function: " + spatialFunctionName);
    }

    @Override
    protected Specification<LocationEntity> handleRelatedPropertyFilter(String propertyName,
            Specification<?> propertyValue) {
        return (root, query, builder) -> {
            if (StaConstants.THINGS.equals(propertyName)) {
                Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
                Root<PlatformEntity> thing = sq.from(PlatformEntity.class);
                Join<PlatformEntity, LocationEntity> join = thing.join(PlatformEntity.PROPERTY_LOCATIONS);
                sq.select(join)
                        .where(((Specification<PlatformEntity>) propertyValue).toPredicate(thing,
                                query,
                                builder));
                return builder.in(root).value(sq);
            } else if (StaConstants.HISTORICAL_LOCATIONS.equals(propertyName)) {
                Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
                Root<HistoricalLocationEntity> historicalLocation = sq.from(HistoricalLocationEntity.class);
                Join<HistoricalLocationEntity, LocationEntity> joinFeature = historicalLocation
                        .join(HistoricalLocationEntity.PROPERTY_LOCATIONS);
                sq.select(joinFeature)
                        .where(((Specification<HistoricalLocationEntity>) propertyValue).toPredicate(
                                historicalLocation,
                                query,
                                builder));
                return builder.in(root).value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    @Override
    protected Specification<LocationEntity> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<LocationEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case StaConstants.PROP_ID:
                        return handleDirectStringPropertyFilter(root.get(LocationEntity.STA_IDENTIFIER),
                                propertyValue,
                                operator,
                                builder,
                                false);
                    case StaConstants.PROP_NAME:
                        return handleDirectStringPropertyFilter(root.get(LocationEntity.NAME),
                                propertyValue,
                                operator,
                                builder,
                                switched);
                    case StaConstants.PROP_DESCRIPTION:
                        return handleDirectStringPropertyFilter(root.get(LocationEntity.DESCRIPTION),
                                propertyValue,
                                operator,
                                builder,
                                switched);
                    case StaConstants.PROP_ENCODINGTYPE:
                        Join<LocationEntity, FormatEntity> join = root.join(LocationEntity.PROPERTY_LOCATION_ENCODINT);
                        return handleDirectStringPropertyFilter(join.get(FormatEntity.FORMAT),
                                propertyValue,
                                operator,
                                builder,
                                switched);
                    default:
                        // We are filtering on variable keys on properties
                        if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                            return handleProperties(root,
                                    query,
                                    builder,
                                    propertyName,
                                    propertyValue,
                                    operator,
                                    switched,
                                    LocationParameterEntity.PROP_LOCATION_ID,
                                    ParameterFactory.EntityType.LOCATION);
                        } else {
                            throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP, propertyName));
                        }
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public String checkPropertyName(String property) {
        if (property.equals(StaConstants.PROP_ENCODINGTYPE)) {
            return LocationEntity.PROPERTY_NAME;
        } else if (property.equals(StaConstants.PROP_LOCATION)) {
            return "name desc";
        } else {
            return property;
        }
    }
}
