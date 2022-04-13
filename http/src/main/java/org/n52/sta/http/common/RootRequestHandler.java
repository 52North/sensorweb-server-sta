/*
 * Copyright (C) 2018-2021 52°North Spatial Information Research GmbH
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
package org.n52.sta.http.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.ServerProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles all requests to the root
 * e.g. /
 *
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@RestController
public class RootRequestHandler {

    protected static final String HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_PROPERTIES_MD =
        "https://github.com/52North/sensorweb-server-sta/extension/server-properties.md";
    private static final String ENDPOINTS = "endpoints";
    private static final String COLON = ":";
    private static final String SLASH = "/";
    private static final String
        HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_CREATE_OBSERVATIONS_VIA_MQTT_OBSERVATIONS_CREATION =
        "http://www.opengis.net/spec/iot_sensing/1.1/req/create-observations-via-mqtt/observations-creation";
    private static final String
        HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_RECEIVE_UPDATES_VIA_MQTT_RECEIVE_UPDATES =
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

    private final String rootResponse;

    public RootRequestHandler(ObjectMapper mapper,
                              Environment environment,
                              ServerProperties serverProperties) {
        rootResponse = createRootResponse(mapper, environment, serverProperties);
    }

    /**
     * Matches the request to the root resource
     * e.g. /
     */
    @GetMapping(
        value = "/",
        produces = "application/json"
    )
    public String returnRootResponse() {
        return rootResponse;
    }

    private String createRootResponse(ObjectMapper mapper,
                                      Environment environment,
                                      ServerProperties serverProperties) {
        ArrayNode endpoints = mapper.createArrayNode();
        String rootUrl = environment.getRequiredProperty("server.rootUrl");

        // parse Endpoints
        addToArray(rootUrl, mapper, endpoints, STAEntityDefinition.CORECOLLECTIONS);
        for (String activeProfile : environment.getActiveProfiles()) {
            if (activeProfile.equals(StaConstants.STAPLUS)) {
                addToArray(rootUrl, mapper, endpoints, STAEntityDefinition.CITSCICOLLECTIONS);
            }
        }
        ObjectNode node = mapper.createObjectNode();
        node.put("value", endpoints);

        // parse ServerSettings based on application.properties
        ObjectNode serverSettings = mapper.createObjectNode();
        ArrayNode conformanceClasses = mapper.createArrayNode();
        serverSettings.put("conformance", conformanceClasses);
        conformanceClasses.add(
            HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_DATAMODEL);
        conformanceClasses.add(
            HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_RESOURCE_PATH_RESOURCE_PATH_TO_ENTITIES);
        conformanceClasses.add(
            HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_REQUEST_DATA);
        // Do not list CUD if we are in readOnly-Mode
        if (!serverProperties.getHttpReadOnly()) {
            conformanceClasses.add(
                HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_CREATE_UPDATE_DELETE);
        }

        // 52N Extensions
        conformanceClasses.add(
            HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_PROPERTIES_MD);
        serverSettings.put(HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_PROPERTIES_MD,
                           serverProperties.getFeatureInformation(mapper));

        conformanceClasses.add(
            HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_VERSION_MD);
        serverSettings.put(HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_VERSION_MD,
                           serverProperties.getVersionInformation(mapper));

        if (environment.getRequiredProperty("server.feature.variableEncodingType", Boolean.class)) {
            conformanceClasses.add(
                HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_EXTENDED_SENSOR_ENCODINGTYPE
            );
        }

        // MQTT Extensions
        Boolean plainTcpEnabled = environment.getRequiredProperty("mqtt.broker.plaintcp.enabled", Boolean.class);
        Boolean wsEnabled = environment.getRequiredProperty("mqtt.broker.websocket.enabled", Boolean.class);
        if (plainTcpEnabled || wsEnabled) {
            // Add to conformanceClasses Array
            conformanceClasses.add(
                HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_RECEIVE_UPDATES_VIA_MQTT_RECEIVE_UPDATES);
            if (!serverProperties.getMqttReadOnly()) {
                conformanceClasses.add(
                    HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_CREATE_OBSERVATIONS_VIA_MQTT_OBSERVATIONS_CREATION);
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
            mqttEndpointsArray.put(ENDPOINTS, mqttEndpoints);
            serverSettings.put(
                HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_RECEIVE_UPDATES_VIA_MQTT_RECEIVE_UPDATES,
                mqttEndpointsArray);

            // MQTT Publish
            if (!serverProperties.getMqttReadOnly()) {
                ObjectNode mqttPublishSettings = mapper.createObjectNode();
                mqttPublishSettings.put(ENDPOINTS, mqttEndpoints);
                serverSettings.put(
                    HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_CREATE_OBSERVATIONS_VIA_MQTT_OBSERVATIONS_CREATION,
                    mqttEndpointsArray);

                ObjectNode mqttCustomPublishSettings = mapper.createObjectNode();
                ArrayNode availableMqttPublishEndpoints = mapper.createArrayNode();
                serverProperties.getMqttPublishTopics().forEach(availableMqttPublishEndpoints::add);
                mqttCustomPublishSettings.put(ENDPOINTS, mqttEndpoints);
                mqttCustomPublishSettings.put("entities", availableMqttPublishEndpoints);
                serverSettings.put(
                    HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_CREATE_VIA_MQTT_MD,
                    mqttCustomPublishSettings);
            }
        }
        node.put("serverSettings", serverSettings);
        return node.toString();
    }

    private void addToArray(String rootUrl, ObjectMapper mapper, ArrayNode array, String[] endpoints) {
        for (String collection : endpoints) {
            ObjectNode node = mapper.createObjectNode();
            node.put("name", collection);
            node.put("url", rootUrl + collection);
            array.add(node);
        }
    }
}
