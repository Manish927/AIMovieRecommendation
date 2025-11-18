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
@RequestMapping("/api/recommendations")
public class RecommendationGatewayController {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationGatewayController.class);

    private final WebClient recommendationWebClient;
    private final LoadBalancerClient loadBalancerClient;

    @Autowired
    public RecommendationGatewayController(
            @Qualifier("recommendationWebClient") WebClient recommendationWebClient,
            LoadBalancerClient loadBalancerClient) {
        this.recommendationWebClient = recommendationWebClient;
        this.loadBalancerClient = loadBalancerClient;
    }

    /**
     * Route GET requests to recommendation service
     * Example: /api/recommendations/user/4/hybrid?limit=10
     * Routes to: http://recommendation-service:8083/recommendations/user/4/hybrid?limit=10
     */
    @GetMapping("/**")
    public Mono<Object> routeRecommendationRequest(
            ServerHttpRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Extract path after /api/recommendations
        String requestPath = request.getURI().getPath();
        String pathAfterApi = requestPath.replace("/api/recommendations", "");
        String queryString = request.getURI().getQuery();
        // Add /recommendations prefix since the service expects /recommendations/user/...
        String fullPath = "/recommendations" + pathAfterApi;
        
        LOG.info("Gateway: Extracted path: {}, Query: {}", fullPath, queryString);
        
        // Resolve service instance using LoadBalancer (wrap blocking call in reactive context)
        // Use subscribeOn to move blocking call to a separate thread pool
        return Mono.fromCallable(() -> {
            ServiceInstance instance = loadBalancerClient.choose("recommendation-service");
            if (instance == null) {
                throw new RuntimeException("No available instance for recommendation-service");
            }
            LOG.debug("Gateway: Selected instance - Host: {}, Port: {}", instance.getHost(), instance.getPort());
            return instance;
        })
        .subscribeOn(Schedulers.boundedElastic()) // Move blocking call to bounded elastic scheduler
        .flatMap(instance -> {
            // Construct full URI manually using resolved instance
            String fullUri = String.format("http://%s:%d%s%s",
                    instance.getHost(),
                    instance.getPort(),
                    fullPath,
                    queryString != null ? "?" + queryString : "");
            
            LOG.info("Gateway: Routing recommendation request to: {}", fullUri);
            
            return recommendationWebClient.get()
                    .uri(fullUri)
                    .header(HttpHeaders.AUTHORIZATION, authHeader != null ? authHeader : "")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .doOnSuccess(result -> LOG.debug("Gateway: Successfully routed recommendation request"))
                    .doOnError(error -> {
                        if (error instanceof WebClientResponseException) {
                            WebClientResponseException ex = (WebClientResponseException) error;
                            LOG.error("Gateway: Error routing to recommendation service - Status: {}, Message: {}", 
                                    ex.getStatusCode(), ex.getMessage());
                        } else {
                            LOG.error("Gateway: Error routing to recommendation service", error);
                        }
                    })
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        // Forward the error response
                        return Mono.error(ex);
                    });
        })
        .onErrorResume(error -> {
            LOG.error("Gateway: Error resolving service instance", error);
            return Mono.error(error);
        });
    }

    /**
     * Route POST requests to recommendation service (if needed)
     */
    @PostMapping("/**")
    public Mono<Object> routePostRecommendationRequest(
            ServerHttpRequest request,
            @RequestBody Mono<Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        String requestPath = request.getURI().getPath();
        String pathAfterApi = requestPath.replace("/api/recommendations", "");
        String queryString = request.getURI().getQuery();
        // Add /recommendations prefix since the service expects /recommendations/...
        String fullPath = "/recommendations" + pathAfterApi;
        
        LOG.info("Gateway: Extracted POST path: {}, Query: {}", fullPath, queryString);
        
        // Resolve service instance using LoadBalancer
        return Mono.fromCallable(() -> {
            ServiceInstance instance = loadBalancerClient.choose("recommendation-service");
            if (instance == null) {
                throw new RuntimeException("No available instance for recommendation-service");
            }
            return instance;
        })
        .subscribeOn(Schedulers.boundedElastic()) // Move blocking call to bounded elastic scheduler
        .flatMap(instance -> {
            String fullUri = String.format("http://%s:%d%s%s",
                    instance.getHost(),
                    instance.getPort(),
                    fullPath,
                    queryString != null ? "?" + queryString : "");
            
            LOG.info("Gateway: Routing POST recommendation request to: {}", fullUri);
            
            return recommendationWebClient.post()
                    .uri(fullUri)
                    .header(HttpHeaders.AUTHORIZATION, authHeader != null ? authHeader : "")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body, Object.class)
                    .retrieve()
                    .bodyToMono(Object.class);
        })
        .onErrorResume(error -> {
            LOG.error("Gateway: Error resolving service instance for POST", error);
            return Mono.error(error);
        });
    }
}

