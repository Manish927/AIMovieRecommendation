package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TheaterMovieService {

    @PostMapping(value = "/theater-movies", consumes = "application/json", produces = "application/json")
    Mono<TheaterMovie> createTheaterMovie(@RequestBody TheaterMovie theaterMovie);

    @GetMapping(value = "/theater-movies/theater/{theaterId}", produces = "application/json")
    Flux<TheaterMovie> getMoviesByTheater(@PathVariable Integer theaterId);

    @GetMapping(value = "/theater-movies/movie/{movieId}", produces = "application/json")
    Flux<TheaterMovie> getTheatersByMovie(@PathVariable Integer movieId);

    @GetMapping(value = "/theater-movies/theater/{theaterId}/movie/{movieId}", produces = "application/json")
    Flux<TheaterMovie> getTheaterMovieSchedule(@PathVariable Integer theaterId, @PathVariable Integer movieId);
}


