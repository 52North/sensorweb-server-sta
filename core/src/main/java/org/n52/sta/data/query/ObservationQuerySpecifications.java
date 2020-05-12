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
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.ObservationEntity;
import org.n52.shetland.ogc.filter.FilterConstants;
import org.n52.shetland.ogc.filter.FilterConstants.ComparisonOperator;
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

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
public class ObservationQuerySpecifications extends EntityQuerySpecifications<ObservationEntity<?>> {

    public Specification<ObservationEntity<?>> withFeatureOfInterestStaIdentifier(final String featureIdentifier) {
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

    public Specification<ObservationEntity<?>> withDatastreamStaIdentifier(final String datastreamIdentifier) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
            Join<DatastreamEntity, DatasetEntity> join = datastream.join(DatastreamEntity.PROPERTY_DATASETS);
            sq.select(join.get(DatasetEntity.PROPERTY_ID)).where(
                    builder.equal(datastream.get(DatastreamEntity.PROPERTY_STA_IDENTIFIER), datastreamIdentifier));
            return builder.in(root.get(ObservationEntity.PROPERTY_DATASET)).value(sq);
        };
    }

    public Specification<ObservationEntity<?>> withDatasetId(final long datasetId) {
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
                case "value":
                    String type = propertyValue.getJavaType().getName();
                    if (type.equals(Double.class.getName())
                            || type.equals(Long.class.getName())
                            || type.equals(Integer.class.getName())) {
                        return createNumericPredicate(root,
                                                      query,
                                                      builder,
                                                      ObservationEntity.class,
                                                      propertyName,
                                                      propertyValue,
                                                      operator
                        );
                    } else if (type.equals(String.class.getName())) {
                        return createStringPredicate(root,
                                                     query,
                                                     builder,
                                                     ObservationEntity.class,
                                                     propertyName,
                                                     propertyValue,
                                                     operator
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
                    }
                default:
                    throw new STAInvalidFilterExpressionException("Currently not implemented!");
                }
            } catch (STAInvalidFilterExpressionException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private <T extends ObservationEntity> Predicate createNumericPredicate(Root<ObservationEntity<?>> root,
                                                                           CriteriaQuery<?> query,
                                                                           CriteriaBuilder builder,
                                                                           Class<T> clazz,
                                                                           String propertyName,
                                                                           Expression<?> propertyValue,
                                                                           ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {
        Subquery<T> sq = query.subquery(clazz);
        Root<T> entity = sq.from(clazz);
        Predicate predicate =
                handleDirectNumberPropertyFilter(entity.get(propertyName), propertyValue, operator, builder);
        sq.select(entity.get(ObservationEntity.PROPERTY_ID)).where(predicate);
        return builder.in(root.get(ObservationEntity.PROPERTY_ID)).value(sq);
    }

    private <T extends ObservationEntity> Predicate createStringPredicate(Root<ObservationEntity<?>> root,
                                                                          CriteriaQuery<?> query,
                                                                          CriteriaBuilder builder,
                                                                          Class<T> clazz,
                                                                          String propertyName,
                                                                          Expression<?> propertyValue,
                                                                          ComparisonOperator operator)
            throws STAInvalidFilterExpressionException {
        Subquery<T> sq = query.subquery(clazz);
        Root<T> entity = sq.from(clazz);
        Predicate predicate =
                handleDirectStringPropertyFilter(entity.get(propertyName),
                                                 propertyValue,
                                                 operator,
                                                 builder,
                                                 false);
        sq.select(entity.get(ObservationEntity.PROPERTY_ID)).where(predicate);
        return builder.in(root.get(ObservationEntity.PROPERTY_ID)).value(sq);
    }
}
