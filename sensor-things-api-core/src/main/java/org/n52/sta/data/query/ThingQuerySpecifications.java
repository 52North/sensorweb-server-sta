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
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class ThingQuerySpecifications extends EntityQuerySpecifications<PlatformEntity> {

    public Specification<PlatformEntity> withRelatedLocation(Long locationId) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, LocationEntity> join =
                    root.join(PlatformEntity.PROPERTY_LOCATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), locationId);
        };
    }

    public Specification<PlatformEntity>  withRelatedHistoricalLocation(Long historicalId) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, HistoricalLocationEntity> join =
                    root.join(PlatformEntity.PROPERTY_HISTORICAL_LOCATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), historicalId);
        };
    }

    public Specification<PlatformEntity> withRelatedDatastream(Long datastreamId) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, DatastreamEntity> join =
                    root.join(PlatformEntity.PROPERTY_DATASTREAMS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), datastreamId);
        };
    }

    @Override
    public Specification<Long> getIdSubqueryWithFilter(Specification filter) {
        return this.toSubquery(PlatformEntity.class, PlatformEntity.PROPERTY_ID, filter);
    }

    @Override
    public Specification<PlatformEntity> getFilterForProperty(String propertyName,
                                       Object propertyValue,
                                       BinaryOperatorKind operator,
                                       boolean switched)
            throws ExpressionVisitException {
        if (propertyName.equals("Datastreams") || propertyName.equals("Locations")
                || propertyName.equals("HistoricalLocations")) {
            return handleRelatedPropertyFilter(propertyName, (Subquery<Long>) propertyValue);
        } else if (propertyName.equals("id")) {
            return (root, query, builder) -> {
                try {
                    return handleDirectNumberPropertyFilter(root.<Long> get(PlatformEntity.PROPERTY_ID),
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

    private Specification<PlatformEntity> handleRelatedPropertyFilter(String propertyName,
            Subquery<Long> propertyValue) throws ExpressionVisitException {
        return (root, query, builder) -> {
            try {
                switch (propertyName) {
                case "Datastreams": {
                    return builder.in(root.get(PlatformEntity.PROPERTY_DATASTREAMS)).value(propertyValue);
                    // return
                    // qPlatform.datastreamEntities.any().id.eqAny(propertyValue);
                }
                case "Locations": {
                    return builder.in(root.get(PlatformEntity.PROPERTY_LOCATIONS)).value(propertyValue);
                    // return
                    // qPlatform.locationEntities.any().id.eqAny(propertyValue);
                }
                case "HistoricalLocations": {
                    return builder.in(root.get(PlatformEntity.PROPERTY_HISTORICAL_LOCATIONS)).value(propertyValue);
                    // return
                    // qPlatform.historicalLocationEntities.any().id.eqAny(propertyValue);
                }
                default:
                    throw new ExpressionVisitException(
                            "Filtering by Related Properties with cardinality >1 is currently not supported!");
                }
            } catch (ExpressionVisitException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Specification<PlatformEntity> handleDirectPropertyFilter(String propertyName, Object propertyValue,
            BinaryOperatorKind operator, boolean switched) {
        return new Specification<PlatformEntity>() {
            @Override
            public Predicate toPredicate(Root<PlatformEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                try {
                    switch (propertyName) {
                    case "name":
                        return handleDirectStringPropertyFilter(root.<String> get(DescribableEntity.PROPERTY_NAME),
                                propertyValue.toString(), operator, builder, switched);
                    case "description":
                        return handleDirectStringPropertyFilter(
                                root.<String> get(DescribableEntity.PROPERTY_DESCRIPTION), propertyValue.toString(), operator,
                                builder, switched);
                    case "properties":
                        // TODO
                        // qPlatform.parameters.any().name.eq("properties")
                        return handleDirectStringPropertyFilter(root.<String> get(PlatformEntity.PROPERTY_PROPERTIES),
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
