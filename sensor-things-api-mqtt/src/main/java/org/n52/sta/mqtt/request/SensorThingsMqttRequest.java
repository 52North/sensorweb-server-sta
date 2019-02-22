/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.request;

import java.util.List;
import org.apache.olingo.server.api.uri.UriResource;
import org.n52.sta.service.query.QueryOptions;
import org.n52.sta.service.request.SensorThingsRequest;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class SensorThingsMqttRequest extends SensorThingsRequest {

    private String topic;

    public SensorThingsMqttRequest() {
        super();
    }

    /**
     *
     * @param resourcePaths list of {@Link UriResource}
     * @param queryOptions {@Link QueryOptions} for the request
     */
    public SensorThingsMqttRequest(String topic, List<UriResource> resourcePaths, QueryOptions queryOptions) {
        super(resourcePaths, queryOptions);
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
