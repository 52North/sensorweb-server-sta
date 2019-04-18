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
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class ObservedPropertyQuerySpecifications extends EntityQuerySpecifications<PhenomenonEntity> {

    public Specification<PhenomenonEntity> withDatastream(Long datastreamId) {
        return (root, query, builder) -> {
            final Join<PhenomenonEntity, DatastreamEntity> join =
                    root.join(DatastreamEntity.PROPERTY_OBSERVABLE_PROPERTY, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), datastreamId);
        };
    }

    @Override
    public Subquery<Long> getIdSubqueryWithFilter(Expression<Boolean> filter) {
//        return this.toSubquery(qobservedproperty, qobservedproperty.id, filter);
        return null;
    }

    @Override
    public Object getFilterForProperty(String propertyName,
                                       Object propertyValue,
                                       BinaryOperatorKind operator,
                                       boolean switched)
            throws ExpressionVisitException {
//        if (propertyName.equals("Datastreams")) {
//            return handleRelatedPropertyFilter(propertyName, (JPQLQuery<Long>) propertyValue);
//        } else if (propertyName.equals("id")) {
//            return handleDirectNumberPropertyFilter(qobservedproperty.id, propertyValue, operator, switched);
//        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
//        }
//        return null;
    }

//    private BooleanExpression handleRelatedPropertyFilter(String propertyName, JPQLQuery<Long> propertyValue)
//            throws ExpressionVisitException {
//        return qobservedproperty.id.in(dQS.toSubquery(qdatastream,
//                                                      qdatastream.observableProperty.id,
//                                                      qdatastream.id.eq(propertyValue)));
//    }
//
//    private Object handleDirectPropertyFilter(String propertyName,
//                                              Object propertyValue,
//                                              BinaryOperatorKind operator,
//                                              boolean switched)
//            throws ExpressionVisitException {
//        switch (propertyName) {
//        case "name":
//            return handleDirectStringPropertyFilter(qobservedproperty.name, propertyValue, operator, switched);
//        case "description":
//            return handleDirectStringPropertyFilter(qobservedproperty.description, propertyValue, operator, switched);
//        case "definition":
//        case "identifier":
//            return handleDirectStringPropertyFilter(qobservedproperty.identifier, propertyValue, operator, switched);
//        default:
//            throw new ExpressionVisitException("Error getting filter for Property: \"" + propertyName
//                    + "\". No such property in Entity.");
//        }
//    }
    
    private Specification<PhenomenonEntity> handleDirectPropertyFilter(String propertyName, Object propertyValue,
            BinaryOperatorKind operator, boolean switched) {
        return new Specification<PhenomenonEntity>() {
            @Override
            public Predicate toPredicate(Root<PhenomenonEntity> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                try {
                    switch (propertyName) {
                    case "name":
                        return handleDirectStringPropertyFilter(root.<String> get(DescribableEntity.PROPERTY_NAME),
                                propertyValue, operator, builder, switched);
                    case "description":
                        return handleDirectStringPropertyFilter(
                                root.<String> get(DescribableEntity.PROPERTY_DESCRIPTION), propertyValue, operator,
                                builder, switched);
                    case "definition":
                    case "identifier":
                        return handleDirectStringPropertyFilter(
                                root.<String> get(DescribableEntity.PROPERTY_IDENTIFIER), propertyValue, operator,
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
