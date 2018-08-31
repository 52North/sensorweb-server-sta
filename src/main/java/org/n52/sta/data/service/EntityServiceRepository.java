/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Repository for all Sensor Things entity data services
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class EntityServiceRepository {

    @Autowired
    private ThingService thingService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private HistoricalLocationService historicalLocationService;

    @Autowired
    private SensorService sensorService;

    /**
     * Provides an entity data service for a entity type
     *
     * @param entityTypeName the type name of the requested entity service
     * @return the requested entity data service
     */
    public AbstractSensorThingsEntityService getEntityService(String entityTypeName) {
        AbstractSensorThingsEntityService entityService = null;

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
            default: {
                //TODO: check if we need to do error handling for invalid endpoints

            }
        }
        return entityService;
    }

}
