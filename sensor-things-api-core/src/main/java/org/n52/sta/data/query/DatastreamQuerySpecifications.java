/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
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

import java.util.Collection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class DatastreamQuerySpecifications extends EntityQuerySpecifications<DatastreamEntity> {

    public Specification<DatastreamEntity> withObservedProperty(final Long observablePropertyId) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, PhenomenonEntity> join =
                    root.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), observablePropertyId);
        };
    }

    public Specification<DatastreamEntity> withObservedProperty(final String name) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, PhenomenonEntity> join =
                    root.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_NAME), name);
        };
    }
    
    public Specification<DatastreamEntity> withThing(final Long thingId) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, PlatformEntity> join =
                    root.join(DatastreamEntity.PROPERTY_THING, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), thingId);
        };
    }

    public Specification<DatastreamEntity> withThing(final String name) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, PlatformEntity> join =
                    root.join(DatastreamEntity.PROPERTY_THING, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_NAME), name);
        };
    }

    public Specification<DatastreamEntity> withSensor(final Long thingId) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, ProcedureEntity> join =
                    root.join(DatastreamEntity.PROPERTY_SENSOR, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), thingId);
        };
    }

    public Specification<DatastreamEntity> withSensor(final String name) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, ProcedureEntity> join =
                    root.join(DatastreamEntity.PROPERTY_SENSOR, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_NAME), name);
        };
    }

    public Specification<DatastreamEntity> withDataset(final Long datasetId) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, DatasetEntity> join =
                    root.join(DatastreamEntity.PROPERTY_DATASETS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), datasetId);
        };
    }

    public Specification<DatastreamEntity> withDataset(final Collection<Long> datasetIds) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, DatasetEntity> join =
                    root.join(DatastreamEntity.PROPERTY_DATASETS, JoinType.INNER);
            return join.get(DescribableEntity.PROPERTY_ID).in(datasetIds);
        };
    }
    
    public Specification<DatastreamEntity> withObservation(Long observationId) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DataEntity> data = sq.from(DataEntity.class);
            sq.select(data.get(DataEntity.PROPERTY_DATASET)).where(builder.equal(data.get(DescribableEntity.PROPERTY_ID), observationId));
            Join<DatastreamEntity, DatasetEntity> join = root.join(DatastreamEntity.PROPERTY_DATASETS);
            return builder.in(join.get(DatasetEntity.PROPERTY_ID)).value(sq);
        };
    }

    @Override
    public Specification<Long> getIdSubqueryWithFilter(Specification filter) {
        return this.toSubquery(DatastreamEntity.class, DatastreamEntity.PROPERTY_ID, filter);
    }

    @Override
    public Specification<DatastreamEntity> getFilterForProperty(String propertyName,
                                       Object propertyValue,
                                       BinaryOperatorKind operator,
                                       boolean switched)
            throws ExpressionVisitException {
        if (propertyName.equals("Sensor") || propertyName.equals("ObservedProperty") || propertyName.equals("Thing")
                || propertyName.equals("Observations")) {
            return handleRelatedPropertyFilter(propertyName, propertyValue, switched);
        } else if (propertyName.equals("id")) {
            return (root, query, builder) -> {
                try {
                    return handleDirectNumberPropertyFilter(root.<Long> get(DatastreamEntity.PROPERTY_ID),
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
    
    private Specification<DatastreamEntity> handleDirectPropertyFilter(String propertyName, Object propertyValue,
            BinaryOperatorKind operator, boolean switched) {
        return new Specification<DatastreamEntity>() {
            @Override
            public Predicate toPredicate(Root<DatastreamEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                try {
                    switch (propertyName) {
                    case "name":
                        return handleDirectStringPropertyFilter(root.<String> get(DescribableEntity.PROPERTY_NAME),
                                propertyValue.toString(), operator, builder, switched);
                    case "description":
                        return handleDirectStringPropertyFilter(
                                root.<String> get(DescribableEntity.PROPERTY_DESCRIPTION), propertyValue.toString(),
                                operator, builder, switched);
                    case "observationType":
                        Join<DatastreamEntity, FormatEntity> join =
                                root.join(DatastreamEntity.PROPERTY_OBSERVATION_TYPE);
                        return handleDirectStringPropertyFilter(join.<String> get(FormatEntity.FORMAT),
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
    
    
    /**
     * Handles filtering of properties in related Entities.
     * 
     * @param propertyName
     *        Name of property
     * @param propertyValue
     *        Supposed value of Property
     * @param operator
     *        Comparison operator between propertyValue and actual Value
     * @return BooleanExpression evaluating to true if Entity is not filtered out
     */
    private Specification<DatastreamEntity> handleRelatedPropertyFilter(String propertyName,
                                                          Object propertyValue,
                                                          boolean switched)
            throws ExpressionVisitException {
        return (root, query, builder) -> {
            try {
                // TODO: handle switched Parameter
                switch (propertyName) {
                case "Sensor": {
                    final Join<DatastreamEntity, ProcedureEntity> join =
                            root.join(DatastreamEntity.PROPERTY_SENSOR, JoinType.INNER);
                    return builder.equal(join.get(DescribableEntity.PROPERTY_ID), propertyValue);

                }
                case "ObservedProperty": {
                    final Join<DatastreamEntity, PhenomenonEntity> join =
                            root.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
                    return builder.equal(join.get(DescribableEntity.PROPERTY_ID), propertyValue);
                }
                case "Thing": {
                    final Join<DatastreamEntity, PlatformEntity> join =
                            root.join(DatastreamEntity.PROPERTY_THING, JoinType.INNER);
                    return builder.equal(join.get(DescribableEntity.PROPERTY_ID), propertyValue);
                }
                case "Observations": {
                    Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
                    Root<DataEntity> data = sq.from(DataEntity.class);
                    sq.select(data.get(DataEntity.PROPERTY_DATASET))
                            .where(builder.equal(data.get(DescribableEntity.PROPERTY_ID), propertyValue));
                    Join<DatastreamEntity, DatasetEntity> join = root.join(DatastreamEntity.PROPERTY_DATASETS);
                    return builder.in(join.get(DatasetEntity.PROPERTY_ID)).value(sq);
                }
                default:
                    throw new ExpressionVisitException(
                            "Error getting filter for Property: \"" + propertyName + "\". No such related Entity.");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
