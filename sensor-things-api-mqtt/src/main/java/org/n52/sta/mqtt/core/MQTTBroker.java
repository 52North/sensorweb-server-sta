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
import java.util.Arrays;
import java.util.Properties;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataRequest;
import org.n52.sta.service.deserializer.SensorThingsDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptAcknowledgedMessage;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.netty.buffer.ByteBufInputStream;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class MQTTBroker {
    
    final Logger LOGGER = LoggerFactory.getLogger(MQTTBroker.class);
    
    @Bean
    public Server initMQTTBroker() {
        Server test = new Server();
        try {
            test.startServer(parseConfig(), Arrays.asList(initMessageHandler()));
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
                //TODO(specki): Check if this has any effect at all
                return "52N";
            }
            
            @Override
            public void onConnect(InterceptConnectMessage msg) {
                LOGGER.debug(msg.getClientID());
            }

            @Override
            public void onDisconnect(InterceptDisconnectMessage msg) {
            }

            @Override
            public void onConnectionLost(InterceptConnectionLostMessage msg) {
            }

            @Override
            public void onPublish(InterceptPublishMessage msg) {
                LOGGER.debug(msg.getTopicName());
                LOGGER.debug(msg.getPayload().toString());
                
                // Reformat into olingo Format
                ODataRequest pseudo = new ODataRequest();
                pseudo.setBody(new ByteBufInputStream(msg.getPayload()));
                pseudo.setRawODataPath(msg.getTopicName());
                // 
                pseudo.set
            }

            @Override
            public void onSubscribe(InterceptSubscribeMessage msg) {
                LOGGER.debug(msg.getClientID());
            }

            @Override
            public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
            }

            @Override
            public void onMessageAcknowledged(InterceptAcknowledgedMessage msg) {
            }
            
        };
        return messageHandler;
    }
    
    private IConfig parseConfig() {
        //TODO: Actually use properties from application properties
        return new MemoryConfig(new Properties());
    }
}
