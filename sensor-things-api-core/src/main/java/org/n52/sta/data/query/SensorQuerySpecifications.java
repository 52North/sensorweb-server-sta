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
import org.n52.series.db.beans.ProcedureEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPQLQuery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class SensorQuerySpecifications extends EntityQuerySpecifications<ProcedureEntity> {

    public BooleanExpression withId(Long id) {
        return qsensor.id.eq(id);
    }
    
    public BooleanExpression wihtName(String name) {
        return qsensor.name.eq(name);
    }
    
    public BooleanExpression withIdentifier(String identifier) {
        return qsensor.identifier.eq(identifier);
    }
    
    public BooleanExpression withDatastream(Long datastreamId) {
        return qsensor.id.in(dQS.toSubquery(qdatastream,
                                            qdatastream.procedure.id,
                                            qdatastream.id.eq(datastreamId)));
    }

    /**
     * Assures that Entity is valid.
     * Entity is valid if:
     * - has Datastream associated with it
     * 
     * @return BooleanExpression evaluating to true if Entity is valid
     */
    public BooleanExpression isValidEntity() {
        return qsensor.id.in(dQS.toSubquery(qdatastream,
                                            qdatastream.procedure.id,
                                            qdatastream.isNotNull()));
    }

    /* (non-Javadoc)
     * @see org.n52.sta.data.query.EntityQuerySpecifications#getIdSubqueryWithFilter(com.querydsl.core.types.dsl.BooleanExpression)
     */
    @Override
    public JPQLQuery<Long> getIdSubqueryWithFilter(BooleanExpression filter) {
        return this.toSubquery(qsensor, qsensor.id, filter);
    }

    /* (non-Javadoc)
     * @see org.n52.sta.data.query.EntityQuerySpecifications#getFilterForProperty(java.lang.String, java.lang.Object, org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind)
     */
    @Override
    public BooleanExpression getFilterForProperty(String propertyName,
                                                  Object propertyValue,
                                                  BinaryOperatorKind operator) throws ExpressionVisitException {
        if (propertyName.equals("Datastreams")) {
            return handleRelatedPropertyFilter(propertyName, (JPQLQuery<Long>)propertyValue);
        } else if (propertyName.equals("id")) {
            return handleIdPropertyFilter(qsensor.id, propertyValue, operator);
        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator);
        }
    }
    
    private BooleanExpression handleRelatedPropertyFilter(String propertyName, JPQLQuery<Long> propertyValue) throws ExpressionVisitException {
        throw new ExpressionVisitException("Filtering by Related Properties with cardinality >1 is currently not supported!");
    }

    private BooleanExpression handleDirectPropertyFilter(String propertyName, Object propertyValue, BinaryOperatorKind operator) throws ExpressionVisitException {
        StringExpression value = toStringExpression(propertyValue);
        if (operator.equals(BinaryOperatorKind.EQ)) {
            switch(propertyName) {
            case "name": {
                return qsensor.name.eq(value);
            }
            case "description": {
                return qsensor.description.eq(value);
            }
            case "encodingType": {
                return qsensor.format.format.eq(value);
            }
            case "metadata": {
                return qsensor.descriptionFile.eq(value);
            }
            default:
                throw new ExpressionVisitException("Error getting filter for Property: \"" + propertyName + "\". No such property in Entity.");
            }
        } else if (operator.equals(BinaryOperatorKind.NE)){
            switch(propertyName) {
            case "name": {
                return qsensor.name.ne(value);
            }
            case "description": {
                return qsensor.description.ne(value);
            }
            case "encodingType": {
                return qsensor.format.format.ne(value);
            }
            case "metadata": {
                return qsensor.descriptionFile.ne(value);
            }
            default:
                throw new ExpressionVisitException("Error getting filter for Property: \"" + propertyName + "\". No such property in Entity.");
            }
        } else {
            throw new ExpressionVisitException("BinaryOperator \"" + operator.toString() + "\" is not supported for \"" + propertyName + "\"");
        }
    }
}
