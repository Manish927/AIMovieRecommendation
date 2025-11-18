package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieService {

    @PostMapping(value = "/movies", consumes = "application/json", produces = "application/json")
    Mono<Movie> createMovie(@RequestBody Movie movie);

    @GetMapping(value = "/movies/{movieId}", produces = "application/json")
    Mono<Movie> getMovie(@PathVariable Integer movieId);

    @GetMapping(value = "/movies", produces = "application/json")
    Flux<Movie> getAllMovies();

    @GetMapping(value = "/movies/genre/{genre}", produces = "application/json")
    Flux<Movie> getMoviesByGenre(@PathVariable String genre);

    @GetMapping(value = "/movies/search", produces = "application/json")
    Flux<Movie> searchMovies(@RequestParam String query);

    @GetMapping(value = "/movies/top-rated", produces = "application/json")
    Flux<Movie> getTopRatedMovies();

    @PutMapping(value = "/movies/{movieId}", consumes = "application/json", produces = "application/json")
    Mono<Movie> updateMovie(@PathVariable Integer movieId, @RequestBody Movie movie);

    @DeleteMapping(value = "/movies/{movieId}")
    Mono<Void> deleteMovie(@PathVariable Integer movieId);
}


