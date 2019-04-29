/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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
//package org.n52.sta.mqtt;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.n52.sta.Application;
//import org.n52.sta.mqtt.MQTTTests.TestConfig;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.SpringBootConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//import org.springframework.integration.dsl.IntegrationFlow;
//import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.GenericMessage;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes= {TestConfig.class})
//@SpringBootConfiguration
////@TestPropertySource(locations="classpath:application.yml")
////@EnableAutoConfiguration
////@AutoConfigureTestDatabase
////@DataJpaTest
//public class MQTTTests {
//
//    private final String clientId = "52NTestClient";
//
//    private Map<String, Object> headers = new HashMap<String, Object>(1);
//
//    @Autowired
//    IntegrationFlow flow;
//
//    @Configuration
//    @Import({Application.class})
//    //@EnableConfigurationProperties
//    //@EnableJpaRepositories(basePackages = {"org.n52.sta.data.repositories"})
//    //@ComponentScan(basePackages = {"org.n52.sta"})
//    public static class TestConfig{}
//
//    /**
//     * Sets up Local Paho Client to connect to local Broker.
//     * @return
//     */
//    @Bean
//    public IntegrationFlow mqttOutboundFlow() {
//        return f -> f.handle(new MqttPahoMessageHandler("tcp://localhost:1883", clientId));
//    }
//
//	@Test
//	public void contextLoads() {
//	    String topic = "test";
//	    headers.put("mqtt_topic", "test");
//        Message<String> msg = new GenericMessage<String>("asdf", headers);
//        flow.getInputChannel().send(msg, 5);
//
//	}
//
//}
