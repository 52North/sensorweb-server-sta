package org.n52.sta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = { "org.n52.series.db", "org.n52.sta.data.repositories"},
                       excludeFilters = @Filter(type = FilterType.REGEX, pattern = "org\\.n52\\.series\\.db\\.old\\..*"))
@ComponentScan(basePackages = {"org.n52.series.srv", "org.n52.series.db", "org.n52.sta"},
               excludeFilters = @Filter(type = FilterType.REGEX, pattern = "org\\.n52\\.series\\.db\\.old\\.da\\.[^EntityCounter]"))

public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
