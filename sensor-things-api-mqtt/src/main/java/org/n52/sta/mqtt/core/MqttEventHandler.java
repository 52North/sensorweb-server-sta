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
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.n52.sta.data.STAEventHandler;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.mqtt.MqttHandlerException;
import org.n52.sta.mqtt.handler.PayloadSerializer;
import org.n52.sta.mqtt.request.SensorThingsMqttRequest;
import org.n52.sta.service.handler.AbstractEntityCollectionRequestHandler;
import org.n52.sta.service.handler.AbstractEntityRequestHandler;
import org.n52.sta.service.handler.AbstractPropertyRequestHandler;
import org.n52.sta.service.query.URIQueryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@Component
public class MqttEventHandler implements STAEventHandler, InitializingBean {

    public static final String INTERNAL_CLIENT_ID = "POC";

    private static final String BASE_URL = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttEventHandler.class);
    private final MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(
            MqttMessageType.PUBLISH,
            false,
            MqttQoS.AT_LEAST_ONCE,
            false,
            0);

    private final MqttUtil config;
    private final Parser parser;
    private final PayloadSerializer serializer;
    private final CsdlAbstractEdmProvider provider;
    private final AbstractEntityCollectionRequestHandler<SensorThingsMqttRequest, MqttEntityCollectionSubscription>
            entityCollectionRequestHandler;
    private final AbstractEntityRequestHandler<SensorThingsMqttRequest, MqttEntitySubscription>
            mqttEntitySubscHandler;
    private final AbstractPropertyRequestHandler<SensorThingsMqttRequest, MqttPropertySubscription>
            mqttPropertySubscHandler;
    private final EntityServiceRepository serviceRepository;
    private Server mqttBroker;
    private Map<AbstractMqttSubscription, HashSet<String>> subscriptions = new HashMap<>();
    private ServiceMetadata edm;
    /*
     * List of all Entity Types that are currently subscribed to. Used for fail-fast.
     */
    private Set<String> watchedEntityTypes = new HashSet<String>();

    public MqttEventHandler(
            MqttUtil config,
            Parser parser,
            PayloadSerializer serializer,
            CsdlAbstractEdmProvider provider,
            EntityServiceRepository serviceRepository,
            AbstractEntityCollectionRequestHandler<SensorThingsMqttRequest, MqttEntityCollectionSubscription>
                    entityCollectionRequestHandler,
            AbstractEntityRequestHandler<SensorThingsMqttRequest, MqttEntitySubscription>
                    mqttEntitySubscHandler,
            AbstractPropertyRequestHandler<SensorThingsMqttRequest, MqttPropertySubscription>
                    mqttPropertySubscHandler) {
        this.config = config;
        this.parser = parser;
        this.serializer = serializer;
        this.provider = provider;
        this.serviceRepository = serviceRepository;
        this.entityCollectionRequestHandler = entityCollectionRequestHandler;
        this.mqttEntitySubscHandler = mqttEntitySubscHandler;
        this.mqttPropertySubscHandler = mqttPropertySubscHandler;
    }


    @Override
    public void afterPropertiesSet() {
        OData odata = OData.newInstance();
        edm = odata.createServiceMetadata(provider, new ArrayList<>());
    }

    public Set<String> getWatchedEntityTypes() {
        return watchedEntityTypes;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void handleEvent(Object rawObject, Set<String> differenceMap) {
        Entity entity;
        Map<String, Set<String>> collections = null;

        //Map beans Object into Olingo Entity
        String entityClass = rawObject.getClass().getName();
        entity = config.getMapper(entityClass).createEntity(rawObject);

        // Check all subscriptions for a match
        ByteBuf serializedEntity = null;
        for (AbstractMqttSubscription subscrip : subscriptions.keySet()) {
            if (subscrip instanceof MqttEntityCollectionSubscription) {
                AbstractSensorThingsEntityService<?, ?> responseService
                        = serviceRepository.getEntityService(
                        MqttUtil.TYPEMAP.get(entityClass));

                collections = responseService.getRelatedCollections(rawObject);
            }
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
                    mqttBroker.internalPublish(msg, INTERNAL_CLIENT_ID);
                    LOGGER.debug("Posted Message to Topic: {}", topic);
                } catch (IOException | SerializerException ex) {
                    LOGGER.error("Error while serializing payload: {}", ex.getMessage());
                    LOGGER.debug("Error while serializing payload: {} {}", ex.getClass().getName(), ex.getMessage());
                }
            }
        }
    }

    public void addSubscription(AbstractMqttSubscription subscription, String clientId) {
        HashSet<String> clients = subscriptions.get(subscription);
        if (clients == null) {
            clients = new HashSet<String>();
        }
        watchedEntityTypes.add(MqttUtil.TYPEMAP.get(subscription.getEdmEntityType().getName()));
        clients.add(clientId);
        subscriptions.put(subscription, clients);
    }

    public void removeSubscription(AbstractMqttSubscription subscription, String clientId) {
        HashSet<String> clients = subscriptions.get(subscription);
        if (clients != null) {
            if (clients.size() == 1) {
                subscriptions.remove(subscription);
                watchedEntityTypes.remove(MqttUtil.TYPEMAP.get(subscription.getEdmEntityType().getName()));
            } else {
                clients.remove(clientId);
                subscriptions.put(subscription, clients);
            }
        }
    }

    private ByteBuf encodeEntity(AbstractMqttSubscription subsc,
                                 Entity entity) throws IOException, SerializerException {
        return serializer.encodeEntity(
                entity,
                subsc.getEdmEntityType(),
                subsc.getEdmEntitySet(),
                subsc.getSelectOption());
    }


    public void processSubscribeMessage(InterceptSubscribeMessage msg) throws MqttHandlerException {
        addSubscription(createMqttSubscription(msg.getTopicFilter()), msg.getClientID());
    }

    public void processUnsubscribeMessage(InterceptUnsubscribeMessage msg) throws MqttHandlerException {
        removeSubscription(createMqttSubscription(msg.getTopicFilter()), msg.getClientID());
    }

    private AbstractMqttSubscription createMqttSubscription(String topic) throws MqttHandlerException {
        AbstractMqttSubscription subscription = validateTopicPattern(topic, "");
        return subscription;
    }

    private AbstractMqttSubscription validateTopicPattern(String topic, String baseUri) throws MqttHandlerException {
        try {
            // Validate that Topic is valid URI
            UriInfo uriInfo = parser.parseUri(topic, null, null, baseUri);
            AbstractMqttSubscription subscription = null;
            switch (uriInfo.getKind()) {
                case resource:
                case entityId:
                    subscription = validateResource(topic, uriInfo);
                    break;
                default:
                    throw new MqttHandlerException("Unsupported MQTT topic.");
            }
            return subscription;
        } catch (UriParserException | UriValidationException ex) {
            throw new MqttHandlerException("Error while parsing MQTT topic.", ex);
        }

    }

    private AbstractMqttSubscription validateResource(String topic, UriInfo uriInfo) throws MqttHandlerException {
        try {
            final int lastPathSegmentIndex = uriInfo.getUriResourceParts().size() - 1;
            final UriResource lastPathSegment = uriInfo.getUriResourceParts().get(lastPathSegmentIndex);
            AbstractMqttSubscription subscription = null;

            switch (lastPathSegment.getKind()) {

                case entitySet:
                case navigationProperty:
                    if (((UriResourcePartTyped) lastPathSegment).isCollection()) {
                        subscription = entityCollectionRequestHandler
                                .handleEntityCollectionRequest(new SensorThingsMqttRequest(topic,
                                        uriInfo.getUriResourceParts(),
                                        new URIQueryOptions(uriInfo, BASE_URL)));
                    } else {
                        subscription = mqttEntitySubscHandler
                                .handleEntityRequest(new SensorThingsMqttRequest(topic,
                                        uriInfo.getUriResourceParts(),
                                        new URIQueryOptions(uriInfo, BASE_URL)));
                    }
                    break;

                case primitiveProperty:
                case complexProperty:
                    subscription = mqttPropertySubscHandler
                            .handlePropertyRequest(new SensorThingsMqttRequest(topic,
                                    uriInfo.getUriResourceParts(),
                                    new URIQueryOptions(uriInfo, BASE_URL)));
                    break;

                default:
                    throw new MqttHandlerException("Unsupported MQTT topic pattern.");
            }
            return subscription;
        } catch (ODataApplicationException ex) {
            throw new MqttHandlerException("Error while resolving MQTT subscription topic.", ex);
        }
    }

    public void setMqttBroker(Server mqttBroker) {
        this.mqttBroker = mqttBroker;
    }
}
