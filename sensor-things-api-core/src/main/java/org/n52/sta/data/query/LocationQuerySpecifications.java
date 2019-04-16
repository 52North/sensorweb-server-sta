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
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.AbstractFeatureEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class LocationQuerySpecifications extends EntityQuerySpecifications<LocationEntity> {

//    public BooleanExpression withRelatedHistoricalLocation(Long historicalId) {
//        return qlocation.historicalLocationEntities.any().id.eq(historicalId);
//    }
    
    public Specification<LocationEntity> withRelatedHistoricalLocation(Long historicalId) {
        return (root, query, builder) -> {
            Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
            Root<HistoricalLocationEntity> dataset = sq.from(HistoricalLocationEntity.class);
            Join<HistoricalLocationEntity, LocationEntity> joinFeature = dataset.join(HistoricalLocationEntity.PROPERTY_LOCATIONS);
            sq.select(joinFeature).where(builder.equal(LocationEntity.get(DescribableEntity.PROPERTY_ID), historicalId));
            return builder.in(root).value(sq);
        };
    }

//    public BooleanExpression withRelatedThing(Long thingId) {
//        return qlocation.thingEntities.any().id.eq(thingId);
//    }
//    
    public Specification<LocationEntity> withRelatedThing(Long thingId) {
        return (root, query, builder) -> {
            Subquery<LocationEntity> sq = query.subquery(LocationEntity.class);
            Root<ThingEntity> dataset = sq.from(ThingEntity.class);
            Join<ThingEntity, LocationEntity> joinFeature = dataset.join(ThingEntity.PROPERTY_LOCATIONS);
            sq.select(joinFeature).where(builder.equal(LocationEntity.get(DescribableEntity.PROPERTY_ID), thingId));
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
    public JPQLQuery<Long> getIdSubqueryWithFilter(Expression<Boolean> filter) {
        return this.toSubquery(qlocation, qlocation.id, filter);
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
        if (propertyName.equals("Things") || propertyName.equals("HistoricalLocations")) {
            return handleRelatedPropertyFilter(propertyName, (JPQLQuery<Long>) propertyValue);
        } else if (propertyName.equals("id")) {
            return handleDirectNumberPropertyFilter(qlocation.id, propertyValue, operator, switched);
        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
        }
    }

    private BooleanExpression handleRelatedPropertyFilter(String propertyName, JPQLQuery<Long> propertyValue)
            throws ExpressionVisitException {
        if (propertyName.equals("Things")) {
            return qlocation.thingEntities.any().id.eqAny(propertyValue);
        } else {
            return qlocation.historicalLocationEntities.any().id.eqAny(propertyValue);
        }
    }

    private Predicate handleDirectPropertyFilter(String propertyName,
                                              Object propertyValue,
                                              BinaryOperatorKind operator,
                                              Root<?> root,
                                              CriteriaBuilder criteriaBuilder,
                                              boolean switched)
            throws ExpressionVisitException {
        switch (propertyName) {
        case "name":
            return handleDirectStringPropertyFilter(root.get(LocationEntity.PROPERTY_NAME), propertyValue, operator, criteriaBuilder, switched);
        case "description":
            return handleDirectStringPropertyFilter(root.get(LocationEntity.PROPERTY_NAME), propertyValue, operator, criteriaBuilder, switched);
        case "encodingType":
            return handleDirectStringPropertyFilter(qlocation.locationEncoding.encodingType,
                                                    propertyValue,
                                                    operator,
                                                    switched);
        default:
            throw new ExpressionVisitException("Error getting filter for Property: \"" + propertyName
                    + "\". No such property in Entity.");
        }
    }
}
