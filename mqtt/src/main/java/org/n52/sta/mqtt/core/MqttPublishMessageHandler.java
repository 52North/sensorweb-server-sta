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

package org.n52.sta.mqtt.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.moquette.interception.messages.InterceptPublishMessage;
import org.n52.series.db.beans.DataEntity;
import org.n52.shetland.ogc.sta.exception.STACRUDException;
import org.n52.shetland.ogc.sta.exception.STAInvalidUrlThrowable;
import org.n52.sta.data.service.AbstractSensorThingsEntityService;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.serdes.model.STAEntityDefinition;
import org.n52.sta.utils.STARequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:s.drost@52north.org">Sebastian Drost</a>
 */
@Component
public class MqttPublishMessageHandler implements STARequestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublishMessageHandler.class);

    private final EntityServiceRepository serviceRepository;
    private final ObjectMapper mapper;
    private final Set<String> publishTopics;

    public MqttPublishMessageHandler(
            @Value("${server.feature.mqttPublishTopics:Observations}") List<String> publishTopics,
            EntityServiceRepository serviceRepository,
            ObjectMapper mapper) {
        this.serviceRepository = serviceRepository;
        this.mapper = mapper;
        this.publishTopics = new HashSet<>(publishTopics);
    }

    public void processPublishMessage(InterceptPublishMessage msg) {
        try {
            // This may only be a reference to Observation collection
            // Remove leading slash if present
            String topic = (msg.getTopicName().startsWith("/")) ? msg.getTopicName().substring(1) : msg.getTopicName();

            // Check topic for syntax+semantics
            validateURL(new StringBuffer(topic), serviceRepository, 0);

            // Check if topic references valid Collection
            boolean valid = false;
            for (String publishTopic : publishTopics) {
                if (topic.endsWith("/" + publishTopic)) {
                    valid = true;
                    break;
                }
            }
            if (valid) {
                ((AbstractSensorThingsEntityService<?, DataEntity>) serviceRepository.getEntityService(OBSERVATIONS))
                        .create(mapper.readValue(msg.getPayload().toString(), DataEntity.class));
            } else {
                throw new STAInvalidUrlThrowable("Topic does not reference a Collection allowed for POSTing via mqtt");
            }
        } catch (STACRUDException | JsonProcessingException | STAInvalidUrlThrowable e) {
            LOGGER.error("Creation of Entity {} on topic {} failed with Exception {}!",
                         msg.getPayload().toString(),
                         msg.getTopicName(),
                         e.getMessage());
        }
    }
}
