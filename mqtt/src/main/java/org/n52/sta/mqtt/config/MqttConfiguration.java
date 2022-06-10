/*
 * Copyright (C) 2018-2022 52°North Spatial Information Research GmbH
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

package org.n52.sta.mqtt.config;

import java.util.List;

import org.n52.sta.api.old.EntityServiceFactory;
import org.n52.sta.mqtt.old.MqttBroker;
import org.n52.sta.mqtt.old.MqttPublishMessageHandler;
import org.n52.sta.mqtt.old.MqttPublishMessageHandlerImpl;
import org.n52.sta.mqtt.old.MqttSubscriptionEventHandler;
import org.n52.sta.mqtt.old.MqttSubscriptionEventHandlerImpl;
import org.n52.sta.old.utils.DTOMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@ConfigurationProperties(prefix = "server.feature")
@ConditionalOnProperty(name = "server.feature.mqtt.enabled", havingValue = "true", matchIfMissing = false)
@SuppressWarnings("checkstyle:linelength")
public class MqttConfiguration {

    @Value("mqtt.enabled:false")
    private boolean mqttEnabled;

    @Bean(initMethod = "init", destroyMethod = "stopServer")
    public MqttBroker getMqttBroker(@Value("${mqtt.broker.store.folder:}") String storePath,
                                    @Value("${mqtt.broker.store.filename:52N-STA-MQTTBroker.h2}") String storeFilename,
                                    @Value("${mqtt.broker.store.autosave-in-seconds:30}") String autosaveIntervalProperty,
                                    @Value("${mqtt.broker.store.enabled}") Boolean persistenceEnabled,
                                    @Value("${mqtt.broker.ws.enabled}") Boolean websocketEnabled,
                                    @Value("${mqtt.broker.ws.port:8080}") String websocketPort,
                                    @Value("${mqtt.broker.tcp.enabled}") Boolean plainTcpEnabled,
                                    @Value("${mqtt.broker.tcp.port:1883}") String plainTcpPort,
                                    MqttSubscriptionEventHandler subscriptionHandler,
                                    MqttPublishMessageHandler publishHandler) {

        return new MqttBroker(storePath,
                              storeFilename,
                              autosaveIntervalProperty,
                              persistenceEnabled,
                              websocketEnabled,
                              websocketPort,
                              plainTcpEnabled,
                              plainTcpPort,
                              subscriptionHandler,
                              publishHandler);
    }

    @Bean
    public MqttSubscriptionEventHandler getMqttMessageHandler(@Value("${server.config.service-root-url}") String rootUrl,
                                                              @Value("${server.feature.escapeId:true}") boolean shouldEscapeId,
                                                              ObjectMapper mapper) {
        return new MqttSubscriptionEventHandlerImpl(rootUrl, shouldEscapeId, mapper);
    }

    @Bean
    public MqttPublishMessageHandler getMqttMessageHandler(@Value("${mqtt.publication-topics:Observations}") List<String> publishTopics,
                                                           @Value("${mqtt.read-only}") boolean readOnly,
                                                           @Value("${server.config.service-root-url}") String rootUrl,
                                                           @Value("${server.feature.escapeId:true}") boolean escapeId,
                                                           EntityServiceFactory serviceRepository,
                                                           ObjectMapper mapper,
                                                           DTOMapper dtoMapper) {

        return new MqttPublishMessageHandlerImpl(publishTopics,
                                                 readOnly,
                                                 rootUrl,
                                                 escapeId,
                                                 serviceRepository,
                                                 mapper,
                                                 dtoMapper);
    }

}
