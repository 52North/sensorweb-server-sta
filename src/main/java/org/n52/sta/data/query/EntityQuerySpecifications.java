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

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public abstract class EntityQuerySpecifications {

    public abstract <T> BooleanExpression selectFrom(JPQLQuery<T> subquery);

    public BooleanExpression getLocationEntityById(Long id) {
        LocationQuerySpecifications locQS = new LocationQuerySpecifications();
        return selectFrom(locQS.toSubquery(locQS.matchesId(id)));
    }
    
    public BooleanExpression getDatastreamEntityById(Long id) {
        DatastreamQuerySpecifications datastreamQS = new DatastreamQuerySpecifications();
        return selectFrom(datastreamQS.toSubquery(datastreamQS.matchesId(id)));
    }
    
    public BooleanExpression getHistoricalLocationEntityById(Long id) {
        HistoricalLocationQuerySpecifications historicalLocationQS = new HistoricalLocationQuerySpecifications();
        return selectFrom(historicalLocationQS.toSubquery(historicalLocationQS.matchesId(id)));
    }
    
    public BooleanExpression getThingEntityById(Long id) {
        ThingQuerySpecifications thingQS = new ThingQuerySpecifications();
        return selectFrom(thingQS.toSubquery(thingQS.matchesId(id)));
    }

    public BooleanExpression getSensorEntityById(Long id) {
        SensorQuerySpecifications sensorQS = new SensorQuerySpecifications();
        return selectFrom(sensorQS.toSubquery(sensorQS.matchesId(id)));
    }

    public BooleanExpression getObservedPropertyEntityById(Long id) {
        ObservedPropertyQuerySpecifications observedPropertyQS = new ObservedPropertyQuerySpecifications();
        return selectFrom(observedPropertyQS.toSubquery(observedPropertyQS.matchesId(id)));
    }
    
    public BooleanExpression getObservationEntityById(Long id) {
        ObservationQuerySpecifications observationQS = new ObservationQuerySpecifications();
        return selectFrom(observationQS.toSubquery(observationQS.matchesId(id)));
    }
}
