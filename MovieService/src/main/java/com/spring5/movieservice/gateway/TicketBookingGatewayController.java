package com.spring5.movieservice.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/ticket-booking")
public class TicketBookingGatewayController {

    private static final Logger LOG = LoggerFactory.getLogger(TicketBookingGatewayController.class);

    private final WebClient ticketBookingWebClient;
    private final LoadBalancerClient loadBalancerClient;

    @Autowired
    public TicketBookingGatewayController(
            @Qualifier("ticketBookingWebClient") WebClient ticketBookingWebClient,
            LoadBalancerClient loadBalancerClient) {
        this.ticketBookingWebClient = ticketBookingWebClient;
        this.loadBalancerClient = loadBalancerClient;
    }

    /**
     * Route GET requests to ticket booking service
     */
    @GetMapping("/**")
    public Mono<Object> routeTicketBookingRequest(
            ServerHttpRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String requestPath = request.getURI().getPath();
        String pathAfterApi = requestPath.replace("/api/ticket-booking", "");
        String queryString = request.getURI().getQuery();
        
        LOG.info("Gateway: Extracted ticket booking path: {}, Query: {}", pathAfterApi, queryString);
        
        // Resolve service instance using LoadBalancer
        return Mono.fromCallable(() -> {
            ServiceInstance instance = loadBalancerClient.choose("ticket-booking-service");
            if (instance == null) {
                throw new RuntimeException("No available instance for ticket-booking-service");
            }
            return instance;
        })
        .subscribeOn(Schedulers.boundedElastic()) // Move blocking call to bounded elastic scheduler
        .flatMap(instance -> {
            String fullUri = String.format("http://%s:%d%s%s",
                    instance.getHost(),
                    instance.getPort(),
                    pathAfterApi,
                    queryString != null ? "?" + queryString : "");
            
            LOG.info("Gateway: Routing ticket booking request to: {}", fullUri);
            
            return ticketBookingWebClient.get()
                    .uri(fullUri)
                    .header(HttpHeaders.AUTHORIZATION, authHeader != null ? authHeader : "")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .doOnError(error -> {
                        if (error instanceof WebClientResponseException) {
                            WebClientResponseException ex = (WebClientResponseException) error;
                            LOG.error("Gateway: Error routing to ticket booking service - Status: {}, Message: {}", 
                                    ex.getStatusCode(), ex.getMessage());
                        } else {
                            LOG.error("Gateway: Error routing to ticket booking service", error);
                        }
                    })
                    .onErrorResume(WebClientResponseException.class, ex -> Mono.error(ex));
        })
        .onErrorResume(error -> {
            LOG.error("Gateway: Error resolving ticket booking service instance", error);
            return Mono.error(error);
        });
    }

    /**
     * Route POST requests to ticket booking service
     */
    @PostMapping("/**")
    public Mono<Object> routePostTicketBookingRequest(
            ServerHttpRequest request,
            @RequestBody Mono<Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String requestPath = request.getURI().getPath();
        String pathAfterApi = requestPath.replace("/api/ticket-booking", "");
        String queryString = request.getURI().getQuery();
        
        LOG.info("Gateway: Extracted POST ticket booking path: {}, Query: {}", pathAfterApi, queryString);
        
        // Resolve service instance using LoadBalancer
        return Mono.fromCallable(() -> {
            ServiceInstance instance = loadBalancerClient.choose("ticket-booking-service");
            if (instance == null) {
                throw new RuntimeException("No available instance for ticket-booking-service");
            }
            return instance;
        })
        .subscribeOn(Schedulers.boundedElastic()) // Move blocking call to bounded elastic scheduler
        .flatMap(instance -> {
            String fullUri = String.format("http://%s:%d%s%s",
                    instance.getHost(),
                    instance.getPort(),
                    pathAfterApi,
                    queryString != null ? "?" + queryString : "");
            
            LOG.info("Gateway: Routing POST ticket booking request to: {}", fullUri);
            
            return ticketBookingWebClient.post()
                    .uri(fullUri)
                    .header(HttpHeaders.AUTHORIZATION, authHeader != null ? authHeader : "")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body, Object.class)
                    .retrieve()
                    .bodyToMono(Object.class);
        })
        .onErrorResume(error -> {
            LOG.error("Gateway: Error resolving ticket booking service instance for POST", error);
            return Mono.error(error);
        });
    }
}

