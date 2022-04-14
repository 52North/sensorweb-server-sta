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
package org.n52.sta.web;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.n52.sta.api.service.DataStreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class DataStreamController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataStreamController.class);

    private final DataStreamService service;
    private final ObjectMapper objectMapper;

    public DataStreamController(DataStreamService service) {
        this.objectMapper = new ObjectMapper();
        this.service = service;
    }

    @GetMapping(path = "/DataStreams", produces = "application/json")
    public StreamingResponseBody getCollection() {
        return outputStream -> {
            JsonFactory jfactory = new JsonFactory();
            try (JsonGenerator jGenerator = jfactory.createGenerator(outputStream, JsonEncoding.UTF8)) {
                jGenerator.setCodec(objectMapper);
                jGenerator.writeStartArray();
                service.<JsonNode>getCollection(objectMapper::valueToTree).forEach(item -> {
                    try {
                        jGenerator.writeTree(item);
                    } catch (IOException e) {
                        LOGGER.error("writing object failed.", e);
                    }
                });
                jGenerator.writeEndArray();
            }
        };
    }

}