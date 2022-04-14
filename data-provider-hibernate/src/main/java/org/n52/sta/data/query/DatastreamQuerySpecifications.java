/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
import org.n52.series.db.beans.CategoryEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.dataset.DatasetParameterEntity;
import org.n52.series.db.beans.sta.plus.PartyEntity;
import org.n52.series.db.beans.sta.plus.ProjectEntity;
import org.n52.series.db.beans.sta.plus.StaPlusAbstractDatasetEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.List;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class DatastreamQuerySpecifications<T> extends EntityQuerySpecifications<T> {

    @Override
    public Specification<T> withName(final String name) {
        return (root, query, builder) ->
            builder.and(builder.isNull(root.get(AbstractDatasetEntity.PROPERTY_AGGREGATION)),
                        builder.equal(root.get(DescribableEntity.PROPERTY_NAME), name));
    }

    public Specification<T> withStaIdentifier(final String name) {
        return (root, query, builder) ->
            builder.and(builder.isNull(root.get(AbstractDatasetEntity.PROPERTY_AGGREGATION)),
                        builder.equal(root.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), name));
    }

    public Specification<T> withStaIdentifier(final List<String> identifiers) {
        return (root, query, builder) ->
            builder.and(builder.isNull(root.get(AbstractDatasetEntity.PROPERTY_AGGREGATION)),
                        builder.in(root.get(DescribableEntity.PROPERTY_STA_IDENTIFIER))
                            .value(identifiers));
    }

    /**
     * Handles filtering of properties in related Entities.
     *
     * @param propertyName  Name of property
     * @param propertyValue Supposed value of Property
     * @return BooleanExpression evaluating to true if Entity is not filtered out
     */
    @Override protected Specification<T> handleRelatedPropertyFilter(
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
                        Root<DataEntity> data = sq.from(DataEntity.class);
                        sq.select(data.get(DataEntity.PROPERTY_DATASET_ID))
                            .where(((Specification<DataEntity>) propertyValue).toPredicate(data,
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

    @Override protected Specification<T> handleDirectPropertyFilter(
        String propertyName,
        Expression<?> propertyValue,
        FilterConstants.ComparisonOperator operator,
        boolean switched) {
        return (root, query, builder) -> {
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
                        // We are filtering on variable keys on properties
                        if (propertyName.startsWith(StaConstants.PROP_PROPERTIES)) {
                            return handleProperties(root,
                                                    query,
                                                    builder,
                                                    propertyName,
                                                    propertyValue,
                                                    operator,
                                                    switched,
                                                    DatasetParameterEntity.PROP_DATASET_ID,
                                                    ParameterFactory.EntityType.DATASET);
                        } else {
                            throw new RuntimeException(String.format(ERROR_GETTING_FILTER_NO_PROP, propertyName));
                        }
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected Predicate handleProperties(Root<?> root,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder builder,
                                         String propertyName,
                                         Expression<?> propertyValue,
                                         FilterConstants.ComparisonOperator operator,
                                         boolean switched,
                                         String referenceName,
                                         ParameterFactory.EntityType entityType)
        throws STAInvalidFilterExpressionException {
        String key = propertyName.substring(11);
        final String categoryPrefix = "category";

        // Handle filter on Category->id
        if (propertyValue.getJavaType().isAssignableFrom(Double.class)) {
            if (key.startsWith(categoryPrefix)) {
                final Join<AbstractDatasetEntity, CategoryEntity> join =
                    root.join(AbstractDatasetEntity.PROPERTY_CATEGORY, JoinType.INNER);
                if (key.substring(categoryPrefix.length()).equals("Id")) {
                    return handleDirectNumberPropertyFilter(join.<Double>get(CategoryEntity.PROPERTY_ID),
                                                            propertyValue,
                                                            operator,
                                                            builder);
                } else {
                    throw new STAInvalidFilterExpressionException(
                        String.format(ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE, key, "Long"));
                }
            }
        }

        // Handle filter on Category->name and Category->description
        if (propertyValue.getJavaType().isAssignableFrom(String.class)) {
            if (key.startsWith(categoryPrefix)) {
                final Join<AbstractDatasetEntity, CategoryEntity> join =
                    root.join(AbstractDatasetEntity.PROPERTY_CATEGORY, JoinType.INNER);
                String property = null;
                switch (key.substring(categoryPrefix.length())) {
                    case "Name":
                        property = CategoryEntity.NAME;
                        break;
                    case "Description":
                        property = CategoryEntity.DESCRIPTION;
                        break;
                    default:
                        throw new STAInvalidFilterExpressionException(
                            String.format(ERROR_GETTING_FILTER_NO_PROP_OR_WRONG_TYPE, key, "String"));
                }
                return handleDirectStringPropertyFilter(join.get(property),
                                                        propertyValue,
                                                        operator,
                                                        builder,
                                                        switched);

            }
        }

        return super.handleProperties(root,
                                      query,
                                      builder,
                                      propertyName,
                                      propertyValue,
                                      operator,
                                      switched,
                                      referenceName,
                                      entityType);

    }

    public String checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_PHENOMENON_TIME:
                return AbstractDatasetEntity.PROPERTY_FIRST_VALUE_AT;
            case StaConstants.PROP_RESULT_TIME:
                return AbstractDatasetEntity.PROPERTY_RESULT_TIME_START;
            default:
                return super.checkPropertyName(property);
        }
    }

    public Specification<AbstractDatasetEntity> withFeatureStaIdentifier(final String staIdentifier) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, FeatureEntity> join =
                root.join(AbstractDatasetEntity.PROPERTY_FEATURE, JoinType.INNER);
            return builder.equal(join.get(FeatureEntity.PROPERTY_STA_IDENTIFIER), staIdentifier);
        };
    }

    public <S> Specification<S> withObservedPropertyStaIdentifier(
        final String observablePropertyIdentifier) {
        return (root, query, builder) -> {
            final Join<AbstractDatasetEntity, PhenomenonEntity> join =
                root.join(AbstractDatasetEntity.PROPERTY_PHENOMENON, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_STA_IDENTIFIER), observablePropertyIdentifier);
        };
    }

    public Specification<AbstractDatasetEntity> withPartyStaIdentifier(
        final String partyIdentifier) {
        return (root, query, builder) -> builder.equal(root.get(StaPlusAbstractDatasetEntity.PROPERTY_PARTY)
                                                           .get(PartyEntity.STA_IDENTIFIER), partyIdentifier);
    }

    public Specification<AbstractDatasetEntity> withProjectStaIdentifier(
        final String projectIdentifier) {
        return (root, query, builder) -> builder.equal(root.get(StaPlusAbstractDatasetEntity.PROPERTY_PROJECT)
                                                           .get(ProjectEntity.STA_IDENTIFIER), projectIdentifier);
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

    public <S> Specification<S> withSensorStaIdentifier(final String sensorIdentifier) {
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
            Root<DataEntity> data = sq.from(DataEntity.class);
            sq.select(data.get(DataEntity.PROPERTY_DATASET_ID))
                .where(builder.equal(data.get(DataEntity.PROPERTY_STA_IDENTIFIER), observationIdentifier));

            Subquery<AbstractDatasetEntity> subquery = query.subquery(AbstractDatasetEntity.class);
            Root<AbstractDatasetEntity> realDataset = subquery.from(AbstractDatasetEntity.class);
            subquery.select(realDataset.get(AbstractDatasetEntity.PROPERTY_AGGREGATION))
                .where(builder.equal(realDataset.get(AbstractDatasetEntity.PROPERTY_ID), sq));

            // Either id matches or aggregation id matches
            return builder.or(builder.equal(root.get(AbstractDatasetEntity.PROPERTY_ID), sq),
                              builder.equal(root.get(AbstractDatasetEntity.PROPERTY_ID), subquery));
        };
    }
}
