package com.spring5.movieservice.domain.repository;

import com.spring5.movieservice.domain.entity.TheaterMovieEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TheaterMovieRepository extends ReactiveCrudRepository<TheaterMovieEntity, Integer> {
    Flux<TheaterMovieEntity> findByTheaterId(Integer theaterId);
    Flux<TheaterMovieEntity> findByMovieId(Integer movieId);
    Flux<TheaterMovieEntity> findByTheaterIdAndMovieId(Integer theaterId, Integer movieId);
    Flux<TheaterMovieEntity> findByShowTimeAfter(LocalDateTime showTime);
}


