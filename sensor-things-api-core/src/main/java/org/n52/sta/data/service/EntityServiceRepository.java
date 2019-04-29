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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
/**
 * Repository for all Sensor Things entity data services
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EntityServiceRepository {

//    private ThingService thingService;
//    private LocationService locationService;
//    private HistoricalLocationService historicalLocationService;
//    private SensorService sensorService;
//    private DatastreamService datastreamService;
//    private ObservationService observationService;
//    private ObservedPropertyService observedPropertyService;
//    private FeatureOfInterestService featureOfInterestService;

    private Map<EntityTypes, AbstractSensorThingsEntityService<?, ?>> entityServices = new LinkedHashMap<>();


    public EntityServiceRepository() {

    }

//    public EntityServiceRepository(ThingService thingService,
//                                   LocationService locationService,
//                                   HistoricalLocationService historicalLocationService,
//                                   SensorService sensorService,
//                                   DatastreamService datastreamService,
//                                   ObservationService observationService,
//                                   ObservedPropertyService observedPropertyService,
//                                   FeatureOfInterestService featureOfInterestService) {
//        this.thingService = thingService;
//        this.locationService = locationService;
//        this.historicalLocationService = historicalLocationService;
//        this.sensorService = sensorService;
//        this.datastreamService = datastreamService;
//        this.observationService = observationService;
//        this.observedPropertyService = observedPropertyService;
//        this.featureOfInterestService = featureOfInterestService;
//
//        final String message = "Unable to get Service Implementation: ";
//        Assert.notNull(thingService, message + thingService.getClass().getName());
//        Assert.notNull(locationService, message + locationService.getClass().getName());
//        Assert.notNull(historicalLocationService, message + historicalLocationService.getClass().getName());
//        Assert.notNull(sensorService, message + sensorService.getClass().getName());
//        Assert.notNull(datastreamService, message + datastreamService.getClass().getName());
//        Assert.notNull(observationService, message + observationService.getClass().getName());
//        Assert.notNull(observedPropertyService, message + observedPropertyService.getClass().getName());
//        Assert.notNull(featureOfInterestService, message + featureOfInterestService.getClass().getName());
//    }

    public void addEntityService(AbstractSensorThingsEntityService<?, ?> entityService) {
       entityServices.put(entityService.getType(), entityService);
    }

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type name of the requested entity service
     * @return the requested entity data service
     */
    public AbstractSensorThingsEntityService<?, ?> getEntityService(String entityTypeName) {
        return getEntityService(EntityTypes.valueOf(entityTypeName));
    }

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type name of the requested entity service
     * @return the requested entity data service
     */
    public AbstractSensorThingsEntityService<?, ?> getEntityService(EntityTypes entityTypeName) {
        return entityServices.get(entityTypeName);
//        AbstractSensorThingsEntityService<?, ?> entityService = null;
//
//        switch (entityTypeName) {
//        case Thing: {
//            entityService = thingService;
//            break;
//        }
//        case Location: {
//            entityService = locationService;
//            break;
//        }
//        case HistoricalLocation: {
//            entityService = historicalLocationService;
//            break;
//        }
//        case Sensor: {
//            entityService = sensorService;
//            break;
//        }
//        case Datastream: {
//            entityService = datastreamService;
//            break;
//        }
//        case Observation: {
//            entityService = observationService;
//            break;
//        }
//        case ObservedProperty: {
//            entityService = observedPropertyService;
//            break;
//        }
//        case FeatureOfInterest: {
//            entityService = featureOfInterestService;
//            break;
//        }
//        default: {
//            //TODO: check if we need to do error handling for invalid endpoints
//        }
//        }
//        return entityService;
    }

    public enum EntityTypes {
        Thing, Location, HistoricalLocation, Sensor, Datastream, Observation, ObservedProperty, FeatureOfInterest;
    }

}
