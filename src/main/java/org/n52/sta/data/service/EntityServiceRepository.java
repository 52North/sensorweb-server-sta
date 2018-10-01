/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data.service;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Repository for all Sensor Things entity data services
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EntityServiceRepository {

    private ThingService thingService;
    private LocationService locationService;
    private HistoricalLocationService historicalLocationService;
    private SensorService sensorService;
    private DatastreamService datastreamService;
    private ObservationService observationService;
    private ObservedPropertyService observedPropertyService;
    private FeatureOfInterestService featureOfInterestService;

    public EntityServiceRepository(ThingService thingService,
                                   LocationService locationService,
                                   HistoricalLocationService historicalLocationService,
                                   SensorService sensorService,
                                   DatastreamService datastreamService,
                                   ObservationService observationService,
                                   ObservedPropertyService observedPropertyService,
                                   FeatureOfInterestService featureOfInterestService) {
        this.thingService = thingService;
        this.locationService = locationService;
        this.historicalLocationService = historicalLocationService;
        this.sensorService = sensorService;
        this.datastreamService = datastreamService;
        this.observationService = observationService;
        this.observedPropertyService = observedPropertyService;
        this.featureOfInterestService = featureOfInterestService;

        final String message = "Unable to get Service Implementation: "; 
        Assert.notNull(thingService, message + thingService.getClass().getName());
        Assert.notNull(locationService, message + locationService.getClass().getName());
        Assert.notNull(historicalLocationService, message + historicalLocationService.getClass().getName());
        Assert.notNull(sensorService, message + sensorService.getClass().getName());
        Assert.notNull(datastreamService, message + datastreamService.getClass().getName());
        Assert.notNull(observationService, message + observationService.getClass().getName());
        Assert.notNull(observedPropertyService, message + observedPropertyService.getClass().getName());
        Assert.notNull(featureOfInterestService, message + featureOfInterestService.getClass().getName());
    }

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type name of the requested entity service
     * @return the requested entity data service
     */
    public AbstractSensorThingsEntityService<?> getEntityService(String entityTypeName) {
        AbstractSensorThingsEntityService<?> entityService = null;

        switch (entityTypeName) {
        case "Thing": {
            entityService = thingService;
            break;
        }
        case "Location": {
            entityService = locationService;
            break;
        }
        case "HistoricalLocation": {
            entityService = historicalLocationService;
            break;
        }
        case "Sensor": {
            entityService = sensorService;
            break;
        }
        case "Datastream": {
            entityService = datastreamService;
            break;
        }
        case "Observation": {
            entityService = observationService;
            break;
        }
        case "ObservedProperty": {
            entityService = observedPropertyService;
            break;
        }
        case "FeatureOfInterest": {
            entityService = featureOfInterestService;
            break;
        }
        default: {
            //TODO: check if we need to do error handling for invalid endpoints
        }
        }
        return entityService;
    }

}
