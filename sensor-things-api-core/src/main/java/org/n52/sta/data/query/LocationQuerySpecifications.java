/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEncodingEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.data.jpa.domain.Specification;

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
            Root<HistoricalLocationEntity> dataset = sq.from(HistoricalLocationEntity.class);
            Join<HistoricalLocationEntity, LocationEntity> joinFeature =
                    dataset.join(HistoricalLocationEntity.PROPERTY_LOCATIONS);
            sq.select(joinFeature)
              .where(builder.equal(dataset.get(DescribableEntity.PROPERTY_IDENTIFIER), historicalLocationIdentifier));
            return builder.in(root).value(sq);
        };
    }

    public Specification<LocationEntity> withRelatedThingIdentifier(final String thingIdentifier) {
        return (root, query, builder) -> {
            Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
            Root<PlatformEntity> dataset = sq.from(PlatformEntity.class);
            Join<PlatformEntity, LocationEntity> joinFeature = dataset.join(PlatformEntity.PROPERTY_LOCATIONS);
            sq.select(joinFeature)
              .where(builder.equal(dataset.get(DescribableEntity.PROPERTY_IDENTIFIER), thingIdentifier));
            return builder.in(root).value(sq);
        };
    }

    @Override
    public Specification<String> getIdSubqueryWithFilter(Specification filter) {
        return this.toSubquery(LocationEntity.class, LocationEntity.PROPERTY_IDENTIFIER, filter);
    }

    @Override
    public Specification<LocationEntity> getFilterForProperty(String propertyName,
                                                              Object propertyValue,
                                                              BinaryOperatorKind operator,
                                                              boolean switched) {
        if (propertyName.equals(THINGS) || propertyName.equals(HISTORICAL_LOCATIONS)) {
            return handleRelatedPropertyFilter(propertyName, (Specification<String>) propertyValue);
        } else if (propertyName.equals("id")) {
            return (root, query, builder) -> {
                try {
                    return handleDirectStringPropertyFilter(root.get(LocationEntity.PROPERTY_IDENTIFIER),
                            propertyValue.toString(), operator, builder, false);
                } catch (ExpressionVisitException e) {
                    throw new RuntimeException(e);
                }
                //
            };
        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
        }
    }

    private Specification<LocationEntity> handleRelatedPropertyFilter(String propertyName,
                                                                      Specification<String> propertyValue) {
        return (root, query, builder) -> {
            if (propertyName.equals(THINGS)) {
                Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
                Root<PlatformEntity> dataset = sq.from(PlatformEntity.class);
                Join<PlatformEntity, LocationEntity> joinFeature = dataset.join(PlatformEntity.PROPERTY_LOCATIONS);
                sq.select(joinFeature)
                  .where(builder.equal(dataset.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue));
                return builder.in(root).value(sq);
            } else {
                Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
                Root<HistoricalLocationEntity> dataset = sq.from(HistoricalLocationEntity.class);
                Join<HistoricalLocationEntity, LocationEntity> joinFeature =
                        dataset.join(HistoricalLocationEntity.PROPERTY_LOCATIONS);
                sq.select(joinFeature)
                  .where(builder.equal(dataset.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue));
                return builder.in(root).value(sq);
            }
        };
    }

    private Specification<LocationEntity> handleDirectPropertyFilter(String propertyName, Object propertyValue,
                                                                     BinaryOperatorKind operator, boolean switched) {
        return (Specification<LocationEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case "name":
                        return handleDirectStringPropertyFilter(root.get(DescribableEntity.PROPERTY_NAME),
                                propertyValue.toString(), operator, builder, switched);
                    case "description":
                        return handleDirectStringPropertyFilter(
                                root.get(DescribableEntity.PROPERTY_DESCRIPTION), propertyValue.toString(),
                                operator, builder, switched);
                    case "encodingType":
                        Join<LocationEntity, LocationEncodingEntity> join =
                                root.join(LocationEntity.PROPERTY_LOCATION_ENCODINT);
                        return handleDirectStringPropertyFilter(
                                join.get(LocationEncodingEntity.PROPERTY_ENCODING_TYPE),
                                propertyValue.toString(), operator, builder, switched);
                    default:
                        throw new RuntimeException("Error getting filter for Property: \"" + propertyName
                                + "\". No such property in Entity.");
                }
            } catch (ExpressionVisitException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
