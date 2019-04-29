/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.service.handler.crud;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class EntityCrudRequestHandlerRepository {

    private ThingEntityCrudRequestHandler thingEntityCrudRequestHandler;

    private LocationEntityCrudRequestHandler locationEntityCrudRequestHandler;

    private HistoricalLocationEntityCrudRequestHandler historicalLocationEntityCrudRequestHandler;

    private SensorEntityCrudRequestHandler sensorEntityCrudRequestHandler;

    private DatastreamEntityCrudRequestHandler datastreamEntityCrudRequestHandler;

    private ObservationEntityCrudRequestHandler observationEntityCrudRequestHandler;

    private ObservedPropertyEntityCrudRequestHandler observedPropertyEntityCrudRequestHandler;

    private FeatureOfInterestEntityCrudRequestHandler featureOfInterestEntityCrudRequestHandler;

    public EntityCrudRequestHandlerRepository(ThingEntityCrudRequestHandler thingEntityCrudRequestHandler,
            LocationEntityCrudRequestHandler locationEntityCrudRequestHandler,
            HistoricalLocationEntityCrudRequestHandler historicalLocationEntityCrudRequestHandler,
            SensorEntityCrudRequestHandler sensorEntityCrudRequestHandler,
            DatastreamEntityCrudRequestHandler datastreamEntityCrudRequestHandler,
            ObservationEntityCrudRequestHandler observationEntityCrudRequestHandler,
            ObservedPropertyEntityCrudRequestHandler observedPropertyEntityCrudRequestHandler,
            FeatureOfInterestEntityCrudRequestHandler featureOfInterestEntityCrudRequestHandler) {
        this.thingEntityCrudRequestHandler = thingEntityCrudRequestHandler;
        this.locationEntityCrudRequestHandler = locationEntityCrudRequestHandler;
        this.historicalLocationEntityCrudRequestHandler = historicalLocationEntityCrudRequestHandler;
        this.sensorEntityCrudRequestHandler = sensorEntityCrudRequestHandler;
        this.datastreamEntityCrudRequestHandler = datastreamEntityCrudRequestHandler;
        this.observationEntityCrudRequestHandler = observationEntityCrudRequestHandler;
        this.observedPropertyEntityCrudRequestHandler = observedPropertyEntityCrudRequestHandler;
        this.featureOfInterestEntityCrudRequestHandler = featureOfInterestEntityCrudRequestHandler;

        final String message = "Unable to get EntityCrudRequestHandler Implementation: ";
        Assert.notNull(thingEntityCrudRequestHandler, message + thingEntityCrudRequestHandler.getClass().getName());
        Assert.notNull(locationEntityCrudRequestHandler,
                message + locationEntityCrudRequestHandler.getClass().getName());
        Assert.notNull(historicalLocationEntityCrudRequestHandler,
                message + historicalLocationEntityCrudRequestHandler.getClass().getName());
        Assert.notNull(sensorEntityCrudRequestHandler, message + sensorEntityCrudRequestHandler.getClass().getName());
        Assert.notNull(datastreamEntityCrudRequestHandler,
                message + datastreamEntityCrudRequestHandler.getClass().getName());
        Assert.notNull(observationEntityCrudRequestHandler,
                message + observationEntityCrudRequestHandler.getClass().getName());
        Assert.notNull(observedPropertyEntityCrudRequestHandler,
                message + observedPropertyEntityCrudRequestHandler.getClass().getName());
        Assert.notNull(featureOfInterestEntityCrudRequestHandler,
                message + featureOfInterestEntityCrudRequestHandler.getClass().getName());
    }

    public AbstractEntityCrudRequestHandler getEntityCrudRequestHandler(String type) {

        switch (type) {
        case "Thing": {
            return thingEntityCrudRequestHandler;
        }
        case "Location": {
            return locationEntityCrudRequestHandler;
        }
        case "HistoricalLocation": {
            return historicalLocationEntityCrudRequestHandler;
        }
        case "Sensor": {
            return sensorEntityCrudRequestHandler;
        }
        case "Datastream": {
            return datastreamEntityCrudRequestHandler;
        }
        case "Observation": {
            return observationEntityCrudRequestHandler;
        }
        case "ObservedProperty": {
            return observedPropertyEntityCrudRequestHandler;
        }
        case "FeatureOfInterest": {
            return featureOfInterestEntityCrudRequestHandler;
        }
        default: {
            // TODO: check if we need to do error handling for invalid endpoints
        }
        }
        return null;
    }

}
