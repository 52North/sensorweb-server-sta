/*
 * Copyright (C) 2012-2019 52°North Initiative for Geospatial Open Source
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

import java.util.HashMap;

import org.n52.sta.data.EventHandler;
import org.n52.sta.data.ObservationCreateEvent;
import org.n52.sta.data.STAEvent;
import org.n52.sta.mqtt.config.MQTTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
@IntegrationComponentScan
public class MQTTLocalClient implements EventHandler {
    
    @Autowired
    IntegrationFlow gate;

    final Logger LOGGER = LoggerFactory.getLogger(MQTTLocalClient.class);

    public void handleEvent(STAEvent staevent) {
        if (staevent.getEventType().equals(STAEvent.EventType.ObservationCreateEvent)) {
            ObservationCreateEvent event = (ObservationCreateEvent)staevent;


            //TODO: parse the topic based on the event id
            //TODO: serialize Event to correct json output
            String topic = "default";
            String message = "TESTMESSAGE";
            Message<String> msg = new GenericMessage<String>(message, new HashMap<String, Object>() {{put("mqtt_topic", topic);}});
            gate.getInputChannel().send(msg);
            LOGGER.debug("Topic: " + topic + " Message: " + message);
        } else {
            //TODO: Handle other types of events (Update Entity)
        }
    }

    @Bean
    public IntegrationFlow mqttOutboundFlow() {
        return f -> f.handle(new MqttPahoMessageHandler("tcp://localhost:1883", MQTTConfiguration.internalClientId));
    }
}
