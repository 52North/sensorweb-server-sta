/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.moquette.broker.Server;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.n52.shetland.oasis.odata.query.option.QueryOptions;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.mqtt.MqttHandlerException;
import org.n52.sta.mqtt.core.subscription.AbstractMqttSubscription;
import org.n52.sta.mqtt.core.subscription.MqttEntityCollectionSubscription;
import org.n52.sta.mqtt.core.subscription.MqttEntitySubscription;
import org.n52.sta.mqtt.core.subscription.MqttPropertySubscription;
import org.n52.sta.mqtt.core.subscription.MqttSelectSubscription;
import org.n52.sta.serdes.util.ElementWithQueryOptions;
import org.n52.sta.utils.AbstractSTARequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class MqttSubscriptionEventHandlerImpl extends AbstractSTARequestHandler
        implements MqttSubscriptionEventHandler {

    private static final String BASE_URL = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSubscriptionEventHandlerImpl.class);
    private final MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
            MqttMessageType.PUBLISH,
            false,
            MqttQoS.AT_LEAST_ONCE,
            false,
            0);

    private final MqttUtil config;
    private EntityServiceRepository serviceRepository;
    private Server mqttBroker;
    private Map<AbstractMqttSubscription, HashSet<String>> subscriptions = new HashMap<>();

    private final ObjectMapper mapper;

    /*
     * List of all Entity Types that are currently subscribed to. Used for fail-fast.
     */
    private Set<String> watchedEntityTypes = new HashSet<>();

    public MqttSubscriptionEventHandlerImpl(@Value("${server.rootUrl}") String rootUrl,
                                            @Value("${server.feature.escapeId}") boolean shouldEscapeId,
                                            MqttUtil config,
                                            ObjectMapper mapper) {
        super(rootUrl, shouldEscapeId, null);
        this.config = config;
        this.mapper = mapper;
    }

    @Override
    public Set<String> getWatchedEntityTypes() {
        return watchedEntityTypes;
    }

    @Override
    public void setServiceRepository(EntityServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    @Async
    public void handleEvent(Object rawObject,
                            String entityType,
                            Set<String> differenceMap,
                            Map<String, Set<String>> collections) {
        try {
            // Invariant: As watchedEntityTypes contains rawObject->class
            // there is at least one subscription that matches.

            // Store serialized Versions for reusing while processing other subscriptions.
            // Multiple serializations may be necessary due to different select clauses.
            Map<QueryOptions, ByteBuf> serializedCache = new HashMap<>();

            // Check all subscriptions for a match
            for (AbstractMqttSubscription subscrip : subscriptions.keySet()) {

                String topic = subscrip.checkSubscription(rawObject, entityType, collections, differenceMap);

                if (topic != null) {
                    // Use cache if applicable
                    ByteBuf out;
                    if (serializedCache.containsKey(subscrip.getQueryOptions())) {
                        out = serializedCache.get(subscrip.getQueryOptions());
                    } else {
                        ElementWithQueryOptions wrapped =
                                ElementWithQueryOptions.from(rawObject, subscrip.getQueryOptions());
                        out = Unpooled.wrappedBuffer(mapper.writeValueAsBytes(wrapped));
                        serializedCache.put(subscrip.getQueryOptions(), out);
                    }
                    MqttPublishMessage msg = new MqttPublishMessage(mqttFixedHeader,
                                                                    new MqttPublishVariableHeader(topic, 52),
                                                                    out);
                    mqttBroker.internalPublish(msg, INTERNAL_CLIENT_ID);
                    LOGGER.debug("Posted Message to Topic: {}", topic);
                } else {
                    LOGGER.debug("Subscription does not match!");
                }
            }
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    public void addSubscription(AbstractMqttSubscription subscription, String clientId) {
        HashSet<String> clients = subscriptions.get(subscription);
        if (clients == null) {
            clients = new HashSet<>();
        }
        LOGGER.debug("Adding to watched EntityType: " + subscription.getEntityType());
        watchedEntityTypes.add(subscription.getEntityType());
        clients.add(clientId);
        subscriptions.put(subscription, clients);
    }

    public void removeSubscription(AbstractMqttSubscription subscription, String clientId) {
        HashSet<String> clients = subscriptions.get(subscription);
        if (clients != null) {
            if (clients.size() == 1) {
                subscriptions.remove(subscription);
                watchedEntityTypes.remove(subscription.getEntityType());
            } else {
                clients.remove(clientId);
                subscriptions.put(subscription, clients);
            }
        }
    }

    @Override public void processSubscribeMessage(InterceptSubscribeMessage msg) throws MqttHandlerException {
        addSubscription(createMqttSubscription(msg.getTopicFilter()), msg.getClientID());
    }

    @Override public void processUnsubscribeMessage(InterceptUnsubscribeMessage msg) throws MqttHandlerException {
        removeSubscription(createMqttSubscription(msg.getTopicFilter()), msg.getClientID());
    }

    private AbstractMqttSubscription createMqttSubscription(String rawTopic) throws MqttHandlerException {
        try {
            Matcher mt;
            // Delete possible leading slash
            String topic = (rawTopic.startsWith("/")) ? rawTopic.substring(1) : rawTopic;

            // Check topic for syntax+semantics
            if (topic.contains("?")) {
                // only check path part of the topic (excluding the select parameter)
                validateResource(topic.substring(0, topic.indexOf("?")), serviceRepository);
                for (Pattern namedSelectPattern : NAMED_SELECT_PATTERNS) {
                    mt = namedSelectPattern.matcher(topic);
                    if (mt.matches()) {
                        // OGC-15-078r6 14.2.4
                        return new MqttSelectSubscription(topic, mt);
                    }
                }
            } else {
                // check full topic
                // This will fail if we have a PropertySubscription
                try {
                    validateResource(topic, serviceRepository);
                    for (Pattern collectionPattern : NAMED_COLL_PATTERNS) {
                        mt = collectionPattern.matcher(topic);
                        if (mt.matches()) {
                            // OGC-15-078r6 14.2.1
                            return new MqttEntityCollectionSubscription(topic, mt);
                        }
                    }

                    for (Pattern namedEntityPattern : NAMED_ENTITY_PATTERNS) {
                        mt = namedEntityPattern.matcher(topic);
                        if (mt.matches()) {
                            // OGC-15-078r6 14.2.2
                            return new MqttEntitySubscription(topic, mt);
                        }
                    }
                } catch (Exception ex) {
                    for (Pattern namedPropertyPattern : NAMED_PROP_PATTERNS) {
                        mt = namedPropertyPattern.matcher(topic);
                        if (mt.matches()) {
                            // OGC-15-078r6 14.2.3
                            // Only check path part of the topic (excluding the property)
                            String path = topic.substring(0, topic.lastIndexOf("/"));
                            validateResource(path, serviceRepository);
                            return new MqttPropertySubscription(topic, mt);
                        }
                    }
                }

            }

            throw new MqttHandlerException("Error while parsing MQTT topic. Could not identify subscription type!");
        } catch (Exception ex) {
            throw new MqttHandlerException("Error while parsing MQTT topic.", ex);
        }
    }

    @Override public void setMqttBroker(Server mqttBroker) {
        this.mqttBroker = mqttBroker;
    }
}
