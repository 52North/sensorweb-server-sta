/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.core;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.n52.sta.service.query.QueryOptions;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class MqttEntityCollectionSubscription extends AbstractMqttSubscription {

    private EdmEntityType sourceEntityType;

    private Long sourceId;

    private EdmEntitySet targetEntitySet;

    public MqttEntityCollectionSubscription(String topic, QueryOptions queryOption,
            EdmEntityType sourceEntityType, Long sourceId, EdmEntitySet targetEntitySet,
            EdmEntityType entityType) {
        super(topic, queryOption, entityType, targetEntitySet);
        this.sourceEntityType = sourceEntityType;
        this.sourceId = sourceId;
        this.targetEntitySet = targetEntitySet;
        
    }

    @Override
    public boolean matches(Entity entity, Map<String, Set<Long>> collections, Set<String> differenceMap) {
        // Check type and fail-fast on type mismatch
        if (!(entity.getType().equals(getEdmEntityType().getName()))) {
            return false;
        }
        
        // Check if Subscription is on root level (e.g. `/Things`)
        // Type was already checked so we can success-fast
        if (sourceId == null) {
            return true;
        }
        
        // Check if Entity belongs to collection of this Subscription
        if (collections != null) {
            for (Entry<String, Set<Long>> collection : collections.entrySet()) {
                //TODO: check if this is ET_THING_NAME etc.
                String test= targetEntitySet.getName();
                
                if (collection.getKey().equals(targetEntitySet.getName())) {
                    for (Long id : collection.getValue()) {
                        if (id.equals(sourceId)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }
}
