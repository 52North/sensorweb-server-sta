/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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

import org.n52.sta.api.AbstractSensorThingsEntityService;
import org.n52.sta.api.EntityServiceFactory;
import org.n52.sta.api.STAEventHandler;
import org.n52.sta.data.CommonSTAServiceImpl;
import org.n52.sta.data.CommonServiceFacade;
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
public class CommonEntityServiceRepository implements EntityServiceFactory {

    protected Map<String, CommonServiceFacade<?, ?>> entityServices = new LinkedHashMap<>();

    @Autowired
    private CommonServiceFacade.ThingServiceFacade thingServiceFacade;

    @Autowired
    private CommonServiceFacade.LocationServiceFacade locationServiceFacade;

    @Autowired
    private CommonServiceFacade.HistoricalLocationServiceFacade historicalLocationService;

    @Autowired
    private CommonServiceFacade.SensorServiceFacade sensorService;

    @Autowired
    private CommonServiceFacade.DatastreamServiceFacade datastreamService;

    @Autowired
    private CommonServiceFacade.ObservationServiceFacade observationService;

    @Autowired
    private CommonServiceFacade.ObservedPropertyServiceFacade observedPropertyService;

    @Autowired
    private CommonServiceFacade.FeatureOfInterestServiceFacade featureOfInterestService;

    @Autowired
    private STAEventHandler mqttSubscriptionEventHandler;

    @PostConstruct
    public void postConstruct() {
        entityServices.put(EntityTypes.Thing.name(), thingServiceFacade);
        entityServices.put(EntityTypes.Things.name(), thingServiceFacade);

        entityServices.put(EntityTypes.Location.name(), locationServiceFacade);
        entityServices.put(EntityTypes.Locations.name(), locationServiceFacade);

        entityServices.put(EntityTypes.HistoricalLocation.name(), historicalLocationService);
        entityServices.put(EntityTypes.HistoricalLocations.name(), historicalLocationService);

        entityServices.put(EntityTypes.Sensor.name(), sensorService);
        entityServices.put(EntityTypes.Sensors.name(), sensorService);

        entityServices.put(EntityTypes.Datastream.name(), datastreamService);
        entityServices.put(EntityTypes.Datastreams.name(), datastreamService);

        entityServices.put(EntityTypes.Observation.name(), observationService);
        entityServices.put(EntityTypes.Observations.name(), observationService);

        entityServices.put(EntityTypes.ObservedProperty.name(), observedPropertyService);
        entityServices.put(EntityTypes.ObservedProperties.name(), observedPropertyService);

        entityServices.put(EntityTypes.FeatureOfInterest.name(), featureOfInterestService);
        entityServices.put(EntityTypes.FeaturesOfInterest.name(), featureOfInterestService);

        mqttSubscriptionEventHandler.setServiceRepository(this);

        entityServices.forEach(
            (t, e) -> e.getServiceImpl().setServiceRepository(this)
        );
    }

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type name of the requested entity service
     * @return the requested entity data service
     */
    @Override public AbstractSensorThingsEntityService<?> getEntityService(String entityTypeName) {
        return entityServices.get(entityTypeName);
    }

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type of the requested entity service
     * @return the requested entity data service
     */
    public CommonSTAServiceImpl<?, ?, ?> getEntityServiceRaw(String entityTypeName) {
        return entityServices.get(entityTypeName).getServiceImpl();
    }

    public enum EntityTypes {
        Thing, Location, HistoricalLocation, Sensor, Datastream, Observation, ObservedProperty, FeatureOfInterest,
        Things, Locations, HistoricalLocations, Sensors, Datastreams, Observations, ObservedProperties,
        FeaturesOfInterest
    }

}
