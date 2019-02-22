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
