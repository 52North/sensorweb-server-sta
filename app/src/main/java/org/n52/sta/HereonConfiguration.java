package org.n52.sta;

import org.n52.sensorweb.server.helgoland.adapters.connector.hereon.HereonConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = HereonConfig.class)
public class HereonConfiguration {
}
