/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.query;

import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.ObservationEntity;
import org.n52.shetland.oasis.odata.ODataConstants;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.n52.sta.data.service.util.HibernateSpatialCriteriaBuilder;
import org.n52.svalbard.odata.core.expr.GeoValueExpr;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class FeatureOfInterestQuerySpecifications extends EntityQuerySpecifications<AbstractFeatureEntity<?>>
        implements SpatialQuerySpecifications {

    private static final String FEATURE = "feature";

    @Override
    public String checkPropertyName(String property) {
        switch (property) {
        case StaConstants.PROP_ENCODINGTYPE:
            return AbstractFeatureEntity.PROPERTY_FEATURE_TYPE;
        default:
            return property;
        }
    }

    public Specification<AbstractFeatureEntity<?>> withObservationStaIdentifier(final String observationIdentifier) {
        return (root, query, builder) -> {
            Subquery<Long> sqFeature = query.subquery(Long.class);
            Root<DatasetEntity> dataset = sqFeature.from(DatasetEntity.class);
            Subquery<DatasetEntity> sqDataset = query.subquery(DatasetEntity.class);
            Root<ObservationEntity> data = sqDataset.from(ObservationEntity.class);
            sqDataset.select(data.get(ObservationEntity.PROPERTY_DATASET))
                     .where(builder.equal(data.get(ObservationEntity.PROPERTY_STA_IDENTIFIER), observationIdentifier));
            sqFeature.select(dataset.get(DatasetEntity.PROPERTY_FEATURE)).where(builder.in(dataset).value(sqDataset));
            return builder.in(root.get(AbstractFeatureEntity.PROPERTY_ID)).value(sqFeature);
        };
    }

    @Override protected Specification<AbstractFeatureEntity<?>> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<AbstractFeatureEntity<?>>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case StaConstants.PROP_ID:
                    return handleDirectStringPropertyFilter(root.get(AbstractFeatureEntity.STA_IDENTIFIER),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(root.get(AbstractFeatureEntity.NAME),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(root.get(AbstractFeatureEntity.DESCRIPTION),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case StaConstants.PROP_ENCODINGTYPE:
                case "featureType":
                    if (operator.equals(FilterConstants.ComparisonOperator.PropertyIsEqualTo)) {
                        return builder.or(builder.equal(propertyValue, "application/vnd.geo+json"),
                                          builder.equal(propertyValue, "application/vnd.geo json"));
                    }
                    return builder.isNotNull(root.get(DescribableEntity.PROPERTY_IDENTIFIER));
                default:
                    throw new RuntimeException("Error getting filter for Property: \"" + propertyName
                                                       + "\". No such property in Entity.");
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override protected Specification<AbstractFeatureEntity<?>> handleRelatedPropertyFilter(
            String propertyName,
            Specification<?> propertyValue) {
        return (root, query, builder) -> {
            if (StaConstants.OBSERVATIONS.equals(propertyName)) {
                Subquery<Long> sqFeature = query.subquery(Long.class);
                Root<DatasetEntity> dataset = sqFeature.from(DatasetEntity.class);
                Subquery<DatasetEntity> sqDataset = query.subquery(DatasetEntity.class);
                Root<ObservationEntity> data = sqDataset.from(ObservationEntity.class);
                sqDataset.select(dataset)
                         .where(((Specification<ObservationEntity>) propertyValue).toPredicate(data,
                                                                                               query,
                                                                                               builder));

                sqFeature.select(dataset.get(DatasetEntity.PROPERTY_FEATURE))
                         .where(builder.in(dataset).value(sqDataset));
                return builder.in(root.get(AbstractFeatureEntity.PROPERTY_ID)).value(sqFeature);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    /**
     * Copies the arguments provided in arguments to the database function call. If too many arguments are provided
     * they are discarded silently.
     *
     * @param spatialFunctionName name of the function to be called
     * @param arguments           arguments of the function
     * @return Specification of LocationEntity matching
     */
    @Override public Specification<AbstractFeatureEntity<?>> handleGeoSpatialPropertyFilter(
            String propertyName,
            String spatialFunctionName,
            String... arguments) {
        return (Specification<AbstractFeatureEntity<?>>) (root, query, builder) -> {
            if (!FEATURE.equals(propertyName)) {
                throw new RuntimeException("Could not find property: " + propertyName);
            }
            if (builder instanceof HibernateSpatialCriteriaBuilder) {
                switch (spatialFunctionName) {
                case ODataConstants.SpatialFunctions.ST_EQUALS:
                    return ((HibernateSpatialCriteriaBuilder) builder).st_equals(
                            root.get(AbstractFeatureEntity.PROPERTY_GEOMETRY_ENTITY),
                            arguments[0]);
                case ODataConstants.SpatialFunctions.ST_DISJOINT:
                    return ((HibernateSpatialCriteriaBuilder) builder).st_disjoint(
                            root.get(AbstractFeatureEntity.PROPERTY_GEOMETRY_ENTITY),
                            arguments[0]);
                case ODataConstants.SpatialFunctions.ST_TOUCHES:
                    return ((HibernateSpatialCriteriaBuilder) builder).st_touches(
                            root.get(AbstractFeatureEntity.PROPERTY_GEOMETRY_ENTITY),
                            arguments[0]);
                case ODataConstants.SpatialFunctions.ST_WITHIN:
                    return ((HibernateSpatialCriteriaBuilder) builder).st_within(
                            root.get(AbstractFeatureEntity.PROPERTY_GEOMETRY_ENTITY),
                            arguments[0]);
                case ODataConstants.SpatialFunctions.ST_OVERLAPS:
                    return ((HibernateSpatialCriteriaBuilder) builder).st_overlaps(
                            root.get(AbstractFeatureEntity.PROPERTY_GEOMETRY_ENTITY),
                            arguments[0]);
                case ODataConstants.SpatialFunctions.ST_CROSSES:
                    return ((HibernateSpatialCriteriaBuilder) builder).st_crosses(
                            root.get(AbstractFeatureEntity.PROPERTY_GEOMETRY_ENTITY),
                            arguments[0]);
                case ODataConstants.GeoFunctions.GEO_INTERSECTS:
                    //fallthru
                case ODataConstants.SpatialFunctions.ST_INTERSECTS:
                    return ((HibernateSpatialCriteriaBuilder) builder).st_intersects(
                            root.get(AbstractFeatureEntity.PROPERTY_GEOMETRY_ENTITY),
                            arguments[0]);
                case ODataConstants.SpatialFunctions.ST_CONTAINS:
                    return ((HibernateSpatialCriteriaBuilder) builder).st_contains(
                            root.get(AbstractFeatureEntity.PROPERTY_GEOMETRY_ENTITY),
                            arguments[0]);
                case ODataConstants.SpatialFunctions.ST_RELATE:
                    return ((HibernateSpatialCriteriaBuilder) builder).st_relate(
                            root.get(AbstractFeatureEntity.PROPERTY_GEOMETRY_ENTITY),
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

    @Override public Expression<Float> handleGeospatial(GeoValueExpr expr,
                                                        String spatialFunctionName,
                                                        String argument,
                                                        HibernateSpatialCriteriaBuilder builder,
                                                        Root root) {
        if (FEATURE.equals(expr.getGeometry())) {
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
}
