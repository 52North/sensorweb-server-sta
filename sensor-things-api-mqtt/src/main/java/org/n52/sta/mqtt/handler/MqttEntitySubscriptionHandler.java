/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.handler;

import java.util.List;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import static org.n52.sta.edm.provider.entities.AbstractSensorThingsEntityProvider.PROP_ID;
import org.n52.sta.mqtt.core.MqttEntitySubscription;
import org.n52.sta.mqtt.request.SensorThingsMqttRequest;
import org.n52.sta.service.handler.AbstractEntityRequestHandler;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.utils.EntityQueryParams;
import org.n52.sta.utils.UriResourceNavigationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class MqttEntitySubscriptionHandler extends AbstractEntityRequestHandler<SensorThingsMqttRequest, MqttEntitySubscription> {

    @Autowired
    private UriResourceNavigationResolver navigationResolver;

    @Override
    public MqttEntitySubscription handleEntityRequest(SensorThingsMqttRequest request) throws ODataApplicationException {
        MqttEntitySubscription subscription = null;

        // handle request depending on the number of UriResource paths
        // e.g the case: sta/Things
        if (request.getResourcePaths().size() == 1) {
            subscription = createResponseForEntity(request.getTopic(), request.getResourcePaths(), request.getQueryOptions());

            // e.g. the case: sta/Things(id)/Locations
        } else {
            subscription = createResponseForNavigation(request.getTopic(), request.getResourcePaths(), request.getQueryOptions());
        }
        return subscription;
    }

    private MqttEntitySubscription createResponseForEntity(String topic, List<UriResource> resourcePaths, QueryOptions queryOptions) throws ODataApplicationException {
        UriResourceEntitySet uriResourceEntitySet = navigationResolver.resolveRootUriResource(resourcePaths.get(0));
        Entity responseEntity = navigationResolver.resolveSimpleEntityRequest(uriResourceEntitySet);

        //TODO ensure Long typecasting
        return new MqttEntitySubscription((Long) responseEntity.getProperty(PROP_ID).getValue(),
                uriResourceEntitySet.getEntitySet(), uriResourceEntitySet.getEntityType(), topic, queryOptions);
    }

    private MqttEntitySubscription createResponseForNavigation(String topic, List<UriResource> resourcePaths, QueryOptions queryOptions) throws ODataApplicationException {
        EntityQueryParams requestParams = navigationResolver.resolveUriResourceNavigationPaths(resourcePaths);
        Entity responseEntity = navigationResolver.resolveComplexEntityRequest(resourcePaths, requestParams);

        return new MqttEntitySubscription((Long) responseEntity.getProperty(PROP_ID).getValue(),
                requestParams.getTargetEntitySet(), requestParams.getTargetEntitySet().getEntityType(), topic, queryOptions);
    }

}
