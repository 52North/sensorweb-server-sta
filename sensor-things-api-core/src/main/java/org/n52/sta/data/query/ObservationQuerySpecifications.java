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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class ObservationQuerySpecifications extends EntityQuerySpecifications<DataEntity< ? >> {

    public Specification<DataEntity< ? >> withFeatureOfInterest(Long featureId) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatasetEntity> dataset = sq.from(DatasetEntity.class);
            Join<DatastreamEntity, DatasetEntity> join = dataset.join(DatasetEntity.PROPERTY_FEATURE);
            sq.select(join.get(AbstractFeatureEntity.PROPERTY_ID)).where(builder.equal(dataset.get(DescribableEntity.PROPERTY_ID), featureId));
            return builder.in(root.get(DataEntity.PROPERTY_DATASET)).value(sq);
        };
    }

    public Specification<DataEntity< ? >> withDatastream(Long datastreamId) {
        return (root, query, builder) -> {
            Subquery<DatasetEntity> sq = query.subquery(DatasetEntity.class);
            Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
            Join<DatastreamEntity, DatasetEntity> join = datastream.join(DatastreamEntity.PROPERTY_DATASETS);
            sq.select(join.get(DatasetEntity.PROPERTY_ID)).where(builder.equal(datastream.get(DescribableEntity.PROPERTY_ID), datastreamId));
            return builder.in(root.get(DataEntity.PROPERTY_DATASET)).value(sq);
        };
    }

    public Specification<DataEntity< ? >> withDataset(Long datasetId) {
        return (root, query, builder) -> {
            final Join<DataEntity, DatasetEntity> join =
                    root.join(DataEntity.PROPERTY_DATASET, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), datasetId);
        };
    }

    @Override
    public Subquery<Long> getIdSubqueryWithFilter(Expression<Boolean> filter) {
//        return this.toSubquery(qobservation, qobservation.id, filter);
        return null;
    }

    @Override
    public Object getFilterForProperty(String propertyName,
                                       Object propertyValue,
                                       BinaryOperatorKind operator,
                                       boolean switched)
            throws ExpressionVisitException {
//        if (propertyName.equals("Datastream") || propertyName.equals("FeatureOfInterest")) {
//            return handleRelatedPropertyFilter(propertyName, (JPQLQuery<Long>) propertyValue);
//        } else if (propertyName.equals("id")) {
//            return handleDirectNumberPropertyFilter(qobservation.id, propertyValue, operator, switched);
//        } else {
//            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
//        }
        return null;
    }

//    private BooleanExpression handleRelatedPropertyFilter(String propertyName, JPQLQuery<Long> propertyValue)
//            throws ExpressionVisitException {
//        if (propertyName.equals("Datastream")) {
//            return qobservation.dataset.id.in(JPAExpressions
//                                              .selectFrom(qdatastream)
//                                              .where(qdatastream.id.eq(propertyValue))
//                                              .select(qdatastream.datasets.any().id));
//        } else {
//            return qobservation.dataset.id.in(JPAExpressions
//                                              .selectFrom(qdataset)
//                                              .where(qdataset.feature.id.eq(propertyValue))
//                                              .select(qdataset.id));
//        }
//    }
//
//    @SuppressWarnings({"unchecked", "rawtypes"})
//    private Object handleDirectPropertyFilter(String propertyName,
//                                              Object propertyValue,
//                                              BinaryOperatorKind operator,
//                                              boolean switched)
//            throws ExpressionVisitException {
//
//        switch (propertyName) {
//        case "value":
//            NumberPath<Double> property = new PathBuilder(DataEntity.class, qobservation.getRoot().toString())
//                                                                                                              .getNumber(propertyName,
//                                                                                                                         Double.class);
//            return handleDirectNumberPropertyFilter(property, propertyValue, operator, switched);
//        case "samplingTimeEnd":
//            DateTimeExpression<Date> value = (DateTimeExpression<Date>) propertyValue;
//            switch (operator) {
//            case LT:
//            case LE:
//                return handleDirectDateTimePropertyFilter(qobservation.samplingTimeEnd, value, operator, switched);
//            case GT:
//            case GE:
//                return handleDirectDateTimePropertyFilter(qobservation.samplingTimeStart, value, operator, switched);
//            case EQ:
//                BooleanExpression eqStart = (BooleanExpression) handleDirectDateTimePropertyFilter(qobservation.samplingTimeStart,
//                                                                                                   value,
//                                                                                                   operator,
//                                                                                                   switched);
//                BooleanExpression eqEnd = (BooleanExpression) handleDirectDateTimePropertyFilter(qobservation.samplingTimeEnd,
//                                                                                                 value,
//                                                                                                 operator,
//                                                                                                 switched);
//                return eqStart.and(eqEnd);
//            case NE:
//                BooleanExpression neStart = (BooleanExpression) handleDirectDateTimePropertyFilter(qobservation.samplingTimeStart,
//                                                                                                   value,
//                                                                                                   operator,
//                                                                                                   switched);
//                BooleanExpression neEnd = (BooleanExpression) handleDirectDateTimePropertyFilter(qobservation.samplingTimeEnd,
//                                                                                                 value,
//                                                                                                 operator,
//                                                                                                 switched);
//                return neStart.or(neEnd);
//            default:
//                throw new ExpressionVisitException("Currently not implemented!");
//            }
//        case "resultTime":
//            return this.handleDirectDateTimePropertyFilter(qobservation.resultTime, propertyValue, operator, switched);
//        default:
//            throw new ExpressionVisitException("Currently not implemented!");
//        }
//    }
}
