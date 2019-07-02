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

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.subscriptions.Subscription;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.*;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.h2.mvstore.Cursor;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.n52.sta.mqtt.handler.MqttMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class MqttBroker {

    final Logger LOGGER = LoggerFactory.getLogger(MqttBroker.class);

    @Value("${mqtt.broker.persistence.path:}")
    private String MOQUETTE_STORE_PATH;

    @Value("${mqtt.broker.persistence.filename:52N-STA-MQTTBroker.h2}")
    private String MOQUETTE_STORE_FILENAME;

    @Value("${mqtt.broker.persistence.autosave_interval:300}")
    private String AUTOSAVE_INTERVAL_PROPERTY;

    @Value("${mqtt.broker.persistence.enabled}")
    private Boolean MOQUETTE_PERSISTENCE_ENABLED;

    @Value("${mqtt.broker.websocket.enabled}")
    private Boolean MOQUETTE_WEBSOCKET_ENABLED;

    @Value("${mqtt.broker.websocket.port:8080}")
    private String MOQUETTE_WEBSOCKET_PORT;

    @Autowired
    private MqttMessageHandler handler;

    private IConfig brokerConfig;

    private Server mqttServer;

    @Bean(destroyMethod = "stopServer")
    public Server initMQTTBroker() {
        mqttServer = new Server();
        brokerConfig = parseConfig();
        return mqttServer;
    }

    @EventListener({ContextRefreshedEvent.class})
    private void restoreSubscriptionsAndStartServer() {
        if (MOQUETTE_PERSISTENCE_ENABLED) {
            MVStore mvStore = new MVStore.Builder()
                    .fileName(brokerConfig.getProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME))
                    .autoCommitDisabled()
                    .open();
            MVMap<Object, Object> subscriptions = mvStore.openMap("subscriptions");
            Cursor<Object, Object> subscriptionsCursor = subscriptions.cursor(null);
            while (subscriptionsCursor.hasNext()) {
                try {
                    subscriptionsCursor.next();
                    Subscription sub = (Subscription) subscriptionsCursor.getValue();
                    handler.processSubscribeMessage(new InterceptSubscribeMessage(sub, sub.getClientId()));
                    LOGGER.info("Restored Subscription of client:'" + sub.getClientId() + "' to topic: '" + sub.getTopicFilter().toString() + "'");
                } catch (Exception e) {
                    subscriptions.remove(subscriptionsCursor.getKey());
                    LOGGER.error("Error while restoring MQTT subscription. Invalid Subscription: " + subscriptionsCursor.getValue() + " was removed from storage.");
                    LOGGER.debug("Error while restoring MQTT subscription", e);
                }
            }
            mvStore.close();
        }

        try {
            mqttServer.startServer(brokerConfig, Arrays.asList(initMessageHandler()));
            Runtime.getRuntime().addShutdownHook(new Thread(mqttServer::stopServer));
        } catch (IOException e) {
            LOGGER.error("Error starting/stopping MQTT Broker. Exception: " + e.getMessage());
            LOGGER.debug("Error starting/stopping MQTT Broker.", e);
        }
    }

    private InterceptHandler initMessageHandler() {
        InterceptHandler messageHandler = new AbstractInterceptHandler() {

            @Override
            public String getID() {
                return "52N-STA-MQTTBroker";
            }

            @Override
            public void onConnect(InterceptConnectMessage msg) {
                LOGGER.debug("Client with ID: " + msg.getClientID() + "has connected");
            }

            @Override
            public void onDisconnect(InterceptDisconnectMessage msg) {
                LOGGER.debug("Client with ID: " + msg.getClientID() + "has disconnected");
            }

            @Override
            public void onConnectionLost(InterceptConnectionLostMessage msg) {
                LOGGER.debug("Client with ID: " + msg.getClientID() + "has lost connection");
            }

            @Override
            public void onPublish(InterceptPublishMessage msg) {
                if (!msg.getClientID().equals(MqttEventHandler.internalClientId)) {
                    LOGGER.debug(msg.getTopicName());
                    LOGGER.debug(msg.getPayload().toString(Charset.forName("UTF-8")));
                    try {
                        handler.processPublishMessage(msg);
                    } catch (UriParserException | UriValidationException
                            | ODataApplicationException | DeserializerException ex) {
                        LOGGER.error("Error while processing MQTT message");
                        LOGGER.debug("Error while processing MQTT message", ex.getMessage());
                    }
                }
            }

            @Override
            public void onSubscribe(InterceptSubscribeMessage msg) {
                LOGGER.debug("Client with ID: " + msg.getClientID() + "has subscribed");
                try {
                    LOGGER.debug("Adding new MQTT subscription");
                    handler.processSubscribeMessage(msg);
                } catch (Exception e) {
                    LOGGER.error("Error while processing MQTT subscription");
                    LOGGER.debug("Error while processing MQTT subscription", e.getMessage());
                }
            }

            @Override
            public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
                LOGGER.debug("Client with ID: " + msg.getClientID() + "has UNsubscribed");
                try {
                    LOGGER.debug("Adding new MQTT subscription");
                    handler.processUnsubscribeMessage(msg);
                } catch (Exception e) {
                    LOGGER.error("Error while processing MQTT subscription");
                    LOGGER.debug("Error while processing MQTT subscription", e);
                }
            }

        };
        return messageHandler;
    }

    private IConfig parseConfig() {
        Properties props = new Properties();

        if (MOQUETTE_PERSISTENCE_ENABLED) {
            // Fallback to default path if not set
            if (MOQUETTE_STORE_PATH.equals("")) {
                MOQUETTE_STORE_PATH = System.getProperty("user.dir") + File.separator + MOQUETTE_STORE_FILENAME;
            }
            LOGGER.info("Initialized MQTT Broker Persistence with Path: " + MOQUETTE_STORE_PATH);
            LOGGER.info("Initialized MQTT Broker Persistence with Filename: " + MOQUETTE_STORE_FILENAME);
            props.put(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, MOQUETTE_STORE_PATH);
            props.put(BrokerConstants.DEFAULT_MOQUETTE_STORE_H2_DB_FILENAME, MOQUETTE_STORE_FILENAME);

            LOGGER.info("Initialized MQTT Broker Persistence with Autosave Interval: " + AUTOSAVE_INTERVAL_PROPERTY);
            props.put(BrokerConstants.AUTOSAVE_INTERVAL_PROPERTY_NAME, AUTOSAVE_INTERVAL_PROPERTY);
        } else {
            // In-Memory Subscription Store
            props.put(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, "");
        }

        if (MOQUETTE_WEBSOCKET_ENABLED) {
            props.put(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, MOQUETTE_WEBSOCKET_PORT);
        }

        props.put(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());
        return new MemoryConfig(props);
    }

}
