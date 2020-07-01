/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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
import org.n52.shetland.ogc.sta.model.extension.CitSciExtensionEntityDefinition;
import org.springframework.beans.factory.annotation.Value;
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

    private final String rootUrl;
    private final ObjectMapper mapper;
    private final boolean citSciExtensionActive;

    private final String NAME = "name";
    private final String URL = "url";

    public RootRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                              ObjectMapper mapper,
                              Environment environment) {
        this.rootUrl = rootUrl;
        this.mapper = mapper;
        //TODO: refactor into global constant
        this.citSciExtensionActive = environment.acceptsProfiles("citSciExtension");
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
        return createRootResponse(this.rootUrl);
    }

    private String createRootResponse(String uri) {
        ArrayNode arrayNode = mapper.createArrayNode();
        for (String collection : STAEntityDefinition.ALLCOLLECTIONS) {
            ObjectNode node = mapper.createObjectNode();
            node.put(NAME, collection);
            node.put(URL, uri + collection);
            arrayNode.add(node);
        }

        if (citSciExtensionActive) {
            for (String collection : CitSciExtensionEntityDefinition.ALLCOLLECTIONS) {
                ObjectNode node = mapper.createObjectNode();
                node.put(NAME, collection);
                node.put(URL, uri + collection);
                arrayNode.add(node);
            }
        }

        ObjectNode node = mapper.createObjectNode();
        node.put("value", arrayNode);
        return node.toString();
    }

}
