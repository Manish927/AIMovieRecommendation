package com.spring5.movieservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

@SpringBootApplication
public class MovieServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(MovieServiceApplication.class);



    public static void main(String[] args) {

        ConfigurableApplicationContext ctx = SpringApplication.run(MovieServiceApplication.class, args);

        String mysqlDBUrl = ctx.getEnvironment().getProperty("spring.r2dbc.url");
        String mysqlDBUsername = ctx.getEnvironment().getProperty("spring.r2dbc.username");
        LOG.info("Connected to MySQL : {} with user: {}", mysqlDBUrl, mysqlDBUsername);
    }
}
