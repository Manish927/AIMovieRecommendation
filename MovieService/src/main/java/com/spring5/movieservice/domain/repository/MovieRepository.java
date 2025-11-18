package com.spring5.movieservice.domain.repository;

import com.spring5.movieservice.domain.entity.MovieEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieRepository extends ReactiveCrudRepository<MovieEntity, Integer> {
    Mono<MovieEntity> findByMovieId(Integer movieId);
    Flux<MovieEntity> findByGenreContaining(String genre);
    Flux<MovieEntity> findByTitleContainingIgnoreCase(String title);
    Flux<MovieEntity> findAllByOrderByRatingDesc();
}


