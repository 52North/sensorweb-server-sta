package org.n52.sta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.n52.sta.ServerProperties;
import org.springframework.core.env.Environment;

public class RootResponse {

    private static final String ENDPOINTS = "endpoints";
    private static final String COLON = ":";
    private static final String HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_PROPERTIES_MD =
            "https://github.com/52North/sensorweb-server-sta/extension/server-properties.md";
    private static final String
            MQTT_CREATE =
            "http://www.opengis.net/spec/iot_sensing/1.1/req/create-observations-via-mqtt/observations-creation";
    private static final String
            MQTT_RECEIVE_UPDATES =
            "http://www.opengis.net/spec/iot_sensing/1.1/req/receive-updates-via-mqtt/receive-updates";
    private static final String HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_CREATE_UPDATE_DELETE =
            "http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete";
    private static final String HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_REQUEST_DATA =
            "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data";
    private static final String
            HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_RESOURCE_PATH_RESOURCE_PATH_TO_ENTITIES =
            "http://www.opengis.net/spec/iot_sensing/1.1/req/resource-path/resource-path-to-entities";
    private static final String HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_DATAMODEL =
            "http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel";
    private static final String HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_CREATE_VIA_MQTT_MD =
            "https://github.com/52North/sensorweb-server-sta/extension/create-via-mqtt.md";
    private static final String HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_VERSION_MD =
            "https://github.com/52North/sensorweb-server-sta/extension/server-version.md";
    private static final String HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_EXTENDED_SENSOR_ENCODINGTYPE =
            "https://github.com/52North/sensorweb-server-sta/extension/extended-sensor-encodingtype.md";

    protected final ArrayNode conformanceClasses;
    protected final ObjectNode serverSettings;

    private final ObjectMapper mapper;
    private final Environment environment;
    private final ServerProperties serverProperties;

    public RootResponse(ObjectMapper mapper, Environment environment, ServerProperties serverProperties) {
        this.mapper = mapper;
        this.conformanceClasses = mapper.createArrayNode();
        this.serverSettings = mapper.createObjectNode();
        this.environment = environment;
        this.serverProperties = serverProperties;
    }

    public ObjectNode asNode() {
        serverSettings.set("conformance", conformanceClasses);
        return serverSettings;
    }

    public void addDatamodel() {
        conformanceClasses.add(
                HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_DATAMODEL);
    }

    public void addRequestData() {
        conformanceClasses.add(
                HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_REQUEST_DATA);
    }

    public void addResourcePath() {
        conformanceClasses.add(
                HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_RESOURCE_PATH_RESOURCE_PATH_TO_ENTITIES);
    }

    public void addCUD() {
        // Do not list CUD if we are in readOnly-Mode
        if (!serverProperties.getHttpReadOnly()) {
            conformanceClasses.add(
                    HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_CREATE_UPDATE_DELETE);
        }
    }

    public void addServerVersionExtension() {
        conformanceClasses.add(
                HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_VERSION_MD);
        serverSettings.set(HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_VERSION_MD,
                serverProperties.getVersionInformation(mapper));
    }

    public void addServerPropertiesExtension() {
        this.conformanceClasses.add(
                HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_PROPERTIES_MD);
        this.serverSettings.set(HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_PROPERTIES_MD,
                serverProperties.getFeatureInformation(mapper));
    }

    public void addVariableEncodingTypeExtension() {
        if (environment.getRequiredProperty("server.feature.variableEncodingType", Boolean.class)) {
            conformanceClasses.add(
                    HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_EXTENDED_SENSOR_ENCODINGTYPE
            );
        }
    }

    public void addMQTT() {
        // MQTT Extensions
        Boolean plainTcpEnabled = environment.getRequiredProperty("mqtt.broker.plaintcp.enabled", Boolean.class);
        Boolean wsEnabled = environment.getRequiredProperty("mqtt.broker.websocket.enabled", Boolean.class);
        if (plainTcpEnabled || wsEnabled) {
            // Add to conformanceClasses Array
            conformanceClasses.add(
                    MQTT_RECEIVE_UPDATES);
            if (!serverProperties.getMqttReadOnly()) {
                conformanceClasses.add(
                        MQTT_CREATE);
                conformanceClasses.add(
                        HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_CREATE_VIA_MQTT_MD);
            }

            // Parse MQTT Endpoints
            ArrayNode mqttEndpoints = mapper.createArrayNode();
            // Check which endpoints are enabled
            if (plainTcpEnabled) {
                mqttEndpoints.add(environment.getRequiredProperty("mqtt.broker.plaintcp.host")
                        + COLON
                        + environment.getRequiredProperty("mqtt.broker.plaintcp.port"));

            }
            if (wsEnabled) {
                mqttEndpoints.add(environment.getRequiredProperty("mqtt.broker.websocket.host")
                        + COLON
                        + environment.getRequiredProperty("mqtt.broker.websocket.port"));

            }

            // MQTT Updates are always active if mqtt is active
            ObjectNode mqttEndpointsArray = mapper.createObjectNode();
            mqttEndpointsArray.set(ENDPOINTS, mqttEndpoints);
            serverSettings.set(
                    MQTT_RECEIVE_UPDATES,
                    mqttEndpointsArray);

            // MQTT Publish
            if (!serverProperties.getMqttReadOnly()) {
                ObjectNode mqttPublishSettings = mapper.createObjectNode();
                mqttPublishSettings.set(ENDPOINTS, mqttEndpoints);
                serverSettings.set(
                        MQTT_CREATE,
                        mqttEndpointsArray);

                ObjectNode mqttCustomPublishSettings = mapper.createObjectNode();
                ArrayNode availableMqttPublishEndpoints = mapper.createArrayNode();
                serverProperties.getMqttPublishTopics().forEach(availableMqttPublishEndpoints::add);
                mqttCustomPublishSettings.set(ENDPOINTS, mqttEndpoints);
                mqttCustomPublishSettings.set("entities", availableMqttPublishEndpoints);
                serverSettings.set(
                        HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_CREATE_VIA_MQTT_MD,
                        mqttCustomPublishSettings);
            }
        }
    }

}
