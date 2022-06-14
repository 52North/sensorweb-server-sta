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

package org.n52.sta.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.n52.shetland.ogc.sta.StaConstants;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.config.ServerProperties;
import org.n52.sta.config.VersionProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 */
@RestController
public class RootController {

    protected static final String HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_PROPERTIES_MD =
            "https://github.com/52North/sensorweb-server-sta/extension/server-properties.md";
    private static final String HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_CREATE_UPDATE_DELETE =
            "http://www.opengis.net/spec/iot_sensing/1.1/req/create-update-delete";
    private static final String HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_REQUEST_DATA =
            "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data";
    private static final String HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_RESOURCE_PATH_RESOURCE_PATH_TO_ENTITIES =
            "http://www.opengis.net/spec/iot_sensing/1.1/req/resource-path/resource-path-to-entities";
    private static final String HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_DATAMODEL =
            "http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel";

    private static final String HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_VERSION_MD =
            "https://github.com/52North/sensorweb-server-sta/extension/server-version.md";
    private static final String HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_EXTENDED_SENSOR_ENCODINGTYPE =
            "https://github.com/52North/sensorweb-server-sta/extension/extended-sensor-encodingtype.md";

    @Value("${server.feature.http.read-only}")
    private final Boolean httpReadOnly = true;
    private final String rootResponse;
    private final ServerProperties serverProperties;
    private final VersionProperties versionProperties;

    public RootController(
            ObjectMapper mapper,
            Environment environment,
            ServerProperties serverProperties,
            VersionProperties versionProperties) {
        this.serverProperties = serverProperties;
        this.versionProperties = versionProperties;
        rootResponse = createRootResponse(mapper, environment);
    }

    @GetMapping(value = "/")
    public String returnRootResponse() {
        return rootResponse;
    }

    private String createRootResponse(ObjectMapper mapper, Environment environment) {
        ArrayNode endpoints = mapper.createArrayNode();
        String rootUrl = environment.getRequiredProperty("server.config.service-root-url");

        // parse Endpoints
        addToArray(rootUrl, mapper, endpoints, STAEntityDefinition.CORECOLLECTIONS);
        for (String activeProfile : environment.getActiveProfiles()) {
            if (activeProfile.equals(StaConstants.STAPLUS)) {
                addToArray(rootUrl, mapper, endpoints, STAEntityDefinition.CITSCICOLLECTIONS);
            }
        }
        ObjectNode node = mapper.createObjectNode();
        node.set("value", endpoints);

        // parse ServerSettings based on application.properties
        ObjectNode serverSettings = mapper.createObjectNode();
        ArrayNode conformanceClasses = mapper.createArrayNode();
        serverSettings.set("conformance", conformanceClasses);
        conformanceClasses.add(
                HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_DATAMODEL);
        conformanceClasses.add(
                HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_RESOURCE_PATH_RESOURCE_PATH_TO_ENTITIES);
        conformanceClasses.add(
                HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_REQUEST_DATA);
        // Do not list CUD if we are in readOnly-Mode
        if (!httpReadOnly) {
            conformanceClasses.add(
                    HTTP_WWW_OPENGIS_NET_SPEC_IOT_SENSING_1_1_REQ_CREATE_UPDATE_DELETE);
        }

        // 52N Extensions
        conformanceClasses.add(
                HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_PROPERTIES_MD);
        serverSettings.set(HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_PROPERTIES_MD,
                           serverProperties.getFeatureInformation(mapper));

        conformanceClasses.add(
                HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_VERSION_MD);
        serverSettings.set(HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_SERVER_VERSION_MD,
                           versionProperties.getVersionInformation(mapper));

        if (environment.getRequiredProperty("server.feature.variableEncodingType", Boolean.class)) {
            conformanceClasses.add(
                    HTTPS_GITHUB_COM_52_NORTH_SENSORWEB_SERVER_STA_EXTENSION_EXTENDED_SENSOR_ENCODINGTYPE);
        }

        node.set("serverSettings", serverSettings);
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
