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
import org.n52.sta.mqtt.config.MQTTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
@IntegrationComponentScan
public class MQTTLocalClient implements STAEventHandler {

    @Autowired
    private IntegrationFlow gate;

    @Autowired
    private MQTTConfiguration config;

    @Value("${mqtt.localClient.maxWaitTime:1}")
    private int maxWaitTime;

    private Map<MQTTSubscription, Integer> subscriptions = new HashMap<MQTTSubscription, Integer>();

    /*
     * List of all Entity Types that are currently subscribed to. Used for fail-fast.
     */
    private Set<String> watchedEntityTypes = new HashSet<String>();

    final Logger LOGGER = LoggerFactory.getLogger(MQTTLocalClient.class);

    @SuppressWarnings({"serial", "unchecked"})
    @Override
    public void handleEvent(Object rawObject) {
        Entity entity;

        //Map beans Object into Olingo Entity
        //Fail-fast if nobody is subscribed to this Type
        if (!watchedEntityTypes.contains(rawObject.getClass().getName())) {
            return;
        } else {
            entity = config.getMapper(rawObject.getClass().getName()).createEntity(rawObject);
        }

        MessageChannel channel = gate.getInputChannel();
        // Check all subscriptions for a match
        subscriptions.forEach((subscrib, count) -> {
            String topic = subscrib.checkSubscription(entity);
            if (topic != null) {

                //TODO: Actually serialize Object to JSON
                String message = entity.toString();

                Message<String> msg = new GenericMessage<String>(message, new HashMap<String, Object>() {{put("mqtt_topic", topic);}});
                channel.send(msg, maxWaitTime);
                LOGGER.debug("Posted Message: " + message + " to Topic: " +topic);
            }
        });
    }

    public void addSubscription(MQTTSubscription subscription) {
        Integer count = subscriptions.get(subscription);
        if (count != null) {
            subscriptions.put(subscription, count++);
        } else {
            subscriptions.put(subscription, 1);
            watchedEntityTypes.add(MQTTConfiguration.getBeanTypes().get(subscription.getEntityType()));
        }
    }
    
    public void removeSubscription(MQTTSubscription subscription) {
        Integer count = subscriptions.get(subscription);
        if (count != null) {
            if (count == 1) {
                subscriptions.remove(subscription);
                watchedEntityTypes.remove(MQTTConfiguration.getBeanTypes().get(subscription.getEntityType()));
            } else {
                subscriptions.put(subscription, count--);
            }
        }
    }
}
