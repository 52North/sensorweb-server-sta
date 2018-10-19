package org.n52.sta.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = { "org.n52.series.db", "org.n52.sta.data.repositories"},
                       excludeFilters = @Filter(type = FilterType.REGEX, pattern = "org\\.n52\\.series\\.db\\.old\\..*"))
@ComponentScan(basePackages = {"org.n52.series.srv", "org.n52.series.db", "org.n52.sta"},
               excludeFilters = @Filter(type = FilterType.REGEX, pattern = "org\\.n52\\.series\\.db\\.old\\.da\\.[^EntityCounter]"))
public class Application  extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

}
