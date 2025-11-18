package com.spring5.movieservice.domain.repository;

import com.spring5.movieservice.domain.entity.BookingEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface BookingRepository extends ReactiveCrudRepository<BookingEntity, Integer> {
    Mono<BookingEntity> findByBookingId(Integer bookingId);
    Flux<BookingEntity> findByUserId(Integer userId);
    Flux<BookingEntity> findByTheaterMovieId(Integer theaterMovieId);
    Flux<BookingEntity> findByBookingTimeBetween(LocalDateTime start, LocalDateTime end);
    Flux<BookingEntity> findByStatus(String status);
}


