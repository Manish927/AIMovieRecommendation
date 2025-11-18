package com.spring5.movieservice.domain.service;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {

    @PostMapping(value = "/bookings", consumes = "application/json", produces = "application/json")
    Mono<Booking> createBooking(@RequestBody BookingRequest request);

    @GetMapping(value = "/bookings/{bookingId}", produces = "application/json")
    Mono<Booking> getBooking(@PathVariable Integer bookingId);

    @GetMapping(value = "/bookings/user/{userId}", produces = "application/json")
    Flux<Booking> getBookingsByUser(@PathVariable Integer userId);

    @PutMapping(value = "/bookings/{bookingId}/cancel", produces = "application/json")
    Mono<Booking> cancelBooking(@PathVariable Integer bookingId);
}

