/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.handler;

import java.util.List;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.n52.sta.mqtt.core.MqttPropertySubscription;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class MqttPropertySubscriptionHandler {

    public MqttPropertySubscription handlePropertyRequest(UriInfo uriInfo) throws ODataApplicationException {
        MqttPropertySubscription subscription = null;

        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

        // handle request depending on the number of UriResource paths
        // e.g. the case: sta/Things(id)/Locations(id)/name
        if (resourcePaths.get(1) instanceof UriResourceNavigation) {
            subscription = resolvePropertyForNavigation(resourcePaths);

            // e.g the case: sta/Things(id)/description
        } else {
            subscription = resolvePropertyForEntity(resourcePaths);

        }
        return subscription;
    }

    private MqttPropertySubscription resolvePropertyForNavigation(List<UriResource> resourcePaths) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private MqttPropertySubscription resolvePropertyForEntity(List<UriResource> resourcePaths) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
