package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface UserService {

    @PostMapping( value = "/user", consumes = "application/json", produces = "application/json")
    Mono<User> createUser(@RequestBody User user);

    @GetMapping(value = "/user/{userId}", produces = "application/json")
    Mono<User> getUser(@PathVariable Integer userId);

    @DeleteMapping(value = "/user/{userID}")
    Mono<Void> deleteUser(@PathVariable Integer userID);
}
