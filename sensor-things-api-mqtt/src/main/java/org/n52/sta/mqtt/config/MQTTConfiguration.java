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
package org.n52.sta.mqtt.config;

import java.util.HashMap;
import java.util.Map;

import org.n52.sta.mapping.AbstractMapper;
import org.n52.sta.mapping.ObservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
@Component
public class MQTTConfiguration {

    public static final String internalClientId = "POC";

    @Autowired
    ObservationMapper obsMapper;
    
    /**
     * Sets up Local Paho Client to connect to local Broker.
     * @return
     */
    @Bean
    public IntegrationFlow mqttOutboundFlow() {
        return f -> f.handle(new MqttPahoMessageHandler("tcp://localhost:1883", MQTTConfiguration.internalClientId));
    }

    /**
     * Multiplexes to the different Mappers for transforming Beans into olingo Entities
     * @param className Name of the base class
     * @return Mapper appropiate for this class
     */
    public AbstractMapper getMapper(String className) {
        switch(className) {
        case "org.n52.series.db.beans.QuantityDataEntity": 
            return obsMapper;
        default: return null;
        }
    }

    /**
     * Maps olingo Types to Base types. Needed for fail-fast.
     * @return Translation map from olingo Entities to raw Data Entities
     */
    public static Map<String, String> getBeanTypes() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("iot.Observation", "org.n52.series.db.beans.QuantityDataEntity");
        return map;
        
    }

}
