package com.spring5.movieservice.domain.repository;

import com.spring5.movieservice.domain.entity.RatingEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RatingRepository extends ReactiveCrudRepository<RatingEntity, Integer> {
    Mono<RatingEntity> findByRatingId(Integer ratingId);
    Flux<RatingEntity> findByUserId(Integer userId);
    Flux<RatingEntity> findByMovieId(Integer movieId);
    Mono<RatingEntity> findByUserIdAndMovieId(Integer userId, Integer movieId);
}


