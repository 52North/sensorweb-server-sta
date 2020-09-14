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

import org.n52.series.db.beans.AbstractDatasetEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.LicenseEntity;
import org.n52.series.db.beans.sta.ObservationEntity;
import org.n52.series.db.beans.sta.PartyEntity;
import org.n52.series.db.beans.sta.ProjectEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.List;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class DatastreamQuerySpecifications extends EntityQuerySpecifications<AbstractDatasetEntity> {

    @Override
    public Specification<AbstractDatasetEntity> withName(final String name) {
        return (root, query, builder) ->
                builder.and(builder.isNull(root.get(AbstractDatasetEntity.PROPERTY_AGGREGATION)),
                            builder.equal(root.get(DescribableEntity.PROPERTY_NAME), name));
    }

    @Override
    public Specification<AbstractDatasetEntity> withStaIdentifier(final String name) {
        return (root, query, builder) ->
                builder.and(builder.isNull(root.get(AbstractDatasetEntity.PROPERTY_AGGREGATION)),
                            builder.equal(root.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), name));
    }

    @Override
    public Specification<AbstractDatasetEntity> withStaIdentifier(final List<String> identifiers) {
        return (root, query, builder) ->
                builder.and(builder.isNull(root.get(AbstractDatasetEntity.PROPERTY_AGGREGATION)),
                            builder.in(root.get(DescribableEntity.PROPERTY_STA_IDENTIFIER))
                                   .value(identifiers));
    }

    public Specification<AbstractDatasetEntity> withFeatureStaIdentifier(final String staIdentifier) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, FeatureEntity> join =
                    root.join(AbstractDatasetEntity.PROPERTY_FEATURE, JoinType.INNER);
            return builder.equal(join.get(FeatureEntity.PROPERTY_STA_IDENTIFIER), staIdentifier);
        };
    }

    public Specification<AbstractDatasetEntity> withObservedPropertyStaIdentifier(
            final String observablePropertyIdentifier) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, PhenomenonEntity> join =
                    root.join(AbstractDatasetEntity.PROPERTY_PHENOMENON, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), observablePropertyIdentifier);
        };
    }

    public Specification<AbstractDatasetEntity> withObservedPropertyName(final String name) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, PhenomenonEntity> join =
                    root.join(AbstractDatasetEntity.PROPERTY_PHENOMENON, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_NAME), name);
        };
    }

    public Specification<AbstractDatasetEntity> withThingStaIdentifier(final String thingIdentifier) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, PlatformEntity> join =
                    root.join(AbstractDatasetEntity.PROPERTY_PLATFORM, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), thingIdentifier);
        };
    }

    public Specification<AbstractDatasetEntity> withThingName(final String name) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, PlatformEntity> join =
                    root.join(AbstractDatasetEntity.PROPERTY_PLATFORM, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_NAME), name);
        };
    }

    public Specification<AbstractDatasetEntity> withSensorStaIdentifier(final String sensorIdentifier) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, ProcedureEntity> join =
                    root.join(AbstractDatasetEntity.PROCEDURE, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), sensorIdentifier);
        };
    }

    public Specification<AbstractDatasetEntity> withSensorName(final String name) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, ProcedureEntity> join =
                    root.join(AbstractDatasetEntity.PROCEDURE, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_NAME), name);
        };
    }

    public Specification<AbstractDatasetEntity> withObservationStaIdentifier(String observationIdentifier) {
        return (root, query, builder) -> {
            Subquery<AbstractDatasetEntity> sq = query.subquery(AbstractDatasetEntity.class);
            Root<ObservationEntity> data = sq.from(ObservationEntity.class);
            sq.select(data.get(ObservationEntity.PROPERTY_DATASET_ID))
              .where(builder.equal(data.get(ObservationEntity.PROPERTY_STA_IDENTIFIER), observationIdentifier));

            Subquery<AbstractDatasetEntity> subquery = query.subquery(AbstractDatasetEntity.class);
            Root<AbstractDatasetEntity> realDataset = subquery.from(AbstractDatasetEntity.class);
            subquery.select(realDataset.get(AbstractDatasetEntity.PROPERTY_AGGREGATION))
                    .where(builder.equal(realDataset.get(AbstractDatasetEntity.PROPERTY_ID), sq));

            // Either id matches or aggregation id matches
            return builder.or(builder.equal(root.get(AbstractDatasetEntity.PROPERTY_ID), sq),
                              builder.equal(root.get(AbstractDatasetEntity.PROPERTY_ID), subquery));
        };
    }

    public Specification<AbstractDatasetEntity> withPartyStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, PartyEntity> join =
                    root.join(AbstractDatasetEntity.PROPERTY_PARTY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), identifier);
        };
    }

    public Specification<AbstractDatasetEntity> withProjectStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, ProjectEntity> join =
                    root.join(AbstractDatasetEntity.PROPERTY_PROJECT, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), identifier);
        };
    }

    public Specification<AbstractDatasetEntity> withLicenseStaIdentifier(final String identifier) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, LicenseEntity> join =
                    root.join(AbstractDatasetEntity.PROPERTY_LICENSE, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), identifier);
        };
    }

    @Override protected Specification<AbstractDatasetEntity> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (Specification<AbstractDatasetEntity>) (root, query, builder) -> {
            try {
                switch (propertyName) {
                case StaConstants.PROP_ID:
                    return handleDirectStringPropertyFilter(root.get(AbstractDatasetEntity.PROPERTY_STA_IDENTIFIER),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            false);
                case StaConstants.PROP_NAME:
                    return handleDirectStringPropertyFilter(root.get(DescribableEntity.PROPERTY_NAME),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            switched);
                case StaConstants.PROP_DESCRIPTION:
                    return handleDirectStringPropertyFilter(
                            root.get(DescribableEntity.PROPERTY_DESCRIPTION),
                            propertyValue,
                            operator,
                            builder,
                            switched);
                case StaConstants.PROP_OBSERVATION_TYPE:
                    Join<AbstractDatasetEntity, FormatEntity> join =
                            root.join(AbstractDatasetEntity.PROPERTY_OM_OBSERVATION_TYPE);
                    return handleDirectStringPropertyFilter(join.get(FormatEntity.FORMAT),
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

    /**
     * Handles filtering of properties in related Entities.
     *
     * @param propertyName  Name of property
     * @param propertyValue Supposed value of Property
     * @return BooleanExpression evaluating to true if Entity is not filtered out
     */
    @Override protected Specification<AbstractDatasetEntity> handleRelatedPropertyFilter(
            String propertyName,
            Specification<?> propertyValue) {
        return (root, query, builder) -> {
            try {
                switch (propertyName) {
                case SENSOR: {
                    Subquery<AbstractDatasetEntity> sq = query.subquery(AbstractDatasetEntity.class);
                    Root<ProcedureEntity> sensor = sq.from(ProcedureEntity.class);
                    sq.select(sensor.get(DescribableEntity.PROPERTY_ID))
                      .where(((Specification<ProcedureEntity>) propertyValue).toPredicate(sensor,
                                                                                          query,
                                                                                          builder));
                    return builder.in(root.get(AbstractDatasetEntity.PROPERTY_PROCEDURE)).value(sq);
                }
                case OBSERVED_PROPERTY: {
                    Subquery<AbstractDatasetEntity> sq = query.subquery(AbstractDatasetEntity.class);
                    Root<PhenomenonEntity> observedProperty = sq.from(PhenomenonEntity.class);

                    sq.select(observedProperty.get(DescribableEntity.PROPERTY_ID))
                      .where(((Specification<PhenomenonEntity>) propertyValue).toPredicate(observedProperty,
                                                                                           query,
                                                                                           builder));
                    return builder.in(root.get(AbstractDatasetEntity.PROPERTY_PHENOMENON)).value(sq);
                }
                case THING: {
                    Subquery<AbstractDatasetEntity> sq = query.subquery(AbstractDatasetEntity.class);
                    Root<PlatformEntity> thing = sq.from(PlatformEntity.class);
                    sq.select(thing.get(DescribableEntity.PROPERTY_ID))
                      .where(((Specification<PlatformEntity>) propertyValue).toPredicate(thing,
                                                                                         query,
                                                                                         builder));
                    return builder.in(root.get(AbstractDatasetEntity.PROPERTY_PLATFORM)).value(sq);
                }
                case OBSERVATIONS: {
                    Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
                    Root<ObservationEntity> data = sq.from(ObservationEntity.class);
                    sq.select(data.get(ObservationEntity.PROPERTY_DATASET))
                      .where(((Specification<ObservationEntity>) propertyValue).toPredicate(data,
                                                                                            query,
                                                                                            builder));
                    return builder.in(root.get(DatasetEntity.PROPERTY_ID)).value(sq);
                }
                default:
                    throw new STAInvalidFilterExpressionException(COULD_NOT_FIND_RELATED_PROPERTY + propertyName);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public String checkPropertyName(String property) {
        switch (property) {
        case "phenomenonTime":
            return AbstractDatasetEntity.PROPERTY_FIRST_VALUE_AT;
        case "resultTime":
            return AbstractDatasetEntity.PROPERTY_RESULT_TIME_START;
        default:
            return super.checkPropertyName(property);
        }
    }
}
