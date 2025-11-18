package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.domain.entity.RatingEntity;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.exception.NotFoundException;
import com.spring5.movieservice.domain.repository.MovieRepository;
import com.spring5.movieservice.domain.repository.RatingRepository;
import com.spring5.movieservice.domain.service.Rating;
import com.spring5.movieservice.domain.service.RatingMapper;
import com.spring5.movieservice.domain.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.logging.Level;

@RestController
public class RatingServiceImpl implements RatingService {

    private static final Logger LOG = LoggerFactory.getLogger(RatingServiceImpl.class);
    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;
    private final RatingMapper ratingMapper;

    @Autowired
    public RatingServiceImpl(RatingRepository ratingRepository, MovieRepository movieRepository, RatingMapper ratingMapper) {
        this.ratingRepository = ratingRepository;
        this.movieRepository = movieRepository;
        this.ratingMapper = ratingMapper;
    }

    @Override
    public Mono<Rating> createRating(Rating rating) {
        if (rating.getUserId() < 1) {
            throw new InvalidInputException("Invalid UserID: " + rating.getUserId());
        }
        if (rating.getMovieId() < 1) {
            throw new InvalidInputException("Invalid MovieID: " + rating.getMovieId());
        }
        if (rating.getRating() < 1 || rating.getRating() > 5) {
            throw new InvalidInputException("Rating must be between 1 and 5");
        }

        // Check if movie exists
        return movieRepository.findByMovieId(rating.getMovieId())
                .switchIfEmpty(Mono.error(new NotFoundException("Movie not found: " + rating.getMovieId())))
                .then(ratingRepository.findByUserIdAndMovieId(rating.getUserId(), rating.getMovieId()))
                .flatMap(existing -> {
                    // Update existing rating
                    RatingEntity updated = ratingMapper.apiToEntity(rating);
                    updated.setRatingId(existing.getRatingId());
                    updated.setCreatedAt(existing.getCreatedAt());
                    return ratingRepository.save(updated);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Create new rating
                    RatingEntity entity = ratingMapper.apiToEntity(rating);
                    entity.setCreatedAt(LocalDateTime.now());
                    return ratingRepository.save(entity);
                }))
                .log(LOG.getName(), Level.FINE)
                .map(ratingMapper::entityToApi)
                .flatMap(this::updateMovieRating);
    }

    @Override
    public Mono<Rating> getRating(Integer ratingId) {
        if (ratingId < 1) {
            throw new InvalidInputException("Invalid RatingID: " + ratingId);
        }

        return ratingRepository.findByRatingId(ratingId)
                .switchIfEmpty(Mono.error(new NotFoundException("No Rating found for Rating ID: " + ratingId)))
                .log(LOG.getName(), Level.FINE)
                .map(ratingMapper::entityToApi);
    }

    @Override
    public Flux<Rating> getRatingsByUser(Integer userId) {
        if (userId < 1) {
            throw new InvalidInputException("Invalid UserID: " + userId);
        }

        return ratingRepository.findByUserId(userId)
                .log(LOG.getName(), Level.FINE)
                .map(ratingMapper::entityToApi);
    }

    @Override
    public Flux<Rating> getRatingsByMovie(Integer movieId) {
        if (movieId < 1) {
            throw new InvalidInputException("Invalid MovieID: " + movieId);
        }

        return ratingRepository.findByMovieId(movieId)
                .log(LOG.getName(), Level.FINE)
                .map(ratingMapper::entityToApi);
    }

    @Override
    public Flux<Rating> getAllRatings() {
        LOG.info("Getting all ratings");
        return ratingRepository.findAll()
                .log(LOG.getName(), Level.FINE)
                .map(ratingMapper::entityToApi);
    }

    @Override
    public Mono<Rating> updateRating(Integer ratingId, Rating rating) {
        if (ratingId < 1) {
            throw new InvalidInputException("Invalid RatingID: " + ratingId);
        }

        return ratingRepository.findByRatingId(ratingId)
                .switchIfEmpty(Mono.error(new NotFoundException("No Rating found for Rating ID: " + ratingId)))
                .map(existing -> {
                    RatingEntity updated = ratingMapper.apiToEntity(rating);
                    updated.setRatingId(ratingId);
                    updated.setCreatedAt(existing.getCreatedAt());
                    return updated;
                })
                .flatMap(ratingRepository::save)
                .log(LOG.getName(), Level.FINE)
                .map(ratingMapper::entityToApi)
                .flatMap(this::updateMovieRating);
    }

    @Override
    public Mono<Void> deleteRating(Integer ratingId) {
        if (ratingId < 1) {
            throw new InvalidInputException("Invalid RatingID: " + ratingId);
        }

        return ratingRepository.findByRatingId(ratingId)
                .log(LOG.getName(), Level.FINE)
                .flatMap(ratingRepository::delete);
    }

    private Mono<Rating> updateMovieRating(Rating rating) {
        // Update movie's average rating
        return ratingRepository.findByMovieId(rating.getMovieId())
                .collectList()
                .flatMap(ratings -> {
                    if (ratings.isEmpty()) {
                        return Mono.just(rating);
                    }
                    double avgRating = ratings.stream()
                            .mapToInt(RatingEntity::getRating)
                            .average()
                            .orElse(0.0);
                    
                    return movieRepository.findByMovieId(rating.getMovieId())
                            .flatMap(movie -> {
                                movie.setRating(avgRating);
                                movie.setTotalRatings(ratings.size());
                                return movieRepository.save(movie);
                            })
                            .then(Mono.just(rating));
                });
    }
}

