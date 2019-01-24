/*
 * Copyright (C) 2012-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.mqtt.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.n52.sta.data.STAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.moquette.broker.Server;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class MQTTEventHandler implements STAEventHandler {
    
    @Autowired
    private MQTTUtil config;
    
    @Autowired
    private Server mqttBroker;
    
    static final String internalClientId = "POC";

    private Map<MQTTSubscription, Integer> subscriptions = new HashMap<MQTTSubscription, Integer>();
    
    private final MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE, false, 0);

    /*
     * List of all Entity Types that are currently subscribed to. Used for fail-fast.
     */
    private Set<String> watchedEntityTypes = new HashSet<String>();

    final Logger LOGGER = LoggerFactory.getLogger(MQTTEventHandler.class);

    @SuppressWarnings({"unchecked"})
    @Override
    public void handleEvent(Object rawObject, Set<String> differenceMap) {
        Entity entity;

        //Map beans Object into Olingo Entity
        //Fail-fast if nobody is subscribed to this Type
        if (!watchedEntityTypes.contains(rawObject.getClass().getName())) {
            return;
        } else {
            entity = config.getMapper(rawObject.getClass().getName()).createEntity(rawObject);
        }
        
        // Check all subscriptions for a match
        subscriptions.forEach((subscrip, count) -> {
            String topic = subscrip.checkSubscription(entity, differenceMap);
            if (topic != null) {
                MqttPublishMessage msg = new MqttPublishMessage(mqttFixedHeader,
                                                                new MqttPublishVariableHeader(topic, 52),
                                                                subscrip.encodeEntity(entity));
                mqttBroker.internalPublish(msg, internalClientId);
                LOGGER.debug("Posted Message to Topic: " + topic);
            }
        });
    }

    public void addSubscription(MQTTSubscription subscription) {
        Integer count = subscriptions.get(subscription);
        if (count != null) {
            subscriptions.put(subscription, count++);
        } else {
            subscriptions.put(subscription, 1);
            watchedEntityTypes.add(MQTTUtil.getBeanTypes().get(subscription.getEntityType()));
        }
    }

    public void removeSubscription(MQTTSubscription subscription) {
        Integer count = subscriptions.get(subscription);
        if (count != null) {
            if (count == 1) {
                subscriptions.remove(subscription);
                watchedEntityTypes.remove(MQTTUtil.getBeanTypes().get(subscription.getEntityType()));
            } else {
                subscriptions.put(subscription, --count);
            }
        }
    }
    

}
