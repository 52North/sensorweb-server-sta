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
public class HistoricalLocationQuerySpecifications extends EntityQuerySpecifications<HistoricalLocationEntity> {

    public Specification<HistoricalLocationEntity> withRelatedLocationIdentifier(final String locationIdentifier) {
        return (root, query, builder) -> {
            final Join<HistoricalLocationEntity, LocationEntity> join =
                    root.join(HistoricalLocationEntity.PROPERTY_LOCATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), locationIdentifier);
        };
    }

    public Specification<HistoricalLocationEntity> withRelatedThingIdentifier(final String thingIdentifier) {
        return (root, query, builder) -> {
            final Join<HistoricalLocationEntity, PlatformEntity> join =
                    root.join(HistoricalLocationEntity.PROPERTY_THING, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), thingIdentifier);
        };
    }

    @Override protected Specification<HistoricalLocationEntity> handleRelatedPropertyFilter(
            String propertyName,
            Specification<?> propertyValue) {
        return (root, query, builder) -> {
            Root<HistoricalLocationEntity> historicalLocationEntityRoot = query.from(HistoricalLocationEntity.class);
            if (THING.equals(propertyName)) {
                Subquery<HistoricalLocationEntity> sq = query.subquery(HistoricalLocationEntity.class);
                Root<PlatformEntity> thing = sq.from(PlatformEntity.class);
                final Join<PlatformEntity, HistoricalLocationEntity> join =
                        historicalLocationEntityRoot.join(HistoricalLocationEntity.PROPERTY_THING, JoinType.INNER);
                sq.select(join)
                  .where(((Specification<PlatformEntity>) propertyValue).toPredicate(thing,
                                                                                     query,
                                                                                     builder));
                return builder.in(root).value(sq);
            } else if (LOCATIONS.equals(propertyName)) {
                Subquery<HistoricalLocationEntity> sq = query.subquery(HistoricalLocationEntity.class);
                Root<LocationEntity> thing = sq.from(LocationEntity.class);
                final Join<LocationEntity, HistoricalLocationEntity> join =
                        historicalLocationEntityRoot.join(HistoricalLocationEntity.PROPERTY_LOCATIONS, JoinType.INNER);
                sq.select(join)
                  .where(((Specification<LocationEntity>) propertyValue).toPredicate(thing,
                                                                                     query,
                                                                                     builder));
                return builder.in(root).value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }

    @Override protected Specification<HistoricalLocationEntity> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<HistoricalLocationEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case "id":
                    return handleDirectStringPropertyFilter(root.get(HistoricalLocationEntity.PROPERTY_IDENTIFIER),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            false);
                case "time":
                    return handleDirectDateTimePropertyFilter(
                            root.get(HistoricalLocationEntity.PROPERTY_TIME),
                            propertyValue,
                            operator,
                            builder);
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
