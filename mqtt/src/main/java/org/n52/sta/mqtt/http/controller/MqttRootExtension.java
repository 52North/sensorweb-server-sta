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

package org.n52.sta.mqtt.http.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.n52.sta.api.server.ServerSettings;
import org.n52.sta.mqtt.config.MqttProperties;

@SuppressWarnings("checkstyle:linelength")
public class MqttRootExtension {

    private static final String OPENGIS_MQTT_OBSERVATIONS_CREATION = "http://www.opengis.net/spec/iot_sensing/1.1/req/create-observations-via-mqtt/observations-creation";
    private static final String OPENGIS_MQTT_RECEIVE_UPDATES = "http://www.opengis.net/spec/iot_sensing/1.1/req/receive-updates-via-mqtt/receive-updates";
    private static final String N52_CREATE_VIA_MQTT = "https://github.com/52North/sensorweb-server-sta/extension/create-via-mqtt.md";

    private final ServerSettings serverSettings;

    public MqttRootExtension(MqttProperties mqttProperties, ServerSettings serverSettings) {
        Objects.requireNonNull(mqttProperties, "mqttProperties must not be null!");
        Objects.requireNonNull(serverSettings, "serverSettings must not be null!");
        this.serverSettings = serverSettings;
        udpateServerSettings(mqttProperties);
    }

    private void udpateServerSettings(MqttProperties mqttProperties) {
        boolean plainTcpEnabled = mqttProperties.isBrokerTcpEnabled();
        boolean wsEnabled = mqttProperties.isBrokerWsEnabled();
        if (plainTcpEnabled || wsEnabled) {
            addConformance(OPENGIS_MQTT_RECEIVE_UPDATES);
            if (mqttProperties.isWritable()) {
                addConformance(OPENGIS_MQTT_OBSERVATIONS_CREATION);
                addConformance(N52_CREATE_VIA_MQTT);
            }

            List<String> endpoints = new ArrayList<>();
            if (plainTcpEnabled) {
                String tcpHost = mqttProperties.getBrokerTcpHost();
                int tcpPort = mqttProperties.getBrokerTcpPort();
                endpoints.add(createUri(tcpHost, tcpPort));

            }
            if (wsEnabled) {
                String wsHost = mqttProperties.getBrokerWsHost();
                int wsPort = mqttProperties.getBrokerWsPort();
                endpoints.add(createUri(wsHost, wsPort));
            }

            ServerSettings.Extension mqttExtension = new ServerSettings.Extension() {
                @Override
                public Map<String, Object> getProperties() {
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("endpoints", endpoints);
                    return properties;
                }
            };

            // MQTT Updates are always active if mqtt is active
            serverSettings.addExtension(OPENGIS_MQTT_RECEIVE_UPDATES, mqttExtension);

            // MQTT Publish
            if (mqttProperties.isWritable()) {
                serverSettings.addExtension(OPENGIS_MQTT_OBSERVATIONS_CREATION, mqttExtension);
                serverSettings.addExtension(N52_CREATE_VIA_MQTT, new ServerSettings.Extension() {
                    @Override
                    public Map<String, Object> getProperties() {
                        Map<String, Object> properties = new HashMap<>(mqttExtension.getProperties());
                        properties.put("entities", mqttProperties.getPublicationTopics());
                        return properties;
                    }
                });
            }
        }
    }

    private String createUri(String host, int port) {
        return String.format("%s:%d", host, port);
    }

    private void addConformance(String conformanceClass) {
        serverSettings.addConformanceClass(conformanceClass);
    }

}
