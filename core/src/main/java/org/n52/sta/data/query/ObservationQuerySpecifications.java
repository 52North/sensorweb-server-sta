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

import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.IdEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.sta.mapped.DatastreamEntity;
import org.n52.series.db.beans.sta.mapped.ObservationEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationQuerySpecifications extends EntityQuerySpecifications<ObservationEntity<?>> {

    public static Specification<ObservationEntity<?>> withFeatureOfInterestStaIdentifier(final String featureIdentifier) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatasetEntity> dataset = sq.from(DatasetEntity.class);
            Subquery<FeatureEntity> subquery = query.subquery(FeatureEntity.class);
            Root<FeatureEntity> feature = subquery.from(FeatureEntity.class);
            subquery.select(feature.get(FeatureEntity.PROPERTY_ID))
                    .where(builder.equal(feature.get(FeatureEntity.PROPERTY_STA_IDENTIFIER), featureIdentifier));
            sq.select(dataset.get(DatasetEntity.PROPERTY_ID))
              .where(builder.equal(dataset.get(DatasetEntity.PROPERTY_FEATURE), subquery));
            return builder.in(root.get(ObservationEntity.PROPERTY_DATASET)).value(sq);
        };
    }

    public static Specification<ObservationEntity<?>> withDatastreamStaIdentifier(final String datastreamIdentifier) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
            Join<DatastreamEntity, DatasetEntity> join = datastream.join(DatastreamEntity.PROPERTY_DATASETS);
            sq.select(join.get(DatasetEntity.PROPERTY_ID)).where(
                    builder.equal(datastream.get(DatastreamEntity.PROPERTY_STA_IDENTIFIER), datastreamIdentifier));
            return builder.in(root.get(ObservationEntity.PROPERTY_DATASET)).value(sq);
        };
    }

    public static Specification<ObservationEntity<?>> withDatasetId(final long datasetId) {
        return (root, query, builder) -> {
            final Join<ObservationEntity, DatasetEntity> join =
                    root.join(ObservationEntity.PROPERTY_DATASET, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), datasetId);
        };
    }

    @Override protected Specification<ObservationEntity<?>> handleRelatedPropertyFilter(
            String propertyName,
            Specification<?> propertyValue) {
        return (root, query, builder) -> {
            try {
                if (DATASTREAM.equals(propertyName)) {
                    Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
                    Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
                    Join<DatastreamEntity, DatasetEntity> join = datastream.join(DatastreamEntity.PROPERTY_DATASETS);
                    sq.select(join).where(
                            ((Specification<DatastreamEntity>) propertyValue).toPredicate(datastream, query, builder));
                    return builder.in(root.get(ObservationEntity.PROPERTY_DATASET)).value(sq);

                } else if (FEATUREOFINTEREST.equals(propertyName)) {
                    Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
                    Root<DatasetEntity> dataset = sq.from(DatasetEntity.class);
                    Subquery<FeatureEntity> subquery = query.subquery(FeatureEntity.class);
                    Root<FeatureEntity> feature = subquery.from(FeatureEntity.class);
                    subquery.select(feature).where(
                            ((Specification<FeatureEntity>) propertyValue).toPredicate(feature, query, builder));
                    sq.select(dataset.get(DatasetEntity.PROPERTY_ID))
                      .where(builder.equal(dataset.get(DatasetEntity.PROPERTY_FEATURE), subquery));
                    return builder.in(root.get(ObservationEntity.PROPERTY_DATASET)).value(sq);
                } else if (ObservationEntity.PROPERTY_PARAMETERS.equals(propertyName)) {
                    Subquery<ObservationEntity> sq = query.subquery(ObservationEntity.class);
                    Root<ParameterEntity> parameters = sq.from(ParameterEntity.class);
                    Join<ParameterEntity, ObservationEntity> join = root.join(ObservationEntity.PROPERTY_PARAMETERS);
                    sq.select(root.get(ObservationEntity.PROPERTY_ID))
                      .where(((Specification<ParameterEntity>) propertyValue).toPredicate(parameters,
                                                                                          query,
                                                                                          builder));
                    return builder.in(join.get(DescribableEntity.PROPERTY_ID)).value(sq);
                } else {
                    throw new STAInvalidFilterExpressionException("Could not find related property: " + propertyName);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected Specification<ObservationEntity<?>> handleDirectPropertyFilter(
            String propertyName,
            Expression<?> propertyValue,
            FilterConstants.ComparisonOperator operator,
            boolean switched) {
        return (root, query, builder) -> {
            try {
                switch (propertyName) {
                case "id":
                    return handleDirectStringPropertyFilter(root.get(ObservationEntity.PROPERTY_STA_IDENTIFIER),
                                                            propertyValue, operator, builder, false);
                case "result":
                    if (propertyValue.getJavaType().isAssignableFrom(Double.class)
                            || propertyValue.getJavaType().isAssignableFrom(Integer.class)) {
                        Predicate countPred = handleDirectNumberPropertyFilter(
                                root.<Double>get(ObservationEntity.PROPERTY_VALUE_COUNT),
                                propertyValue,
                                operator,
                                builder);

                        Predicate quantityPred = handleDirectNumberPropertyFilter(
                                root.<Double>get(ObservationEntity.PROPERTY_VALUE_QUANTITY),
                                propertyValue,
                                operator,
                                builder);
                        // Check for quantity or count as those are numeric
                        // Do not return observations with non-numeric result type
                        return builder.and(
                                builder.or(countPred, root.get(ObservationEntity.PROPERTY_VALUE_COUNT).isNull()),
                                builder.or(quantityPred, root.get(ObservationEntity.PROPERTY_VALUE_QUANTITY).isNull()),
                                root.get(ObservationEntity.PROPERTY_VALUE_CATEGORY).isNull(),
                                root.get(ObservationEntity.PROPERTY_VALUE_TEXT).isNull(),
                                root.get(ObservationEntity.PROPERTY_VALUE_BOOLEAN).isNull()
                        );
                    } else if (propertyValue.getJavaType().isAssignableFrom(String.class)) {
                        Predicate categoryPred = handleDirectStringPropertyFilter(
                                root.get(ObservationEntity.PROPERTY_VALUE_CATEGORY),
                                propertyValue,
                                operator,
                                builder,
                                false);

                        Predicate textPred = handleDirectStringPropertyFilter(
                                root.get(ObservationEntity.PROPERTY_VALUE_TEXT),
                                propertyValue,
                                operator,
                                builder,
                                false);

                        /*
                        Predicate boolPred = handleDirectStringPropertyFilter(
                                root.get(ObservationEntity.PROPERTY_VALUE_BOOLEAN),
                                propertyValue,
                                operator,
                                builder,
                                false);
                        */

                        // Check for category, text, boolean as those represented by String in query
                        // Do not return observations with numeric result type as we are filtering on string
                        return builder.and(
                                builder.or(categoryPred, root.get(ObservationEntity.PROPERTY_VALUE_CATEGORY).isNull()),
                                builder.or(textPred, root.get(ObservationEntity.PROPERTY_VALUE_TEXT).isNull()),
                                // builder.or(boolPred, root.get(ObservationEntity.PROPERTY_VALUE_BOOLEAN).isNull()),
                                root.get(ObservationEntity.PROPERTY_VALUE_COUNT).isNull(),
                                root.get(ObservationEntity.PROPERTY_VALUE_QUANTITY).isNull()
                        );
                    } else {
                        throw new STAInvalidFilterExpressionException("Value type not supported!");
                    }
                case "resultTime":
                    return this.handleDirectDateTimePropertyFilter(
                            root.get(ObservationEntity.PROPERTY_RESULT_TIME), propertyValue, operator, builder);
                case "phenomenonTime":
                    switch (operator) {
                    case PropertyIsLessThan:
                    case PropertyIsLessThanOrEqualTo:
                        return handleDirectDateTimePropertyFilter(
                                root.get(ObservationEntity.PROPERTY_SAMPLING_TIME_END), propertyValue, operator,
                                builder);
                    case PropertyIsGreaterThan:
                    case PropertyIsGreaterThanOrEqualTo:
                        return handleDirectDateTimePropertyFilter(
                                root.get(ObservationEntity.PROPERTY_SAMPLING_TIME_START), propertyValue, operator,
                                builder);
                    case PropertyIsEqualTo:
                        Predicate eqStart = handleDirectDateTimePropertyFilter(
                                root.get(ObservationEntity.PROPERTY_SAMPLING_TIME_START), propertyValue, operator,
                                builder);
                        Predicate eqEnd = handleDirectDateTimePropertyFilter(
                                root.get(ObservationEntity.PROPERTY_SAMPLING_TIME_END), propertyValue, operator,
                                builder);
                        return builder.and(eqStart, eqEnd);
                    case PropertyIsNotEqualTo:
                        Predicate neStart = handleDirectDateTimePropertyFilter(
                                root.get(ObservationEntity.PROPERTY_SAMPLING_TIME_START), propertyValue, operator,
                                builder);
                        Predicate neEnd = handleDirectDateTimePropertyFilter(
                                root.get(ObservationEntity.PROPERTY_SAMPLING_TIME_END), propertyValue, operator,
                                builder);
                        return builder.or(neStart, neEnd);
                    default:
                        throw new STAInvalidFilterExpressionException("Unknown operator: " + operator.toString());
                    }
                default:
                    throw new STAInvalidFilterExpressionException("Currently not implemented!");
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
