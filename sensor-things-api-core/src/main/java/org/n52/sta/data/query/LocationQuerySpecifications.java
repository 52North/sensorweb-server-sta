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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEncodingEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class LocationQuerySpecifications extends EntityQuerySpecifications<LocationEntity> {

    public Specification<LocationEntity> withRelatedHistoricalLocation(Long historicalId) {
        return (root, query, builder) -> {
            Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
            Root<HistoricalLocationEntity> dataset = sq.from(HistoricalLocationEntity.class);
            Join<HistoricalLocationEntity, LocationEntity> joinFeature = dataset.join(HistoricalLocationEntity.PROPERTY_LOCATIONS);
            sq.select(joinFeature).where(builder.equal(dataset.get(DescribableEntity.PROPERTY_ID), historicalId));
            return builder.in(root).value(sq);
        };
    }

    public Specification<LocationEntity> withRelatedThing(Long thingId) {
        return (root, query, builder) -> {
            Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
            Root<PlatformEntity> dataset = sq.from(PlatformEntity.class);
            Join<PlatformEntity, LocationEntity> joinFeature = dataset.join(PlatformEntity.PROPERTY_LOCATIONS);
            sq.select(joinFeature).where(builder.equal(dataset.get(DescribableEntity.PROPERTY_ID), thingId));
            return builder.in(root).value(sq);
        };
    }

    @Override
    public Specification<Long> getIdSubqueryWithFilter(Specification filter) {
        return this.toSubquery(LocationEntity.class, LocationEntity.PROPERTY_ID, filter);
    }

    @Override
    public Specification<LocationEntity> getFilterForProperty(String propertyName,
                                       Object propertyValue,
                                       BinaryOperatorKind operator,
                                       boolean switched)
            throws ExpressionVisitException {
        if (propertyName.equals("Things") || propertyName.equals("HistoricalLocations")) {
            return handleRelatedPropertyFilter(propertyName, (Specification<Long>) propertyValue);
        } else if (propertyName.equals("id")) {
            return (root, query, builder) -> {
                try {
                    return handleDirectNumberPropertyFilter(root.<Long> get(LocationEntity.PROPERTY_ID),
                            Long.getLong(propertyValue.toString()), operator, builder);
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
            Specification<Long> propertyValue) throws ExpressionVisitException {
        return (root, query, builder) -> {
            if (propertyName.equals("Things")) {
                Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
                Root<PlatformEntity> dataset = sq.from(PlatformEntity.class);
                Join<PlatformEntity, LocationEntity> joinFeature = dataset.join(PlatformEntity.PROPERTY_LOCATIONS);
                sq.select(joinFeature).where(builder.equal(dataset.get(DescribableEntity.PROPERTY_ID), propertyValue));
                return builder.in(root).value(sq);
            } else {
                Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
                Root<HistoricalLocationEntity> dataset = sq.from(HistoricalLocationEntity.class);
                Join<HistoricalLocationEntity, LocationEntity> joinFeature =
                        dataset.join(HistoricalLocationEntity.PROPERTY_LOCATIONS);
                sq.select(joinFeature).where(builder.equal(dataset.get(DescribableEntity.PROPERTY_ID), propertyValue));
                return builder.in(root).value(sq);
            }
        };
    }

    private Specification<LocationEntity> handleDirectPropertyFilter(String propertyName, Object propertyValue,
            BinaryOperatorKind operator, boolean switched) {
        return new Specification<LocationEntity>() {
            @Override
            public Predicate toPredicate(Root<LocationEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                try {
                    switch (propertyName) {
                    case "name":
                        return handleDirectStringPropertyFilter(root.<String> get(DescribableEntity.PROPERTY_NAME),
                                propertyValue.toString(), operator, builder, switched);
                    case "description":
                        return handleDirectStringPropertyFilter(
                                root.<String> get(DescribableEntity.PROPERTY_DESCRIPTION), propertyValue.toString(),
                                operator, builder, switched);
                    case "encodingType":
                        Join<LocationEntity, LocationEncodingEntity> join =
                                root.join(LocationEntity.PROPERTY_LOCATION_ENCODINT);
                        return handleDirectStringPropertyFilter(
                                join.<String> get(LocationEncodingEntity.PROPERTY_ENCODING_TYPE),
                                propertyValue.toString(), operator, builder, switched);
                    default:
                        throw new RuntimeException("Error getting filter for Property: \"" + propertyName
                                + "\". No such property in Entity.");
                    }
                } catch (ExpressionVisitException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
