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
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Collection;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class DatastreamQuerySpecifications extends EntityQuerySpecifications<DatastreamEntity> {

    public Specification<DatastreamEntity> withObservedPropertyIdentifier(final String observablePropertyIdentifier) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, PhenomenonEntity> join =
                    root.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
            return builder.equal(join.get("staIdentifier"), observablePropertyIdentifier);
        };
    }

    public Specification<DatastreamEntity> withObservedPropertyName(final String name) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, PhenomenonEntity> join =
                    root.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_NAME), name);
        };
    }

    public Specification<DatastreamEntity> withThingIdentifier(final String thingIdentifier) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, PlatformEntity> join =
                    root.join(DatastreamEntity.PROPERTY_THING, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), thingIdentifier);
        };
    }

    public Specification<DatastreamEntity> withThingName(final String name) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, PlatformEntity> join =
                    root.join(DatastreamEntity.PROPERTY_THING, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_NAME), name);
        };
    }

    public Specification<DatastreamEntity> withSensorIdentifier(final String sensorIdentifier) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, ProcedureEntity> join =
                    root.join(DatastreamEntity.PROPERTY_SENSOR, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), sensorIdentifier);
        };
    }

    public Specification<DatastreamEntity> withSensorName(final String name) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, ProcedureEntity> join =
                    root.join(DatastreamEntity.PROPERTY_SENSOR, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_NAME), name);
        };
    }

    public Specification<DatastreamEntity> withDatasetIdentifier(final String datasetId) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, DatasetEntity> join =
                    root.join(DatastreamEntity.PROPERTY_DATASETS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), datasetId);
        };
    }

    public Specification<DatastreamEntity> withDatasetIdentifiers(final Collection<String> datasetIds) {
        return (root, query, builder) -> {
            final Join<DatastreamEntity, DatasetEntity> join =
                    root.join(DatastreamEntity.PROPERTY_DATASETS, JoinType.INNER);
            return join.get(DescribableEntity.PROPERTY_IDENTIFIER).in(datasetIds);
        };
    }

    public Specification<DatastreamEntity> withObservationIdentifier(String observationIdentifier) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DataEntity> data = sq.from(DataEntity.class);
            sq.select(data.get(DataEntity.PROPERTY_DATASET))
                    .where(builder.equal(data.get(DescribableEntity.PROPERTY_IDENTIFIER), observationIdentifier));
            Join<DatastreamEntity, DatasetEntity> join = root.join(DatastreamEntity.PROPERTY_DATASETS);
            return builder.in(join.get(DatasetEntity.PROPERTY_ID)).value(sq);
        };
    }

    @Override
    public Specification<String> getIdSubqueryWithFilter(Specification filter) {
        return this.toSubquery(DatastreamEntity.class, DatastreamEntity.PROPERTY_IDENTIFIER, filter);
    }

    @Override
    public Specification<DatastreamEntity> getFilterForProperty(String propertyName,
                                                                Object propertyValue,
                                                                BinaryOperatorKind operator,
                                                                boolean switched)
            throws ExpressionVisitException {
        if (propertyName.equals(SENSOR) || propertyName.equals(OBSERVED_PROPERTY) || propertyName.equals(THING)
                || propertyName.equals(OBSERVATIONS)) {
            return handleRelatedPropertyFilter(propertyName, propertyValue, switched);
        } else if (propertyName.equals("id")) {
            return (root, query, builder) -> {
                try {
                    return handleDirectStringPropertyFilter(root.get(DatastreamEntity.PROPERTY_IDENTIFIER),
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

    private Specification<DatastreamEntity> handleDirectPropertyFilter(String propertyName, Object propertyValue,
                                                                       BinaryOperatorKind operator, boolean switched) {
        return (Specification<DatastreamEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case "name":
                        return handleDirectStringPropertyFilter(root.get(DescribableEntity.PROPERTY_NAME),
                                propertyValue.toString(), operator, builder, switched);
                    case "description":
                        return handleDirectStringPropertyFilter(
                                root.get(DescribableEntity.PROPERTY_DESCRIPTION), propertyValue.toString(),
                                operator, builder, switched);
                    case "observationType":
                        Join<DatastreamEntity, FormatEntity> join =
                                root.join(DatastreamEntity.PROPERTY_OBSERVATION_TYPE);
                        return handleDirectStringPropertyFilter(join.get(FormatEntity.FORMAT),
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


    /**
     * Handles filtering of properties in related Entities.
     *
     * @param propertyName  Name of property
     * @param propertyValue Supposed value of Property
     * @return BooleanExpression evaluating to true if Entity is not filtered out
     */
    private Specification<DatastreamEntity> handleRelatedPropertyFilter(String propertyName,
                                                                        Object propertyValue,
                                                                        boolean switched) {
        return (root, query, builder) -> {
            try {
                // TODO: handle switched Parameter
                switch (propertyName) {
                    case SENSOR: {
                        final Join<DatastreamEntity, ProcedureEntity> join =
                                root.join(DatastreamEntity.PROPERTY_SENSOR, JoinType.INNER);
                        return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue);

                    }
                    case OBSERVED_PROPERTY: {
                        final Join<DatastreamEntity, PhenomenonEntity> join =
                                root.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
                        return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue);
                    }
                    case THING: {
                        final Join<DatastreamEntity, PlatformEntity> join =
                                root.join(DatastreamEntity.PROPERTY_THING, JoinType.INNER);
                        return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue);
                    }
                    case OBSERVATIONS: {
                        Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
                        Root<DataEntity> data = sq.from(DataEntity.class);
                        sq.select(data.get(DataEntity.PROPERTY_DATASET))
                                .where(builder.equal(data.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue));
                        Join<DatastreamEntity, DatasetEntity> join = root.join(DatastreamEntity.PROPERTY_DATASETS);
                        return builder.in(join.get(DatasetEntity.PROPERTY_IDENTIFIER)).value(sq);
                    }
                    default:
                        throw new ExpressionVisitException(
                                "Error getting filter for Property \"" + propertyName + "\". No such related Entity.");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
