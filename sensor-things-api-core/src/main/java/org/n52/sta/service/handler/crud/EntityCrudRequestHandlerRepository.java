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
