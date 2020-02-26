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

import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
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
public class ObservationQuerySpecifications extends EntityQuerySpecifications<DataEntity<?>> {

    public Specification<DataEntity<?>> withFeatureOfInterestIdentifier(final String featureIdentifier) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatasetEntity> dataset = sq.from(DatasetEntity.class);
            Subquery<FeatureEntity> subquery = query.subquery(FeatureEntity.class);
            Root<FeatureEntity> feature = subquery.from(FeatureEntity.class);
            subquery.select(feature.get(FeatureEntity.PROPERTY_ID))
                    .where(builder.equal(feature.get(FeatureEntity.IDENTIFIER), featureIdentifier));
            sq.select(dataset.get(DatasetEntity.PROPERTY_ID))
              .where(builder.equal(dataset.get(DatasetEntity.PROPERTY_FEATURE), subquery));
            return builder.in(root.get(DataEntity.PROPERTY_DATASET)).value(sq);
        };
    }

    public Specification<DataEntity<?>> withDatastreamIdentifier(final String datastreamIdentifier) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
            Join<DatastreamEntity, DatasetEntity> join = datastream.join(DatastreamEntity.PROPERTY_DATASETS);
            sq.select(join.get(DatasetEntity.PROPERTY_ID))
              .where(builder.equal(datastream.get(DatastreamEntity.PROPERTY_IDENTIFIER), datastreamIdentifier));
            return builder.in(root.get(DataEntity.PROPERTY_DATASET)).value(sq);
        };
    }

    public Specification<DataEntity<?>> withDatasetId(final long datasetId) {
        return (root, query, builder) -> {
            final Join<DataEntity, DatasetEntity> join =
                    root.join(DataEntity.PROPERTY_DATASET, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), datasetId);
        };
    }

    @Override
    public Specification<String> getIdSubqueryWithFilter(Specification filter) {
        return this.toSubquery(DataEntity.class, DataEntity.PROPERTY_IDENTIFIER, filter);
    }

    @Override
    public Specification<DataEntity<?>> getFilterForProperty(String propertyName,
                                                             Expression<?> propertyValue,
                                                             FilterConstants.ComparisonOperator operator,
                                                             boolean switched)
            throws STAInvalidFilterExpressionException {

        if (propertyName.equals(DATASTREAM) || propertyName.equals(FEATUREOFINTEREST)) {
            return handleRelatedPropertyFilter(propertyName, propertyValue);
        } else if (propertyName.equals("id")) {
            return (root, query, builder) -> {
                try {
                    return handleDirectStringPropertyFilter(root.get(DataEntity.PROPERTY_IDENTIFIER),
                                                            propertyValue,
                                                            operator,
                                                            builder,
                                                            false);
                } catch (STAInvalidFilterExpressionException e) {
                    throw new RuntimeException(e);
                }
                //
            };
        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
        }
    }

    private Specification<DataEntity<?>> handleRelatedPropertyFilter(String propertyName,
                                                                     Expression<?> propertyValue) {
        return (root, query, builder) -> {
            try {
                if (propertyName.equals(DATASTREAM)) {
                    Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
                    Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
                    Join<DatastreamEntity, DatasetEntity> join = datastream.join(DatastreamEntity.PROPERTY_DATASETS);
                    sq.select(join.get(DatasetEntity.PROPERTY_IDENTIFIER))
                      .where(builder.equal(datastream.get(DatastreamEntity.PROPERTY_IDENTIFIER), propertyValue));
                    return builder.in(root.get(DataEntity.PROPERTY_DATASET)).value(sq);
                    // return qobservation.dataset.id.in(JPAExpressions
                    // .selectFrom(qdatastream)
                    // .where(qdatastream.id.eq(propertyValue))
                    // .select(qdatastream.datasets.any().id));
                } else {
                    final Join<DataEntity, DatasetEntity> join =
                            root.join(DataEntity.PROPERTY_DATASET, JoinType.INNER);
                    return builder.equal(join.get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue);
                    // return qobservation.dataset.id.in(JPAExpressions
                    // .selectFrom(qdataset)
                    // .where(qdataset.feature.id.eq(propertyValue))
                    // .select(qdataset.id));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Specification<DataEntity<?>> handleDirectPropertyFilter(String propertyName,
                                                                    Expression<?> propertyValue,
                                                                    FilterConstants.ComparisonOperator operator,
                                                                    boolean switched) {
        return (root, query, builder) -> {
            try {
                switch (propertyName) {
                case "value":
                    // TODO: implement
                    /*
                    Subquery<QuantityDataEntity> sq = query.subquery(QuantityDataEntity.class);
                    Root<QuantityDataEntity> dataset = sq.from(QuantityDataEntity.class);
                    Predicate predicate = handleDirectNumberPropertyFilter(dataset.get(propertyName),
                                                                           Double.valueOf(propertyValue),
                                                                           operator,
                                                                           builder);
                    sq.select(dataset.get(QuantityDataEntity.PROPERTY_IDENTIFIER)).where(predicate);
                    return builder.in(root.get(DataEntity.PROPERTY_IDENTIFIER)).value(sq);
                    */
                    //return handleDirectNumberPropertyFilter(
                    //        dataset.<Number> get(propertyName),
                    //        propertyValue,
                    //        operator,
                    //        builder,
                    //        switched);
                case "samplingTimeEnd":
                    switch (operator) {
                    case PropertyIsLessThan:
                    case PropertyIsLessThanOrEqualTo:
                        return handleDirectDateTimePropertyFilter(
                                root.get(DataEntity.PROPERTY_SAMPLING_TIME_END),
                                propertyValue,
                                operator,
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
                                root.get(DataEntity.PROPERTY_SAMPLING_TIME_END),
                                propertyValue,
                                operator,
                                builder);
                        return builder.and(eqStart, eqEnd);
                    case PropertyIsNotEqualTo:
                        Predicate neStart = handleDirectDateTimePropertyFilter(
                                root.get(DataEntity.PROPERTY_SAMPLING_TIME_START),
                                propertyValue,
                                operator,
                                builder);
                        Predicate neEnd = handleDirectDateTimePropertyFilter(
                                root.get(DataEntity.PROPERTY_SAMPLING_TIME_END),
                                propertyValue,
                                operator,
                                builder);
                        return builder.or(neStart, neEnd);
                    default:
                        throw new STAInvalidFilterExpressionException("Operator not implemented!");
                    }
                case "resultTime":
                    return this.handleDirectDateTimePropertyFilter(
                            root.get(DataEntity.PROPERTY_RESULT_TIME),
                            propertyValue,
                            operator,
                            builder);
                default:
                    throw new STAInvalidFilterExpressionException("Currently not implemented!");
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
