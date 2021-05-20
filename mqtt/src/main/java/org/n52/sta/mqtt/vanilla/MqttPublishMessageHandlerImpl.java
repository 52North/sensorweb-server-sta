/*
 * Copyright (C) 2018-2021 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.mqtt.vanilla;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.moquette.interception.messages.InterceptPublishMessage;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlException;
import org.n52.shetland.ogc.sta.model.STAEntityDefinition;
import org.n52.sta.DTOMapper;
import org.n52.sta.api.AbstractSensorThingsEntityService;
import org.n52.sta.api.CoreRequestUtils;
import org.n52.sta.api.EntityServiceFactory;
import org.n52.sta.api.dto.StaDTO;
import org.n52.sta.mqtt.MqttHandlerException;
import org.n52.sta.utils.AbstractSTARequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class MqttPublishMessageHandlerImpl extends AbstractSTARequestHandler
    implements MqttPublishMessageHandler, CoreRequestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublishMessageHandlerImpl.class);

    private final ObjectMapper mapper;
    private final Set<String> publishTopics;

    private final boolean readOnly;
    private final DTOMapper dtoMapper;

    public MqttPublishMessageHandlerImpl(
        @Value("${server.feature.mqttPublishTopics:Observations}") List<String> publishTopics,
        @Value("${server.feature.mqttReadOnly}") boolean readOnly,
        @Value("${server.rootUrl}") String rootUrl,
        @Value("${server.feature.escapeId:true}") boolean shouldEscapeId,
        EntityServiceFactory serviceRepository,
        ObjectMapper mapper,
        DTOMapper dtoMapper) {
        super(rootUrl, shouldEscapeId, serviceRepository);
        this.mapper = mapper;
        this.readOnly = readOnly;
        this.dtoMapper = dtoMapper;
        Set topics = new HashSet<>(publishTopics);

        // Fallback to default if parameter was invalid
        if (!validateTopics(topics)) {
            this.publishTopics = Collections.singleton(STAEntityDefinition.OBSERVATIONS);
            LOGGER.error("Invalid mqttPublishTopics given. Using only default Topic!");
        } else {
            this.publishTopics = topics;
        }
        LOGGER.info("Initialized mqttPublishTopics: " + String.join(",", topics));
    }

    /**
     * Validates that topics only include STA collections
     *
     * @param topics list of wanted topics
     */
    private boolean validateTopics(Set<String> topics) {
        boolean valid = true;
        for (String topic : topics) {
            valid = Arrays.asList(STAEntityDefinition.CORECOLLECTIONS).contains(topic)
                || Arrays.asList(STAEntityDefinition.CITSCICOLLECTIONS).contains(topic);
        }
        return valid;
    }

    @Override public <T extends StaDTO> void processPublishMessage(InterceptPublishMessage msg) {
        try {
            if (msg.getClientID().equals(INTERNAL_CLIENT_ID) || readOnly) {
                return;
            }
            // This may only be a reference to Observation collection
            // Remove leading slash if present
            String topic = (msg.getTopicName().startsWith("/")) ? msg.getTopicName().substring(1) : msg.getTopicName();
            if (!topic.startsWith("v1.1/")) {
                throw new MqttHandlerException("Error while parsing MQTT topic. Missing Version information!");
            }
            topic = topic.substring(5);

            // Check topic for syntax+semantics
            validateResource(new StringBuffer(topic), serviceRepository);

            // Check if topic references valid Collection
            boolean valid = false;
            String collection = "";
            for (String publishTopic : publishTopics) {
                if (topic.endsWith(publishTopic)) {
                    valid = true;
                    collection = publishTopic;
                    break;
                }
            }
            if (valid) {
                String payload;
                // Check whether we are posted via a related collection
                if (topic.contains("/")) {
                    String[] split = topic.split("/");
                    String[] reference = split[split.length - 2].split("\\(");
                    String sourceType = reference[0];
                    String sourceId = reference[1].replace(")", "");
                    ObjectNode jsonBody =
                        (ObjectNode) mapper.readTree(msg.getPayload().toString(Charset.defaultCharset()));
                    jsonBody.put(REFERENCED_FROM_TYPE, sourceType);
                    jsonBody.put(REFERENCED_FROM_ID, sourceId);
                    payload = jsonBody.toString();
                } else {
                    payload = msg.getPayload().toString(Charset.defaultCharset());
                }

                Class<T> clazz = dtoMapper.collectionNameToClass(collection);
                ((AbstractSensorThingsEntityService<T>) serviceRepository.getEntityService(collection))
                    .create(mapper.readValue(payload, clazz));
            } else {
                throw new STAInvalidUrlException("Topic does not reference a Collection allowed for POSTing via mqtt");
            }
        } catch (Throwable e) {
            LOGGER.error("Creation of Entity {} on topic {} failed with Exception {}!",
                         msg.getPayload().toString(StandardCharsets.UTF_8),
                         msg.getTopicName(),
                         e.getMessage());
        }
    }
}
