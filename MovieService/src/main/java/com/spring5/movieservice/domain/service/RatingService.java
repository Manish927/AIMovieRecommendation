package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingService {

    @PostMapping(value = "/ratings", consumes = "application/json", produces = "application/json")
    Mono<Rating> createRating(@RequestBody Rating rating);

    @GetMapping(value = "/ratings/{ratingId}", produces = "application/json")
    Mono<Rating> getRating(@PathVariable Integer ratingId);

    @GetMapping(value = "/ratings/user/{userId}", produces = "application/json")
    Flux<Rating> getRatingsByUser(@PathVariable Integer userId);

    @GetMapping(value = "/ratings/movie/{movieId}", produces = "application/json")
    Flux<Rating> getRatingsByMovie(@PathVariable Integer movieId);

    @GetMapping(value = "/ratings", produces = "application/json")
    Flux<Rating> getAllRatings();

    @PutMapping(value = "/ratings/{ratingId}", consumes = "application/json", produces = "application/json")
    Mono<Rating> updateRating(@PathVariable Integer ratingId, @RequestBody Rating rating);

    @DeleteMapping(value = "/ratings/{ratingId}")
    Mono<Void> deleteRating(@PathVariable Integer ratingId);
}

