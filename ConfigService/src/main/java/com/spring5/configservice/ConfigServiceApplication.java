package com.spring5.configservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Spring Cloud Config Server
 * 
 * Centralized configuration management service for all microservices.
 * Provides externalized configuration from a Git repository.
 * 
 * Features:
 * - Git-based configuration storage
 * - Environment-specific configurations
 * - Dynamic configuration refresh
 * - REST API for configuration access
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServiceApplication.class, args);
    }
}

