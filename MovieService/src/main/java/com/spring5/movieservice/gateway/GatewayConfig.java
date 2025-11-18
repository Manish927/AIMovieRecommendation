package com.spring5.movieservice.gateway;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    /**
     * Create LoadBalanced WebClient Builder
     * This enables load balancing across service instances
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Recommendation Service WebClient with Load Balancing
     * Uses service name - LoadBalancer will resolve to available instances
     */
    @Bean(name = "recommendationWebClient")
    public WebClient recommendationWebClient() {
        // Create WebClient without baseUrl since we'll construct full URIs manually
        // LoadBalancer is used via LoadBalancerClient for instance selection
        return WebClient.builder().build();
    }

    /**
     * Ticket Booking Service WebClient with Load Balancing
     */
    @Bean(name = "ticketBookingWebClient")
    public WebClient ticketBookingWebClient(@LoadBalanced WebClient.Builder builder) {
        // Use service name only - LoadBalancer will resolve host and port from ServiceInstance
        return builder.baseUrl("http://ticket-booking-service").build();
    }
}

