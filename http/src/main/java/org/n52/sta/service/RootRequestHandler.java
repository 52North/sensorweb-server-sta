/*
 * Copyright (C) 2018-2023 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
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

    private final String rootResponse;

    public RootRequestHandler(ObjectMapper mapper,
                              Environment environment,
                              RootResponse response) {
        rootResponse = createRootResponse(mapper, environment, response);
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
                                      RootResponse response) {
        ArrayNode endpoints = mapper.createArrayNode();
        String rootUrl = environment.getRequiredProperty("server.rootUrl");

        // parse Endpoints
        addToArray(rootUrl, mapper, endpoints, STAEntityDefinition.ALLCOLLECTIONS);
        ObjectNode node = mapper.createObjectNode();
        node.set("value", endpoints);

        node.set("serverSettings", response.asNode());
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
