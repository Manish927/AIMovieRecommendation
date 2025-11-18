package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DynamicPricingService {

    @GetMapping(value = "/dynamic-pricing/{theaterMovieId}", produces = "application/json")
    Mono<DynamicPricing> getDynamicPricing(@PathVariable Integer theaterMovieId);

    @PostMapping(value = "/dynamic-pricing/update/{theaterMovieId}", produces = "application/json")
    Mono<DynamicPricing> updatePricing(@PathVariable Integer theaterMovieId);

    @PostMapping(value = "/dynamic-pricing/update-all", produces = "application/json")
    Mono<String> updateAllPricing();

    @GetMapping(value = "/dynamic-pricing/theater/{theaterId}", produces = "application/json")
    Flux<DynamicPricing> getPricingForTheater(@PathVariable Integer theaterId);

    @GetMapping(value = "/dynamic-pricing/revenue-impact", produces = "application/json")
    Mono<Double> getTotalRevenueImpact();
}


