package org.n52.sta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application {

//    @Autowired
//    private ApplicationContext appContext;

    public static void main(String[] args) {
//        System.setProperty("server.servlet.context-path", "/sta");
        SpringApplication.run(Application.class, args);
    }
}
