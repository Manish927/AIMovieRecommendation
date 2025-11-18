package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TheaterService {

    @PostMapping(value = "/theaters", consumes = "application/json", produces = "application/json")
    Mono<Theater> createTheater(@RequestBody Theater theater);

    @GetMapping(value = "/theaters/{theaterId}", produces = "application/json")
    Mono<Theater> getTheater(@PathVariable Integer theaterId);

    @GetMapping(value = "/theaters", produces = "application/json")
    Flux<Theater> getAllTheaters();

    @GetMapping(value = "/theaters/city/{city}", produces = "application/json")
    Flux<Theater> getTheatersByCity(@PathVariable String city);
}


