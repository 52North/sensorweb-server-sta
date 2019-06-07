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

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.joda.time.DateTime;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
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

    @Override
    public Specification<Long> getIdSubqueryWithFilter(Specification filter) {
        return this.toSubquery(HistoricalLocationEntity.class, HistoricalLocationEntity.PROPERTY_IDENTIFIER, filter);
    }

    @Override
    public Specification<HistoricalLocationEntity> getFilterForProperty(String propertyName,
                                       Object propertyValue,
                                       BinaryOperatorKind operator,
                                       boolean switched)
            throws ExpressionVisitException {
        if (propertyName.equals("Thing") || propertyName.equals("Locations")) {
            return handleRelatedPropertyFilter(propertyName, (Specification<Long>) propertyValue, switched);
        } else if (propertyName.equals("id")) {
            return (root, query, builder) -> {
                try {
                    return handleDirectStringPropertyFilter(root.get(HistoricalLocationEntity.PROPERTY_IDENTIFIER),
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

    private Specification<HistoricalLocationEntity> handleRelatedPropertyFilter(String propertyName,
            Specification<Long> propertyValue, boolean switched) throws ExpressionVisitException {
        return (root, query, builder) -> {
            if (propertyName.equals("Thing")) {
                final Join<HistoricalLocationEntity, PlatformEntity> join =
                        root.join(HistoricalLocationEntity.PROPERTY_THING, JoinType.INNER);
                return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue);
            } else {
                final Join<HistoricalLocationEntity, LocationEntity> join =
                        root.join(HistoricalLocationEntity.PROPERTY_LOCATIONS, JoinType.INNER);
                return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue);
            }
        };
    }

    private Specification<HistoricalLocationEntity> handleDirectPropertyFilter(String propertyName, Object propertyValue,
            BinaryOperatorKind operator, boolean switched) {
        return new Specification<HistoricalLocationEntity>() {
            @Override
            public Predicate toPredicate(Root<HistoricalLocationEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                try {
                    switch (propertyName) {
                    case "time":
                        return handleDirectDateTimePropertyFilter(
                                root.<Date> get(HistoricalLocationEntity.PROPERTY_TIME), new DateTime(propertyValue).toDate(), operator,
                                builder);
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
