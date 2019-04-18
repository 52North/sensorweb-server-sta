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
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FormatEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class SensorQuerySpecifications extends EntityQuerySpecifications<ProcedureEntity> {

    public Specification<ProcedureEntity> withDatastream(Long datastreamId) {
        return (root, query, builder) -> {
            Subquery<ProcedureEntity> sq = query.subquery(ProcedureEntity.class);
            Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
            Join<DatastreamEntity, ProcedureEntity> join = datastream.join(DatastreamEntity.PROPERTY_PROCEDURE);
            sq.select(join).where(builder.equal(datastream.get(DescribableEntity.PROPERTY_ID), datastreamId));
            return builder.in(root).value(sq);
        };
    }

    /**
     * Assures that Entity is valid. Entity is valid if: - has Datastream associated with it
     * 
     * @return BooleanExpression evaluating to true if Entity is valid
     */
    public Specification<ProcedureEntity> isValidEntity() {
//        return qsensor.id.in(dQS.toSubquery(qdatastream,
//                                            qdatastream.procedure.id,
//                                            qdatastream.isNotNull()));
        
        return (root, query, builder) -> {
            Subquery<ProcedureEntity> sq = query.subquery(ProcedureEntity.class);
            Root<DatastreamEntity> datastream = sq.from(DatastreamEntity.class);
            Join<DatastreamEntity, ProcedureEntity> join = datastream.join(DatastreamEntity.PROPERTY_PROCEDURE);
            sq.select(join).where(builder.isNotNull(datastream));
            return builder.in(root).value(sq);
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
//        return this.toSubquery(ProcedureEntity.class, DescribableEntity.PROPERTY_ID, filter);
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
//        if (propertyName.equals("Datastreams")) {
//            return handleRelatedPropertyFilter(propertyName, (JPQLQuery<Long>) propertyValue);
//        } else if (propertyName.equals("id")) {
//            return handleDirectNumberPropertyFilter(qsensor.id, propertyValue, operator, switched);
//        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
//        }
//        return null;
    }

//    private BooleanExpression handleRelatedPropertyFilter(String propertyName, JPQLQuery<Long> propertyValue)
//            throws ExpressionVisitException {
//            return qsensor.id.in(dQS.toSubquery(qdatastream,
//                                                qdatastream.procedure.id,
//                                                qdatastream.id.eq(propertyValue)));
//    }
//
//    private Object handleDirectPropertyFilter(String propertyName,
//                                              Object propertyValue,
//                                              BinaryOperatorKind operator,
//                                              boolean switched)
//            throws ExpressionVisitException {
//
//        switch (propertyName) {
//        case "name":
//            return handleDirectStringPropertyFilter(qsensor.name, propertyValue, operator, switched);
//        case "description":
//            return handleDirectStringPropertyFilter(qsensor.description, propertyValue, operator, switched);
//        case "format":
//        case "encodingType":
//            return handleDirectStringPropertyFilter(qsensor.format.format, propertyValue, operator, switched);
//        case "metadata":
//            return handleDirectStringPropertyFilter(qsensor.descriptionFile, propertyValue, operator, switched);
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
                    case "format":
                    case "encodingType":
                        Join<ProcedureEntity, FormatEntity> join = root.join(ProcedureEntity.PROPERTY_PROCEDURE_DESCRIPTION_FORMAT);
                        return handleDirectStringPropertyFilter(join.<String> get(FormatEntity.FORMAT), propertyValue, operator, builder, 
                                switched);
                    case "metadata":
                        return handleDirectStringPropertyFilter(
                                root.<String> get(ProcedureEntity.PROPERTY_DESCRIPTION_FILE), propertyValue, operator,
                                builder, switched);
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
