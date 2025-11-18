package com.spring5.recommendation.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:80", "http://localhost"})
public interface RecommendationService {

    @GetMapping(value = "/recommendations/user/{userId}", produces = "application/json")
    Flux<Recommendation> getRecommendationsForUser(@PathVariable Integer userId, @RequestParam(defaultValue = "10") Integer limit);

    @GetMapping(value = "/recommendations/user/{userId}/collaborative", produces = "application/json")
    Flux<Recommendation> getCollaborativeFilteringRecommendations(@PathVariable Integer userId, @RequestParam(defaultValue = "10") Integer limit);

    @GetMapping(value = "/recommendations/user/{userId}/content-based", produces = "application/json")
    Flux<Recommendation> getContentBasedRecommendations(@PathVariable Integer userId, @RequestParam(defaultValue = "10") Integer limit);

    @GetMapping(value = "/recommendations/user/{userId}/hybrid", produces = "application/json")
    Flux<Recommendation> getHybridRecommendations(@PathVariable Integer userId, @RequestParam(defaultValue = "10") Integer limit);
}


