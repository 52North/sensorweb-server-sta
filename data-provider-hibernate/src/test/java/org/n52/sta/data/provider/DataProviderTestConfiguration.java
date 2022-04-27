package org.n52.sta.data.provider;

import org.n52.sta.config.DataProviderConfiguration;
import org.n52.sta.data.repositories.BaseRepositoryImpl;
import org.n52.sta.data.repositories.entity.ThingRepository;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
public class DataProviderTestConfiguration extends DataProviderConfiguration {

    @Bean
    public ThingEntityProvider getThingEntityProvider(ThingRepository thingRepository) {
        return new ThingEntityProvider(thingRepository);
    }

    @Configuration
    @EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class, basePackages = "org.n52.sta.data.repositories")
    public static class RepositoryConfig {
        // inject via annotations
    }
}
