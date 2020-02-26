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

import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ThingQuerySpecifications extends EntityQuerySpecifications<PlatformEntity> {

    public Specification<PlatformEntity> withRelatedLocationIdentifier(final String locationIdentifier) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, LocationEntity> join =
                    root.join(PlatformEntity.PROPERTY_LOCATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), locationIdentifier);
        };
    }

    public Specification<PlatformEntity> withRelatedHistoricalLocationIdentifier(final String historicalIdentifier) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, HistoricalLocationEntity> join =
                    root.join(PlatformEntity.PROPERTY_HISTORICAL_LOCATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), historicalIdentifier);
        };
    }

    public Specification<PlatformEntity> withRelatedDatastreamIdentifier(final String datastreamIdentifier) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, DatastreamEntity> join =
                    root.join(PlatformEntity.PROPERTY_DATASTREAMS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), datastreamIdentifier);
        };
    }

    @Override
    public Specification<String> getIdSubqueryWithFilter(Specification filter) {
        return this.toSubquery(PlatformEntity.class, PlatformEntity.PROPERTY_IDENTIFIER, filter);
    }

    @Override
    public Specification<PlatformEntity> getFilterForProperty(String propertyName,
                                                              Expression<?> propertyValue,
                                                              FilterConstants.ComparisonOperator operator,
                                                              boolean switched) {
        if (propertyName.equals(DATASTREAMS) || propertyName.equals(LOCATIONS)
                || propertyName.equals(HISTORICAL_LOCATIONS)) {
            return handleRelatedPropertyFilter(propertyName, propertyValue);
        } else if (propertyName.equals("id")) {
            return (root, query, builder) -> {
                try {
                    return handleDirectStringPropertyFilter(root.get(PlatformEntity.PROPERTY_IDENTIFIER),
                                                            propertyValue, operator, builder, false);
                } catch (STAInvalidFilterExpressionException e) {
                    throw new RuntimeException(e);
                }
            };
        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
        }
    }

    private Specification<PlatformEntity> handleRelatedPropertyFilter(String propertyName,
                                                                      Expression<?> propertyValue) {
        return (root, query, builder) -> {
            try {
                switch (propertyName) {
                case DATASTREAMS: {
                    Subquery<DatastreamEntity> subquery = query.subquery(DatastreamEntity.class);
                    Root<DatastreamEntity> datastream = subquery.from(DatastreamEntity.class);
                    subquery.select(datastream.get(DatastreamEntity.PROPERTY_ID))
                            .where(builder.equal(datastream.get(DatastreamEntity.PROPERTY_IDENTIFIER), propertyValue));
                    return builder.in(root.get(PlatformEntity.PROPERTY_DATASTREAMS)).value(subquery);
                    // return
                    // qPlatform.datastreamEntities.any().id.eqAny(propertyValue);
                }
                case LOCATIONS: {
                    Subquery<LocationEntity> subquery = query.subquery(LocationEntity.class);
                    Root<LocationEntity> location = subquery.from(LocationEntity.class);
                    subquery.select(location.get(LocationEntity.PROPERTY_ID))
                            .where(builder.equal(location.get(LocationEntity.PROPERTY_IDENTIFIER), propertyValue));
                    return builder.in(root.get(PlatformEntity.PROPERTY_LOCATIONS)).value(subquery);
                    // return
                    // qPlatform.locationEntities.any().id.eqAny(propertyValue);
                }
                case HISTORICAL_LOCATIONS:
                    Subquery<HistoricalLocationEntity> subquery = query.subquery(HistoricalLocationEntity.class);
                    Root<HistoricalLocationEntity> historicalLocation =
                            subquery.from(HistoricalLocationEntity.class);
                    subquery.select(historicalLocation.get(HistoricalLocationEntity.PROPERTY_ID))
                            .where(builder.equal(
                                    historicalLocation.get(HistoricalLocationEntity.PROPERTY_IDENTIFIER),
                                    propertyValue));
                    return builder.in(root.get(PlatformEntity.PROPERTY_HISTORICAL_LOCATIONS)).value(subquery);
                // return
                // qPlatform.historicalLocationEntities.any().id.eqAny(propertyValue);
                default:
                    throw new STAInvalidFilterExpressionException(
                            "Filtering by Related Properties with cardinality >1 is currently not supported!");
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Specification<PlatformEntity> handleDirectPropertyFilter(String propertyName,
                                                                     Expression<?> propertyValue,
                                                                     FilterConstants.ComparisonOperator operator,
                                                                     boolean switched) {
        return (Specification<PlatformEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case "name":
                    return handleDirectStringPropertyFilter(root.get(DescribableEntity.PROPERTY_NAME),
                                                            propertyValue, operator, builder, switched);
                case "description":
                    return handleDirectStringPropertyFilter(
                            root.get(DescribableEntity.PROPERTY_DESCRIPTION),
                            propertyValue,
                            operator,
                            builder,
                            switched);
                case "properties":
                    // TODO
                    // qPlatform.parameters.any().name.eq("properties")
                    return handleDirectStringPropertyFilter(
                            root.get(PlatformEntity.PROPERTY_PROPERTIES),
                            propertyValue,
                            operator,
                            builder,
                            switched);
                default:
                    throw new RuntimeException("Error getting filter for Property: \"" + propertyName
                                                       + "\". No such property in Entity.");
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
