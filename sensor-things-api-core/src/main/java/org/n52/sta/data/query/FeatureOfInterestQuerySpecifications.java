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

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.AbstractFeatureEntity;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class FeatureOfInterestQuerySpecifications extends EntityQuerySpecifications<AbstractFeatureEntity< ? >> {

    public BooleanExpression withId(Long id) {
        return qfeature.id.eq(id);
    }

    public BooleanExpression withObservation(Long observationId) {
        return qfeature.id.in(JPAExpressions
                                            .selectFrom(qdataset)
                                            .where(qdataset.id.in(
                                                                  JPAExpressions
                                                                                .selectFrom(qobservation)
                                                                                .where(qobservation.id.eq(observationId))
                                                                                .select(qobservation.dataset.id)))
                                            .select(qdataset.feature.id));
    }

    public BooleanExpression withIdentifier(String identifier) {
        return qfeature.identifier.eq(identifier);
    }

    public BooleanExpression withName(String name) {
        return qfeature.name.eq(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sta.data.query.EntityQuerySpecifications#getIdSubqueryWithFilter(com.querydsl.core.types.dsl.
     * BooleanExpression)
     */
    @Override
    public JPQLQuery<Long> getIdSubqueryWithFilter(Expression<Boolean> filter) {
        return this.toSubquery(qfeature, qfeature.id, filter);
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
        if (propertyName.equals("Observations")) {
            return handleRelatedPropertyFilter(propertyName, (JPQLQuery<Long>) propertyValue, switched);
        } else if (propertyName.equals("id")) {
            return handleDirectNumberPropertyFilter(qfeature.id, propertyValue, operator, switched);
        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
        }
    }

    private BooleanExpression handleRelatedPropertyFilter(String propertyName,
                                                          JPQLQuery<Long> propertyValue,
                                                          boolean switched)
            throws ExpressionVisitException {
        return qfeature.id.in(JPAExpressions
                              .selectFrom(qdataset)
                              .where(qdataset.id.in(
                                                    JPAExpressions
                                                                  .selectFrom(qobservation)
                                                                  .where(qobservation.id.eq(propertyValue))
                                                                  .select(qobservation.dataset.id)))
                              .select(qdataset.feature.id));
    }

    private Object handleDirectPropertyFilter(String propertyName,
                                              Object propertyValue,
                                              BinaryOperatorKind operator,
                                              boolean switched)
            throws ExpressionVisitException {
        switch (propertyName) {
        case "name":
            return handleDirectStringPropertyFilter(qfeature.name, propertyValue, operator, switched);
        case "description":
            return handleDirectStringPropertyFilter(qfeature.description, propertyValue, operator, switched);
        case "encodingType":
        case "featureType":
            return handleStringFilter(toStringExpression("application/vnd.geo+json"), toStringExpression(propertyValue), operator, switched);
        default:
            throw new ExpressionVisitException("Error getting filter for Property: \"" + propertyName
                    + "\". No such property in Entity.");
        }
    }
}
