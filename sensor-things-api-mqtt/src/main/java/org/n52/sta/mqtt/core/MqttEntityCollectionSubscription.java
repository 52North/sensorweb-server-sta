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
public class MqttEntityCollectionSubscription extends AbstractMqttSubscription {

    private EdmEntityType sourceEntityType;

    private Long sourceId;

    private EdmEntitySet targetEntitySet;

    public MqttEntityCollectionSubscription(String topic, QueryOptions queryOption,
            EdmEntityType sourceEntityType, Long sourceId, EdmEntitySet targetEntitySet,
            EdmEntityType entityType) {
        super(topic, queryOption, entityType);
        this.sourceEntityType = sourceEntityType;
        this.sourceId = sourceId;
        this.targetEntitySet = targetEntitySet;
    }

    @Override
    public boolean matches(Entity entity, Map<String, Long> collections, Set<String> differenceMap) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.  
    }

}
