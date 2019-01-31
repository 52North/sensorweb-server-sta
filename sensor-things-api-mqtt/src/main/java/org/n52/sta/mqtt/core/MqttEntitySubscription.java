/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sta.mqtt.core;

import java.util.Map;
import java.util.Set;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.n52.sta.service.query.QueryOptions;

/**
 *
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
public class MqttEntitySubscription extends AbstractMqttSubscription {

    private EdmEntitySet entitySet;

    private Long entityId;

    public MqttEntitySubscription(EdmEntityType sourceEntityType, Long sourceId, EdmEntitySet targetEntitySet, EdmEntityType entityType, Long targetId, String topic, QueryOptions queryOptions) {
        super(topic, queryOptions, entityType, targetEntitySet);
        this.entitySet = targetEntitySet;
        this.entityId = targetId;
    }

    @Override
    public boolean matches(Entity entity, Map<String, Set<Long>> collections, Set<String> differenceMap) {
        // Check type and fail-fast on type mismatch
        if (!(entity.getType().equals(getEdmEntityType().getName()))) {
            return false;
        }

        // Check ID (if not collection) and fail-fast if wrong id is present
        if (!entityId.equals(entity.getProperty("id").getValue())) {
            return false;
        }

        return true;
    }

}
