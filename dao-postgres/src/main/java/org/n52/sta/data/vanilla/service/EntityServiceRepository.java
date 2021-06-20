/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.data.vanilla.service;

import org.n52.sta.api.AbstractSensorThingsEntityService;
import org.n52.sta.api.EntityServiceFactory;
import org.n52.sta.api.STAEventHandler;
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
public class EntityServiceRepository implements EntityServiceFactory {

    private Map<EntityTypes, ServiceFacade<?, ?>> entityServices = new LinkedHashMap<>();

    @Autowired
    private ServiceFacade.ThingServiceFacade thingServiceFacade;

    @Autowired
    private ServiceFacade.LocationServiceFacade locationServiceFacade;

    @Autowired
    private ServiceFacade.HistoricalLocationServiceFacade historicalLocationService;

    @Autowired
    private ServiceFacade.SensorServiceFacade sensorService;

    @Autowired
    private ServiceFacade.DatastreamServiceFacade datastreamService;

    @Autowired
    private ServiceFacade.ObservationServiceFacade observationService;

    @Autowired
    private ServiceFacade.ObservedPropertyServiceFacade observedPropertyService;

    @Autowired
    private ServiceFacade.FeatureOfInterestServiceFacade featureOfInterestService;

    @Autowired
    private ServiceFacade.ObservationGroupServiceFacade obsGroupService;

    @Autowired
    private ServiceFacade.ObservationRelationServiceFacade obsRelationService;

    @Autowired
    private ServiceFacade.LicenseServiceFacade licenseService;

    @Autowired
    private ServiceFacade.PartyServiceFacade partyService;

    @Autowired
    private ServiceFacade.ProjectServiceFacade projectService;

    @Autowired
    private STAEventHandler mqttSubscriptionEventHandler;

    @PostConstruct
    public void postConstruct() {
        entityServices.put(EntityTypes.Thing, thingServiceFacade);
        entityServices.put(EntityTypes.Things, thingServiceFacade);

        entityServices.put(EntityTypes.Location, locationServiceFacade);
        entityServices.put(EntityTypes.Locations, locationServiceFacade);

        entityServices.put(EntityTypes.HistoricalLocation, historicalLocationService);
        entityServices.put(EntityTypes.HistoricalLocations, historicalLocationService);

        entityServices.put(EntityTypes.Sensor, sensorService);
        entityServices.put(EntityTypes.Sensors, sensorService);

        entityServices.put(EntityTypes.Datastream, datastreamService);
        entityServices.put(EntityTypes.Datastreams, datastreamService);

        entityServices.put(EntityTypes.Observation, observationService);
        entityServices.put(EntityTypes.Observations, observationService);

        entityServices.put(EntityTypes.ObservedProperty, observedPropertyService);
        entityServices.put(EntityTypes.ObservedProperties, observedPropertyService);

        entityServices.put(EntityTypes.FeatureOfInterest, featureOfInterestService);
        entityServices.put(EntityTypes.FeaturesOfInterest, featureOfInterestService);

        entityServices.put(EntityTypes.ObservationGroup, obsGroupService);
        entityServices.put(EntityTypes.ObservationGroups, obsGroupService);

        entityServices.put(EntityTypes.ObservationRelation, obsRelationService);
        entityServices.put(EntityTypes.ObservationRelations, obsRelationService);

        entityServices.put(EntityTypes.Objects, obsRelationService);
        entityServices.put(EntityTypes.Object, observationService);

        entityServices.put(EntityTypes.Subjects, obsRelationService);
        entityServices.put(EntityTypes.Subject, observationService);

        entityServices.put(EntityTypes.License, licenseService);
        entityServices.put(EntityTypes.Licenses, licenseService);

        entityServices.put(EntityTypes.Party, partyService);
        entityServices.put(EntityTypes.Parties, partyService);

        entityServices.put(EntityTypes.Project, projectService);
        entityServices.put(EntityTypes.Projects, projectService);

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
        return entityServices.get(EntityTypes.valueOf(entityTypeName));
    }

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type name of the requested entity service
     * @return the requested entity data service
     */
    public AbstractSensorThingsEntityServiceImpl getEntityServiceRaw(EntityTypes entityTypeName) {
        return entityServices.get(entityTypeName).getServiceImpl();
    }

    public enum EntityTypes {
        Thing, Location, HistoricalLocation, Sensor, Datastream, Observation, ObservedProperty, FeatureOfInterest,
        Things, Locations, HistoricalLocations, Sensors, Datastreams, Observations, ObservedProperties,
        FeaturesOfInterest, ObservationGroup, ObservationGroups, Subject, Subjects, Object, Objects,
        ObservationRelation,
        ObservationRelations, License, Licenses, Party, Parties, Project, Projects
    }

}
