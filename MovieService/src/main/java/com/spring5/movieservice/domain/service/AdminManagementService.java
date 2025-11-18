package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminManagementService {

    // Movie Management
    @PostMapping(value = "/admin/movies", consumes = "application/json", produces = "application/json")
    Mono<Movie> createMovie(@RequestHeader("Authorization") String token, @RequestBody Movie movie);

    @PutMapping(value = "/admin/movies/{movieId}", consumes = "application/json", produces = "application/json")
    Mono<Movie> updateMovie(@RequestHeader("Authorization") String token, @PathVariable Integer movieId, @RequestBody Movie movie);

    @DeleteMapping(value = "/admin/movies/{movieId}")
    Mono<Void> deleteMovie(@RequestHeader("Authorization") String token, @PathVariable Integer movieId);

    // Theater Management
    @PostMapping(value = "/admin/theaters", consumes = "application/json", produces = "application/json")
    Mono<Theater> createTheater(@RequestHeader("Authorization") String token, @RequestBody Theater theater);

    @PutMapping(value = "/admin/theaters/{theaterId}", consumes = "application/json", produces = "application/json")
    Mono<Theater> updateTheater(@RequestHeader("Authorization") String token, @PathVariable Integer theaterId, @RequestBody Theater theater);

    @DeleteMapping(value = "/admin/theaters/{theaterId}")
    Mono<Void> deleteTheater(@RequestHeader("Authorization") String token, @PathVariable Integer theaterId);

    // User Management
    @GetMapping(value = "/admin/users", produces = "application/json")
    Flux<com.spring5.movieservice.domain.service.User> getAllUsers(@RequestHeader("Authorization") String token);

    @GetMapping(value = "/admin/users/{userId}", produces = "application/json")
    Mono<com.spring5.movieservice.domain.service.User> getUser(@RequestHeader("Authorization") String token, @PathVariable Integer userId);

    @DeleteMapping(value = "/admin/users/{userId}")
    Mono<Void> deleteUser(@RequestHeader("Authorization") String token, @PathVariable Integer userId);

    // Booking Reports & Analytics
    @GetMapping(value = "/admin/bookings", produces = "application/json")
    Flux<BookingReport> getAllBookings(@RequestHeader("Authorization") String token);

    @GetMapping(value = "/admin/bookings/stats", produces = "application/json")
    Mono<BookingStats> getBookingStats(@RequestHeader("Authorization") String token);

    @GetMapping(value = "/admin/analytics/revenue", produces = "application/json")
    Mono<RevenueReport> getRevenueReport(@RequestHeader("Authorization") String token);
}

