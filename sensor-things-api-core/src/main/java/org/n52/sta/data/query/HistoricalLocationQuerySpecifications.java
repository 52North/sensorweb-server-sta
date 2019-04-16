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

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class HistoricalLocationQuerySpecifications extends EntityQuerySpecifications<HistoricalLocationEntity> {

//    public BooleanExpression withRelatedLocation(Long historicalId) {
//        return qhistoricallocation.locationEntities.any().id.eq(historicalId);
//    }
    
    public Specification<HistoricalLocationEntity> withRelatedLocation(final String historicalId) {
        return (root, query, builder) -> {
            final Join<HistoricalLocationEntity, LocationEntity> join =
                    root.join(HistoricalLocationEntity.PROPERTY_LOCATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), historicalId);
        };
    }

//    public BooleanExpression withRelatedThing(Long thingId) {
//        return qhistoricallocation.thingEntity.id.eq(thingId);
//    }
    
    public Specification<HistoricalLocationEntity> withRelatedThing(final String thingId) {
        return (root, query, builder) -> {
            final Join<HistoricalLocationEntity, PlatformEntity> join =
                    root.join(HistoricalLocationEntity.PROPERTY_THING, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), thingId);
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
    public JPQLQuery<Long> getIdSubqueryWithFilter(Expression<Boolean> filter) {
        return this.toSubquery(qhistoricallocation, qhistoricallocation.id, filter);
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
        if (propertyName.equals("Thing") || propertyName.equals("Locations")) {
            return handleRelatedPropertyFilter(propertyName, (JPQLQuery<Long>) propertyValue, switched);
        } else if (propertyName.equals("id")) {
            return handleDirectNumberPropertyFilter(qhistoricallocation.id, propertyValue, operator, switched);
        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
        }
    }

    private BooleanExpression handleRelatedPropertyFilter(String propertyName,
                                                          JPQLQuery<Long> propertyValue,
                                                          boolean switched)
            throws ExpressionVisitException {
        if (propertyName.equals("Thing")) {
            return qhistoricallocation.thingEntity.id.eqAny(propertyValue);
        } else {
            return qhistoricallocation.locationEntities.any().id.eqAny(propertyValue);
        }
    }

    private Object handleDirectPropertyFilter(String propertyName,
                                              Object propertyValue,
                                              BinaryOperatorKind operator,
                                              boolean switched)
            throws ExpressionVisitException {
        switch (propertyName) {
        case "time":
            return handleDirectDateTimePropertyFilter(qhistoricallocation.time, propertyValue, operator, switched);
        default:
            throw new ExpressionVisitException("Currently not implemented!");
        }
    }
}
