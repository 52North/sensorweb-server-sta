/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.service;

import org.n52.sta.data.STAEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Repository for all Sensor Things entity data services
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class CoreServiceRepository {

    private Map<EntityTypes, AbstractSensorThingsEntityService<?, ?, ?>> entityServices = new LinkedHashMap<>();

    @Autowired private ThingService thingService;

    @Autowired private LocationService locationService;

    @Autowired private HistoricalLocationService historicalLocationService;

    @Autowired private SensorService sensorService;

    @Autowired private DatastreamService datastreamService;

    @Autowired private ObservationService observationService;

    @Autowired private ObservedPropertyService observedPropertyService;

    @Autowired private FeatureOfInterestService featureOfInterestService;

    @Autowired private STAEventHandler mqttSubscriptionEventHandler;

    @PostConstruct
    public void postConstruct() {
        this.thingService.setServiceRepository(this);
        entityServices.put(EntityTypes.Thing, thingService);
        entityServices.put(EntityTypes.Things, thingService);

        this.locationService.setServiceRepository(this);
        entityServices.put(EntityTypes.Location, locationService);
        entityServices.put(EntityTypes.Locations, locationService);

        this.historicalLocationService.setServiceRepository(this);
        entityServices.put(EntityTypes.HistoricalLocation, historicalLocationService);
        entityServices.put(EntityTypes.HistoricalLocations, historicalLocationService);

        this.sensorService.setServiceRepository(this);
        entityServices.put(EntityTypes.Sensor, sensorService);
        entityServices.put(EntityTypes.Sensors, sensorService);

        this.datastreamService.setServiceRepository(this);
        entityServices.put(EntityTypes.Datastream, datastreamService);
        entityServices.put(EntityTypes.Datastreams, datastreamService);

        this.observationService.setServiceRepository(this);
        entityServices.put(EntityTypes.Observation, observationService);
        entityServices.put(EntityTypes.Observations, observationService);

        this.observedPropertyService.setServiceRepository(this);
        entityServices.put(EntityTypes.ObservedProperty, observedPropertyService);
        entityServices.put(EntityTypes.ObservedProperties, observedPropertyService);

        this.featureOfInterestService.setServiceRepository(this);
        entityServices.put(EntityTypes.FeatureOfInterest, featureOfInterestService);
        entityServices.put(EntityTypes.FeaturesOfInterest, featureOfInterestService);

        this.mqttSubscriptionEventHandler.setServiceRepository(this);
    }

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type name of the requested entity service
     * @return the requested entity data service
     */
    public AbstractSensorThingsEntityService<?, ?, ?> getEntityService(String entityTypeName) {
        return getEntityService(EntityTypes.valueOf(entityTypeName));
    }

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type name of the requested entity service
     * @return the requested entity data service
     */
    public AbstractSensorThingsEntityService<?, ?, ?> getEntityService(EntityTypes entityTypeName) {
        return entityServices.get(entityTypeName);
    }

    public enum EntityTypes {
        Thing, Location, HistoricalLocation, Sensor, Datastream, Observation, ObservedProperty, FeatureOfInterest,
        Things, Locations, HistoricalLocations, Sensors, Datastreams, Observations, ObservedProperties,
        FeaturesOfInterest
    }

}
