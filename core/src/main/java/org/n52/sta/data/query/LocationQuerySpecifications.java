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
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class LocationQuerySpecifications extends EntityQuerySpecifications<LocationEntity> {

    public Specification<LocationEntity> withRelatedHistoricalLocationIdentifier(String historicalLocationIdentifier) {
        return (root, query, builder) -> {
            Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
            Root<HistoricalLocationEntity> historicalLoc = sq.from(HistoricalLocationEntity.class);
            Join<HistoricalLocationEntity, LocationEntity> joinFeature =
                    historicalLoc.join(HistoricalLocationEntity.PROPERTY_LOCATIONS);
            sq.select(joinFeature)
              .where(builder.equal(historicalLoc.get(DescribableEntity.PROPERTY_IDENTIFIER),
                                   historicalLocationIdentifier));
            return builder.in(root).value(sq);
        };
    }

    public Specification<LocationEntity> withRelatedThingIdentifier(final String thingIdentifier) {
        return (root, query, builder) -> {
            Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
            Root<PlatformEntity> platform = sq.from(PlatformEntity.class);
            Join<PlatformEntity, LocationEntity> joinFeature = platform.join(PlatformEntity.PROPERTY_LOCATIONS);
            sq.select(joinFeature)
              .where(builder.equal(platform.get(DescribableEntity.PROPERTY_IDENTIFIER), thingIdentifier));
            return builder.in(root).value(sq);
        };
    }

    @Override protected Specification<LocationEntity> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<LocationEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case "id":
                    return handleDirectStringPropertyFilter(root.get(LocationEntity.PROPERTY_IDENTIFIER),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            false);
                case "name":
                    return handleDirectStringPropertyFilter(root.get(DescribableEntity.PROPERTY_NAME),
                                                            propertyValue, operator, builder, switched);
                case "description":
                    return handleDirectStringPropertyFilter(
                            root.get(DescribableEntity.PROPERTY_DESCRIPTION), propertyValue,
                            operator, builder, switched);
                case "encodingType":
                    Join<LocationEntity, FormatEntity> join =
                            root.join(LocationEntity.PROPERTY_LOCATION_ENCODINT);
                    return handleDirectStringPropertyFilter(
                            join.get(FormatEntity.FORMAT),
                            propertyValue, operator, builder, switched);
                default:
                    throw new RuntimeException("Error getting filter for Property: \"" + propertyName
                                                       + "\". No such property in Entity.");
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override protected Specification<LocationEntity> handleRelatedPropertyFilter(String propertyName,
                                                                                  Specification<?> propertyValue) {
        return (root, query, builder) -> {
            if (THINGS.equals(propertyName)) {
                Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
                Root<PlatformEntity> thing = sq.from(PlatformEntity.class);
                Join<PlatformEntity, LocationEntity> joinFeature = thing.join(PlatformEntity.PROPERTY_LOCATIONS);
                sq.select(joinFeature)
                  .where(((Specification<PlatformEntity>) propertyValue).toPredicate(thing,
                                                                                     query,
                                                                                     builder));
                return builder.in(root).value(sq);
            } else if (HISTORICAL_LOCATIONS.equals(propertyName)) {
                Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
                Root<HistoricalLocationEntity> historicalLocation = sq.from(HistoricalLocationEntity.class);
                Join<HistoricalLocationEntity, LocationEntity> joinFeature =
                        historicalLocation.join(HistoricalLocationEntity.PROPERTY_LOCATIONS);
                sq.select(joinFeature)
                  .where(((Specification<HistoricalLocationEntity>) propertyValue).toPredicate(historicalLocation,
                                                                                               query,
                                                                                               builder));
                return builder.in(root).value(sq);
            } else {
                throw new RuntimeException("Could not find related property: " + propertyName);
            }
        };
    }
}
