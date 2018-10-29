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

import java.util.Locale;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.QDataEntity;
import org.n52.series.db.beans.QDatasetEntity;
import org.n52.series.db.beans.QFeatureEntity;
import org.n52.series.db.beans.QPhenomenonEntity;
import org.n52.series.db.beans.QProcedureEntity;
import org.n52.series.db.beans.sta.QDatastreamEntity;
import org.n52.series.db.beans.sta.QHistoricalLocationEntity;
import org.n52.series.db.beans.sta.QLocationEntity;
import org.n52.series.db.beans.sta.QThingEntity;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public abstract class EntityQuerySpecifications<T> {
    final static DatastreamQuerySpecifications dQS = new DatastreamQuerySpecifications();
    final static ObservationQuerySpecifications oQS = new ObservationQuerySpecifications();
    
    final static QDatastreamEntity qdatastream = QDatastreamEntity.datastreamEntity;
    final static QLocationEntity qlocation = QLocationEntity.locationEntity;
    final static QHistoricalLocationEntity qhistoricallocation = QHistoricalLocationEntity.historicalLocationEntity;
    final static QProcedureEntity qsensor = QProcedureEntity.procedureEntity;
    final static QDataEntity qobservation = QDataEntity.dataEntity;
    final static QFeatureEntity qfeature = QFeatureEntity.featureEntity;
    final static QDatasetEntity qdataset = QDatasetEntity.datasetEntity;
    final static QThingEntity qthing = QThingEntity.thingEntity;
    final static QPhenomenonEntity qobservedproperty = QPhenomenonEntity.phenomenonEntity;
    
    /**
     * 
     * @param filter
     * @return
     */
    public abstract JPQLQuery<Long> getIdSubqueryWithFilter(BooleanExpression filter);
    
    /**
     * Gets Entity-specific Filter for property with given name. Filters may not accept all BinaryOperators,
     * as they may not be defined for the datatype of the property. 
     * 
     * @param propertyName Name of the property to be filtered on
     * @param propertyValue supposed Value of the property
     * @param operator Operator to be used for comparing propertyValue and actual Value
     * @return BooleanExpression evaluating to true if Entity is not to be filtered out
     */
    public abstract BooleanExpression getFilterForProperty(String propertyName,
                                                           Object propertyValue,
                                                           BinaryOperatorKind operator)
            throws ExpressionVisitException;
    
    //TODO:JavaDoc
    protected JPQLQuery<Long> toSubquery(EntityPath<T> relation, Expression<Long> selector, BooleanExpression filter) {
        return JPAExpressions.selectFrom(relation)
                             .select(selector)
                             .where(filter);
    }
    
    protected StringExpression toStringExpression(Object expr) {
        if (expr instanceof String) {
            return Expressions.asString((String)expr);
        } else if (expr instanceof StringExpression){
            return (StringExpression)expr;
        } else {
            return null;
        }
    }
    
    protected NumberExpression toNumberExpression(Object expr) {
        if (expr instanceof String) {
            return Expressions.asNumber((Long)expr);
        } else if (expr instanceof NumberExpression){
            return (NumberExpression)expr;
        } else {
            return null;
        }
    }
    
    protected BooleanExpression handleIdPropertyFilter(NumberPath<Long> idPath,
                                                       Object propertyValue,
                                                       BinaryOperatorKind operator) throws ExpressionVisitException {
        NumberExpression<Long> value = toNumberExpression(propertyValue);
        switch (operator) {
        case GE:
            return idPath.goe(value);
        case GT:
            return idPath.gt(value);
        case LE:
            return idPath.loe(value);
        case LT:
            return idPath.lt(value);
        case EQ:
            return ((ComparableExpressionBase)idPath).eq(value);
        case NE:
            return ((ComparableExpressionBase)idPath).ne(value);
        default:
            throw new ExpressionVisitException("BinaryOperator \"" + operator.toString() + "\" is not supported for property \"id\"");
        }
    }
}
