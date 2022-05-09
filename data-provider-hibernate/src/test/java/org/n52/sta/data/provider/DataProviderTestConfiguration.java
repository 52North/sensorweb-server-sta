package org.n52.sta.data.provider;

import org.n52.sta.config.DataProviderConfiguration;
import org.n52.sta.config.HibernateConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootConfiguration
@Import(HibernateConfiguration.class)
@TestPropertySource("classpath:/application.properties")
public class DataProviderTestConfiguration extends DataProviderConfiguration {


}
