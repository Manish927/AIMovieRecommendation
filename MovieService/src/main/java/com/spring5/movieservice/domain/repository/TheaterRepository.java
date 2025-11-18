package com.spring5.movieservice.domain.repository;

import com.spring5.movieservice.domain.entity.TheaterEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TheaterRepository extends ReactiveCrudRepository<TheaterEntity, Integer> {
    Mono<TheaterEntity> findByTheaterId(Integer theaterId);
    Flux<TheaterEntity> findByCity(String city);
}


