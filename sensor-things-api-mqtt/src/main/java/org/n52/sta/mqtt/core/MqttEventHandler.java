/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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

import io.moquette.broker.Server;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.*;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.n52.sta.data.STAEventHandler;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.mqtt.handler.PayloadSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class MqttEventHandler implements STAEventHandler, InitializingBean {

    static final String internalClientId = "POC";
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttEventHandler.class);
    private final MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE, false, 0);

    @Autowired
    private MqttUtil config;

    @Autowired
    private Server mqttBroker;

    @Autowired
    private PayloadSerializer serializer;

    @Autowired
    private CsdlAbstractEdmProvider provider;

    @Autowired
    private EntityServiceRepository serviceRepository;
    private Map<AbstractMqttSubscription, HashSet<String>> subscriptions = new HashMap<AbstractMqttSubscription, HashSet<String>>();
    private ServiceMetadata edm;
    /*
     * List of all Entity Types that are currently subscribed to. Used for fail-fast.
     */
    private Set<String> watchedEntityTypes = new HashSet<String>();

    public Set<String> getWatchedEntityTypes() {
        return watchedEntityTypes;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void handleEvent(Object rawObject, Set<String> differenceMap) {
        Entity entity;
        Map<String, Set<String>> collections;

        //Map beans Object into Olingo Entity
        String entityClass = rawObject.getClass().getName();
        entity = config.getMapper(entityClass).createEntity(rawObject);
        AbstractSensorThingsEntityService<?, ?> responseService
                = serviceRepository.getEntityService(
                MqttUtil.typeMap.get(entityClass));

        collections = responseService.getRelatedCollections(rawObject);

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
        }
        ;
    }

    public void addSubscription(AbstractMqttSubscription subscription, String clientId) {
        HashSet<String> clients = subscriptions.get(subscription);
        if (clients == null) {
            clients = new HashSet<String>();
        }
        watchedEntityTypes.add(MqttUtil.typeMap.get(subscription.getEdmEntityType().getName()));
        clients.add(clientId);
        subscriptions.put(subscription, clients);
    }

    public void removeSubscription(AbstractMqttSubscription subscription, String clientId) {
        HashSet<String> clients = subscriptions.get(subscription);
        if (clients != null) {
            if (clients.size() == 1) {
                subscriptions.remove(subscription);
                watchedEntityTypes.remove(MqttUtil.typeMap.get(subscription.getEdmEntityType().getName()));
            } else {
                clients.remove(clientId);
                subscriptions.put(subscription, clients);
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
