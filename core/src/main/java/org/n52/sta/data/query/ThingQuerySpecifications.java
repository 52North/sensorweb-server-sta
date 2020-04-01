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

    @Override protected Specification<PlatformEntity> handleRelatedPropertyFilter(String propertyName,
                                                                                  Specification<?> propertyValue) {
        return (root, query, builder) -> {
            try {
                Root<PlatformEntity> platformEntityRoot = query.from(PlatformEntity.class);
                switch (propertyName) {
                case DATASTREAMS: {
                    Subquery<DatastreamEntity> subquery = query.subquery(DatastreamEntity.class);
                    Root<DatastreamEntity> datastream = subquery.from(DatastreamEntity.class);
                    final Join<PlatformEntity, DatastreamEntity> join =
                            platformEntityRoot.join(PlatformEntity.PROPERTY_DATASTREAMS, JoinType.INNER);
                    subquery.select(join)
                            .where(((Specification<DatastreamEntity>) propertyValue).toPredicate(datastream,
                                                                                                 query,
                                                                                                 builder));
                    return builder.in(join).value(subquery);
                }
                case LOCATIONS: {
                    Subquery<LocationEntity> subquery = query.subquery(LocationEntity.class);
                    Root<LocationEntity> location = subquery.from(LocationEntity.class);
                    subquery.select(location.get(LocationEntity.PROPERTY_ID))
                            .where(((Specification<LocationEntity>) propertyValue).toPredicate(location,
                                                                                               query,
                                                                                               builder));
                    final Join<PlatformEntity, LocationEntity> join =
                            platformEntityRoot.join(PlatformEntity.PROPERTY_LOCATIONS, JoinType.INNER);
                    return builder.in(join.get(DescribableEntity.PROPERTY_ID)).value(subquery);
                }
                case HISTORICAL_LOCATIONS:
                    Subquery<HistoricalLocationEntity> subquery = query.subquery(HistoricalLocationEntity.class);
                    Root<HistoricalLocationEntity> historicalLocation =
                            subquery.from(HistoricalLocationEntity.class);
                    subquery.select(historicalLocation.get(HistoricalLocationEntity.PROPERTY_ID))
                            .where(((Specification<HistoricalLocationEntity>) propertyValue).toPredicate(
                                    historicalLocation,
                                    query,
                                    builder));
                    final Join<PlatformEntity, HistoricalLocationEntity> join =
                            platformEntityRoot.join(PlatformEntity.PROPERTY_HISTORICAL_LOCATIONS, JoinType.INNER);
                    return builder.in(join.get(DescribableEntity.PROPERTY_ID)).value(subquery);
                default:
                    throw new STAInvalidFilterExpressionException("Could not find related property: " + propertyName);
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override protected Specification<PlatformEntity> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<PlatformEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case "id":
                    return handleDirectStringPropertyFilter(root.get(PlatformEntity.PROPERTY_IDENTIFIER),
                                                            propertyValue, operator, builder, false);
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
