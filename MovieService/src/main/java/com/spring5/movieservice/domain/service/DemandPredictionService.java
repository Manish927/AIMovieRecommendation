package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface DemandPredictionService {

    @GetMapping(value = "/demand-prediction/{theaterMovieId}", produces = "application/json")
    Mono<DemandPrediction> predictDemand(@PathVariable Integer theaterMovieId);

    @GetMapping(value = "/demand-prediction/batch", produces = "application/json")
    Mono<java.util.List<DemandPrediction>> predictDemandBatch(@RequestParam String theaterMovieIds);
}


