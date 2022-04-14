/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterFactory;
import org.n52.series.db.beans.parameter.observation.ObservationParameterEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.exception.STAInvalidFilterExpressionException;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationQuerySpecifications extends EntityQuerySpecifications<DataEntity<?>> {

    public static Specification<DataEntity<?>> withFeatureOfInterestStaIdentifier(
        final String featureIdentifier) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatasetEntity> dataset = sq.from(DatasetEntity.class);
            Subquery<FeatureEntity> subquery = query.subquery(FeatureEntity.class);
            Root<FeatureEntity> feature = subquery.from(FeatureEntity.class);
            subquery.select(feature.get(FeatureEntity.PROPERTY_ID))
                .where(builder.equal(feature.get(FeatureEntity.PROPERTY_STA_IDENTIFIER), featureIdentifier));
            sq.select(dataset.get(DatasetEntity.PROPERTY_ID))
                .where(builder.equal(dataset.get(DatasetEntity.PROPERTY_FEATURE), subquery));
            return builder.in(root.get(DataEntity.PROPERTY_DATASET)).value(sq);
        };
    }

    public static Specification<DataEntity<?>> withDatastreamStaIdentifier(
        final String datastreamStaIdentifier) {
        return (root, query, builder) -> {
            Join<DataEntity<?>, DatasetEntity> join = root.join(DataEntity.PROPERTY_DATASET);
            Path<DatasetEntity> pathSta = join.get(DatasetEntity.PROPERTY_STA_IDENTIFIER);
            Path<DatasetEntity> pathAggregation = join.get(DatasetEntity.PROPERTY_AGGREGATION);

            Subquery<AbstractDatasetEntity> subquery = query.subquery(AbstractDatasetEntity.class);
            Root<AbstractDatasetEntity> realDataset = subquery.from(AbstractDatasetEntity.class);
            subquery.select(realDataset.get(AbstractDatasetEntity.PROPERTY_ID)).where(builder
                    .equal(realDataset.get(AbstractDatasetEntity.PROPERTY_STA_IDENTIFIER), datastreamStaIdentifier));

            return builder.or(builder.equal(pathAggregation, subquery),
                              builder.equal(pathSta, datastreamStaIdentifier));
        };
    }

    public static Specification<DataEntity<?>> withDatasetId(final long datasetId) {
        return (root, query, builder) -> builder.equal(root.get(DataEntity.PROPERTY_DATASET_ID), datasetId);
    }

    public static Specification<DataEntity<?>> withParent(final long parentId) {
        return (root, query, builder) -> builder.equal(root.get(DataEntity.PROPERTY_PARENT), parentId);
    }

    @Override protected Specification<DataEntity<?>> handleRelatedPropertyFilter(
        String propertyName,
        Specification<?> propertyValue) {
        return (root, query, builder) -> {
            try {
                if (StaConstants.DATASTREAM.equals(propertyName)) {
                    Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
                    Root<AbstractDatasetEntity> datastream = sq.from(AbstractDatasetEntity.class);
                    sq.select(datastream.get(AbstractDatasetEntity.PROPERTY_ID)).where(
                        ((Specification<AbstractDatasetEntity>) propertyValue).toPredicate(datastream,
                                                                                           query,
                                                                                           builder));
                    return builder.in(root.get(DataEntity.PROPERTY_DATASET)).value(sq);

                } else if (StaConstants.FEATURE_OF_INTEREST.equals(propertyName)) {
                    Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
                    Root<DatasetEntity> dataset = sq.from(DatasetEntity.class);
                    Subquery<FeatureEntity> subquery = query.subquery(FeatureEntity.class);
                    Root<FeatureEntity> feature = subquery.from(FeatureEntity.class);
                    subquery.select(feature).where(
                        ((Specification<FeatureEntity>) propertyValue).toPredicate(feature, query, builder));
                    sq.select(dataset.get(DatasetEntity.PROPERTY_ID))
                        .where(builder.in(dataset.get(DatasetEntity.PROPERTY_FEATURE)).value(subquery));
                    return builder.in(root.get(DataEntity.PROPERTY_DATASET)).value(sq);
                } else if (StaConstants.PROP_PARAMETERS.equals(propertyName)) {
                    Subquery<DataEntity> sq = query.subquery(DataEntity.class);
                    Root<ParameterEntity> parameters = sq.from(ParameterEntity.class);
                    Join<ParameterEntity, DataEntity> join = root.join(DataEntity.PROPERTY_PARAMETERS);
                    sq.select(root.get(DataEntity.PROPERTY_ID))
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
    protected Specification<DataEntity<?>> handleDirectPropertyFilter(
        String propertyName,
        Expression<?> propertyValue,
        FilterConstants.ComparisonOperator operator,
        boolean switched) {
        return (root, query, builder) -> {
            try {
                switch (propertyName) {
                    case StaConstants.PROP_ID:
                        return handleDirectStringPropertyFilter(root.get(DataEntity.PROPERTY_STA_IDENTIFIER),
                                                                propertyValue, operator, builder, false);
                    case StaConstants.PROP_RESULT:
                        if (propertyValue.getJavaType().isAssignableFrom(Double.class)
                            || propertyValue.getJavaType().isAssignableFrom(Integer.class)) {
                            Predicate countPred = handleDirectNumberPropertyFilter(
                                root.<Double>get(DataEntity.PROPERTY_VALUE_COUNT),
                                propertyValue,
                                operator,
                                builder);

                            Predicate quantityPred = handleDirectNumberPropertyFilter(
                                root.<Double>get(DataEntity.PROPERTY_VALUE_QUANTITY),
                                propertyValue,
                                operator,
                                builder);
                            // Check for quantity or count as those are numeric
                            // Do not return observations with non-numeric result type
                            return builder.and(
                                builder.or(countPred, root.get(DataEntity.PROPERTY_VALUE_COUNT).isNull()),
                                builder.or(quantityPred,
                                           root.get(DataEntity.PROPERTY_VALUE_QUANTITY).isNull()),
                                root.get(DataEntity.PROPERTY_VALUE_CATEGORY).isNull(),
                                root.get(DataEntity.PROPERTY_VALUE_TEXT).isNull(),
                                root.get(DataEntity.PROPERTY_VALUE_BOOLEAN).isNull()
                            );
                        } else if (propertyValue.getJavaType().isAssignableFrom(String.class)) {
                            Predicate categoryPred = handleDirectStringPropertyFilter(
                                root.get(DataEntity.PROPERTY_VALUE_CATEGORY),
                                propertyValue,
                                operator,
                                builder,
                                false);

                            Predicate textPred = handleDirectStringPropertyFilter(
                                root.get(DataEntity.PROPERTY_VALUE_TEXT),
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
                                builder.or(categoryPred,
                                           root.get(DataEntity.PROPERTY_VALUE_CATEGORY).isNull()),
                                builder.or(textPred, root.get(DataEntity.PROPERTY_VALUE_TEXT).isNull()),
                                // builder.or(boolPred, root.get(ObservationEntity.PROPERTY_VALUE_BOOLEAN).isNull
                                // ()),
                                root.get(DataEntity.PROPERTY_VALUE_COUNT).isNull(),
                                root.get(DataEntity.PROPERTY_VALUE_QUANTITY).isNull()
                            );
                        } else {
                            throw new STAInvalidFilterExpressionException("Value type not supported!");
                        }
                    case StaConstants.PROP_RESULT_TIME:
                        return this.handleDirectDateTimePropertyFilter(
                            root.get(DataEntity.PROPERTY_RESULT_TIME), propertyValue, operator, builder);
                    case StaConstants.PROP_PHENOMENON_TIME:
                        switch (operator) {
                            case PropertyIsLessThan:
                            case PropertyIsLessThanOrEqualTo:
                                return handleDirectDateTimePropertyFilter(
                                    root.get(DataEntity.PROPERTY_SAMPLING_TIME_END), propertyValue, operator,
                                    builder);
                            case PropertyIsGreaterThan:
                            case PropertyIsGreaterThanOrEqualTo:
                                return handleDirectDateTimePropertyFilter(
                                    root.get(DataEntity.PROPERTY_SAMPLING_TIME_START),
                                    propertyValue,
                                    operator,
                                    builder);
                            case PropertyIsEqualTo:
                                Predicate eqStart = handleDirectDateTimePropertyFilter(
                                    root.get(DataEntity.PROPERTY_SAMPLING_TIME_START),
                                    propertyValue,
                                    operator,
                                    builder);
                                Predicate eqEnd = handleDirectDateTimePropertyFilter(
                                    root.get(DataEntity.PROPERTY_SAMPLING_TIME_END), propertyValue, operator,
                                    builder);
                                return builder.and(eqStart, eqEnd);
                            case PropertyIsNotEqualTo:
                                Predicate neStart = handleDirectDateTimePropertyFilter(
                                    root.get(DataEntity.PROPERTY_SAMPLING_TIME_START),
                                    propertyValue,
                                    operator,
                                    builder);
                                Predicate neEnd = handleDirectDateTimePropertyFilter(
                                    root.get(DataEntity.PROPERTY_SAMPLING_TIME_END), propertyValue, operator,
                                    builder);
                                return builder.or(neStart, neEnd);
                            default:
                                throw new STAInvalidFilterExpressionException(
                                    "Unknown operator: " + operator.toString());
                        }
                    default:
                        // We are filtering on variable keys on parameters
                        if (propertyName.startsWith(StaConstants.PROP_PARAMETERS)) {
                            return handleProperties(root,
                                                    query,
                                                    builder,
                                                    propertyName,
                                                    propertyValue,
                                                    operator,
                                                    switched,
                                                    ObservationParameterEntity.PROP_OBSERVATION_ID,
                                                    ParameterFactory.EntityType.OBSERVATION);
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
    public String checkPropertyName(String property) {
        switch (property) {
            case StaConstants.PROP_PHENOMENON_TIME:
                // TODO: proper ISO8601 comparison
                return DataEntity.PROPERTY_SAMPLING_TIME_END;
            // This is handled separately as result is split up over multiple columns
            //case "result":
            //    return "valueBoolean";
            default:
                return super.checkPropertyName(property);
        }
    }
}
