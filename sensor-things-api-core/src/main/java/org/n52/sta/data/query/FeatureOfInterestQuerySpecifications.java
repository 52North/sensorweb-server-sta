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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class FeatureOfInterestQuerySpecifications extends EntityQuerySpecifications<AbstractFeatureEntity< ? >> {

//    public BooleanExpression withObservation(Long observationId) {
//        return qfeature.id.in(JPAExpressions
//                                            .selectFrom(qdataset)
//                                            .where(qdataset.id.in(
//                                                                  JPAExpressions
//                                                                                .selectFrom(qobservation)
//                                                                                .where(qobservation.id.eq(observationId))
//                                                                                .select(qobservation.dataset.id)))
//                                            .select(qdataset.feature.id));
//    }
    
    public Specification<AbstractFeatureEntity<?>> withObservation(Long observationId) {
        return (root, query, builder) -> {
//            Subquery<AbstractFeatureEntity> sq = query.subquery(AbstractFeatureEntity.class);
//            Root<DatasetEntity> dataset = sq.from(DatasetEntity.class);
//            Join<DatasetEntity, AbstractFeatureEntity> joinFeature = dataset.join(DatasetEntity.PROPERTY_FEATURE);
//            
//            Subquery<AbstractFeatureEntity> sqData = query.subquery(AbstractFeatureEntity.class);
//            Root<DataEntity> observation = sqData.from(DataEntity.class);
//            Join<DataEntity, DatasetEntity> joinDataset = dataset.join(DataEntity.PROPERTY_DATASET);
//            
//            sq.select(joinFeature).where(builder.equal(dataset.get(DescribableEntity.PROPERTY_ID), observationId));
//            return builder.in(root).value(sq);
            return null;
        };
    }

    @Override
    public Specification<AbstractFeatureEntity<?>> withIdentifier(final String identifier) {
        return (root, query, builder) -> {
            return builder.equal(root.get(DescribableEntity.PROPERTY_ID), identifier);
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sta.data.query.EntityQuerySpecifications#getIdSubqueryWithFilter(com.querydsl.core.types.dsl.
     * BooleanExpression)
     */
    @Override
    public Subquery<Long> getIdSubqueryWithFilter(Expression<Boolean> filter) {
//        return this.toSubquery(qfeature, qfeature.id, filter);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sta.data.query.EntityQuerySpecifications#getFilterForProperty(java.lang.String,
     * java.lang.Object, org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind)
     */
    @Override
    public Object getFilterForProperty(String propertyName,
                                       Object propertyValue,
                                       BinaryOperatorKind operator,
                                       boolean switched)
            throws ExpressionVisitException {
//        if (propertyName.equals("Observations")) {
//            return handleRelatedPropertyFilter(propertyName, (JPQLQuery<Long>) propertyValue, switched);
//        } else if (propertyName.equals("id")) {
//            return handleDirectNumberPropertyFilter(qfeature.id, propertyValue, operator, switched);
//        } else {
//            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
//        }
        return null;
    }

//    private BooleanExpression handleRelatedPropertyFilter(String propertyName,
//                                                          JPQLQuery<Long> propertyValue,
//                                                          boolean switched)
//            throws ExpressionVisitException {
//        return qfeature.id.in(JPAExpressions
//                              .selectFrom(qdataset)
//                              .where(qdataset.id.in(
//                                                    JPAExpressions
//                                                                  .selectFrom(qobservation)
//                                                                  .where(qobservation.id.eq(propertyValue))
//                                                                  .select(qobservation.dataset.id)))
//                              .select(qdataset.feature.id));
//    }
//
//    private Object handleDirectPropertyFilter(String propertyName,
//                                              Object propertyValue,
//                                              BinaryOperatorKind operator,
//                                              boolean switched)
//            throws ExpressionVisitException {
//        switch (propertyName) {
//        case "name":
//            return handleDirectStringPropertyFilter(qfeature.name, propertyValue, operator, switched);
//        case "description":
//            return handleDirectStringPropertyFilter(qfeature.description, propertyValue, operator, switched);
//        case "encodingType":
//        case "featureType":
//            return handleStringFilter("application/vnd.geo+json"), propertyValue, operator, switched);
//        default:
//            throw new ExpressionVisitException("Error getting filter for Property: \"" + propertyName
//                    + "\". No such property in Entity.");
//        }
//    }
    
    private Specification<ProcedureEntity> handleDirectPropertyFilter(String propertyName, Object propertyValue,
            BinaryOperatorKind operator, boolean switched) {
        return new Specification<ProcedureEntity>() {
            @Override
            public Predicate toPredicate(Root<ProcedureEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                try {
                    switch (propertyName) {
                    case "name":
                        return handleDirectStringPropertyFilter(root.<String> get(DescribableEntity.PROPERTY_NAME),
                                propertyValue, operator, builder, switched);
                    case "description":
                        return handleDirectStringPropertyFilter(
                                root.<String> get(DescribableEntity.PROPERTY_DESCRIPTION), propertyValue, operator,
                                builder, switched);
                    case "encodingType":
                    case "featureType":
                        return handleStringFilter(root.<String> get("application/vnd.geo+json"), (String) propertyValue, operator, builder, switched);
//                        Join<AbstractFeatureEntity<?>, FormatEntity> join = root.join(ProcedureEntity.PROPERTY_PROCEDURE_DESCRIPTION_FORMAT);
//                        return handleDirectStringPropertyFilter(join.<String> get(FormatEntity.FORMAT), propertyValue, operator, builder, 
//                                switched);
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
