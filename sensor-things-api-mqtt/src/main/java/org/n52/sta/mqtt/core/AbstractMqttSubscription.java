/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.core;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.n52.sta.service.query.QueryOptions;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public abstract class AbstractMqttSubscription {

    private String topic;

    private QueryOptions queryOptions;

    private EdmEntityType entityType;

    private EdmEntitySet entitySet;

    public AbstractMqttSubscription(String topic,
                                    QueryOptions queryOptions,
                                    EdmEntityType entityType,
                                    EdmEntitySet entitySet) {
        this.topic = topic;
        this.queryOptions = queryOptions;
        this.entityType = entityType;
        this.entitySet = entitySet;
    }

    /**
     * Returns the topic given entity should be posted to. null if the entity
     * does not match this subscription.
     *
     * @param entity Entity to be posted
     * @param differenceMap differenceMap names of properties that have changed.
     * if null all properties have changed (new entity)
     * @return Topic to be posted to. May be null if Entity does not match this
     * subscription.
     */
    public String checkSubscription(Entity entity, Map<String, Set<Long>> collections, Set<String> differenceMap) {
        return matches(entity, collections, differenceMap) ? topic : null;
    }
    
    public abstract boolean matches(Entity entity, Map<String, Set<Long>> collections, Set<String> differenceMap);
    
    public String getTopic() {
        return topic;
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public EdmEntityType getEdmEntityType() {
        return entityType;
    }

    public EdmEntitySet getEdmEntitySet() {
        return this.entitySet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof MQTTSubscription && ((MQTTSubscription) other).getTopic().equals(this.topic));
    }

}
