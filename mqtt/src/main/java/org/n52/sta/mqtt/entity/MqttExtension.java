package org.n52.sta.mqtt.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.n52.sta.api.server.ServerSettings;
import org.n52.sta.mqtt.config.MqttProperties;

@SuppressWarnings("checkstyle:linelength")
public class MqttExtension {

    private static final String OPENGIS_MQTT_OBSERVATIONS_CREATION = "http://www.opengis.net/spec/iot_sensing/1.1/req/create-observations-via-mqtt/observations-creation";
    private static final String OPENGIS_MQTT_RECEIVE_UPDATES = "http://www.opengis.net/spec/iot_sensing/1.1/req/receive-updates-via-mqtt/receive-updates";
    private static final String N52_CREATE_VIA_MQTT = "https://github.com/52North/sensorweb-server-sta/extension/create-via-mqtt.md";

    private final ServerSettings serverSettings;

    public MqttExtension(MqttProperties mqttProperties, ServerSettings serverSettings) {
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
            if (!mqttProperties.isReadOnly()) {
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
            if (!mqttProperties.isReadOnly()) {
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
