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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.n52.sta.data.STAEventHandler;
import org.n52.sta.service.serializer.PayloadSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.moquette.broker.Server;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.util.ArrayList;
import org.apache.olingo.commons.api.data.Linked;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class MQTTEventHandler implements STAEventHandler, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(MQTTEventHandler.class);

    @Autowired
    private MQTTUtil config;

    @Autowired
    private Server mqttBroker;

    @Autowired
    private PayloadSerializer serializer;

    @Autowired
    private CsdlAbstractEdmProvider provider;

    static final String internalClientId = "POC";

    private Map<AbstractMqttSubscription, Integer> subscriptions = new HashMap<AbstractMqttSubscription, Integer>();

    private final MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE, false, 0);

    private ServiceMetadata edm;

    /*
     * List of all Entity Types that are currently subscribed to. Used for fail-fast.
     */
    private Set<String> watchedEntityTypes = new HashSet<String>();

    @SuppressWarnings({"unchecked"})
    @Override
    public void handleEvent(Object rawObject, Set<String> differenceMap) {
        Entity entity;
        Map<String, Long> collections ;

        //Map beans Object into Olingo Entity
        //Fail-fast if nobody is subscribed to this Type
        if (!watchedEntityTypes.contains(rawObject.getClass().getName())) {
            return;
        } else {
            entity = config.getMapper(rawObject.getClass().getName()).createEntity(rawObject);
            collections = config.getMapper(rawObject.getClass().getName()).getRelatedCollections(rawObject);
        }

        // Check all subscriptions for a match
        ByteBuf serializedEntity = null;
        for (AbstractMqttSubscription subscrip : subscriptions.keySet()) {
            String topic = subscrip.checkSubscription(entity, collections, differenceMap);
            if (topic != null) {
                try {
                    // Cache Entity for other matching Topics
                    if (serializedEntity == null) {
                        serializedEntity = encodeEntity(subscrip, entity);
                    }
                    MqttPublishMessage msg = new MqttPublishMessage(mqttFixedHeader,
                            new MqttPublishVariableHeader(topic, 52),
                            serializedEntity);
                    mqttBroker.internalPublish(msg, internalClientId);
                    LOGGER.debug("Posted Message to Topic: " + topic);
                } catch (IOException | SerializerException ex) {
                    LOGGER.error(ex.getMessage());
                    LOGGER.debug("Error while serializing payload.", ex);
                }
            }
        };
    }

    public void addSubscription(AbstractMqttSubscription subscription) {
        Integer count = subscriptions.get(subscription);
        if (count != null) {
            subscriptions.put(subscription, count++);
        } else {
            subscriptions.put(subscription, 1);
            watchedEntityTypes.add(MQTTUtil.getBeanTypes().get(subscription.getEntityType()));
        }
    }

    public void removeSubscription(AbstractMqttSubscription subscription) {
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

    private ByteBuf encodeEntity(AbstractMqttSubscription subsc, Entity entity) throws IOException, SerializerException {
        return serializer.encodeEntity(edm, entity, subsc.getEdmEntityType(), subsc.getEdmEntitySet(), subsc.getQueryOptions(), watchedEntityTypes);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        OData odata = OData.newInstance();
        edm = odata.createServiceMetadata(provider, new ArrayList<EdmxReference>());
    }

}
