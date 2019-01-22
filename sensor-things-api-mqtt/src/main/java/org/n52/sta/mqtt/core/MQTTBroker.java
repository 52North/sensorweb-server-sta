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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.n52.sta.mqtt.config.MQTTConfiguration;
import org.n52.sta.mqtt.handler.MqttObservationCreateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class MQTTBroker {

    final Logger LOGGER = LoggerFactory.getLogger(MQTTBroker.class);

    @Value("${mqtt.broker.persistence.path}")
    private String MOQUETTE_STORE_PATH;
    
    @Value("${mqtt.broker.persistence.filename:52N-STA-MQTTBroker.h2}")
    private String MOQUETTE_STORE_FILENAME;
    
    @Value("${mqtt.broker.persistence.autosave_interval:300}")
    private String AUTOSAVE_INTERVAL_PROPERTY;
    
    @Autowired
    private MqttObservationCreateHandler handler;
    
    @Autowired
    private MQTTEventHandler localClient;
    
    @Autowired
    private Parser parser;

    @Bean
    public Server initMQTTBroker() {
        Server test = new Server();
        try {
            test.startServer(parseConfig(), Arrays.asList(initMessageHandler()));
            Runtime.getRuntime().addShutdownHook(new Thread(test::stopServer));
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return test;
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
                if (!msg.getClientID().equals(MQTTConfiguration.internalClientId)) {
                    LOGGER.debug(msg.getTopicName());
                    LOGGER.debug(msg.getPayload().toString(Charset.forName("UTF-8")));
                    try {
                        handler.processMessage(msg);
                    } catch (UriParserException | UriValidationException
                            | ODataApplicationException | DeserializerException ex) {
                        LOGGER.error("Error while processing MQTT message");
                        LOGGER.debug("Error while processing MQTT message", ex);
                    }
                }
            }

            @Override
            public void onSubscribe(InterceptSubscribeMessage msg) {
                LOGGER.debug("Client with ID: " + msg.getClientID() + "has subscribed");
                try {
                    LOGGER.debug("Adding new MQTT subscription");
                    localClient.addSubscription(new MQTTSubscription(msg.getTopicFilter(), parser));
                } catch (Exception e) {
                    LOGGER.error("Error while processing MQTT subscription");
                    LOGGER.debug("Error while processing MQTT subscription", e);
                }
            }

            @Override
            public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
                LOGGER.debug("Client with ID: " + msg.getClientID() + "has UNsubscribed");
                try {
                    LOGGER.debug("Adding new MQTT subscription");
                    localClient.removeSubscription(new MQTTSubscription(msg.getTopicFilter(), parser));
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
        // Fallback to default path if not set
        if (MOQUETTE_STORE_PATH.equals("")) {
            MOQUETTE_STORE_PATH = System.getProperty("user.dir") + File.separator + MOQUETTE_STORE_FILENAME;
        }
        LOGGER.info("Initialized MQTT Broker Persistence with Path: " + MOQUETTE_STORE_PATH);
        LOGGER.info("Initialized MQTT Broker Persistence with Filename: " + MOQUETTE_STORE_FILENAME);
        LOGGER.info("Initialized MQTT Broker Persistence with Autosave Interval: " + AUTOSAVE_INTERVAL_PROPERTY);
        props.put(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, MOQUETTE_STORE_PATH);
        props.put(BrokerConstants.DEFAULT_MOQUETTE_STORE_H2_DB_FILENAME, MOQUETTE_STORE_FILENAME);
        props.put(BrokerConstants.AUTOSAVE_INTERVAL_PROPERTY_NAME, AUTOSAVE_INTERVAL_PROPERTY);
        props.put(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());
        return new MemoryConfig(props);
    }

}
