package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.domain.entity.TheaterMovieEntity;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.repository.TheaterMovieRepository;
import com.spring5.movieservice.domain.service.TheaterMovie;
import com.spring5.movieservice.domain.service.TheaterMovieMapper;
import com.spring5.movieservice.domain.service.TheaterMovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@RestController
public class TheaterMovieServiceImpl implements TheaterMovieService {

    private static final Logger LOG = LoggerFactory.getLogger(TheaterMovieServiceImpl.class);
    private final TheaterMovieRepository repository;
    private final TheaterMovieMapper mapper;

    @Autowired
    public TheaterMovieServiceImpl(TheaterMovieRepository repository, TheaterMovieMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<TheaterMovie> createTheaterMovie(TheaterMovie theaterMovie) {
        if (theaterMovie.getTheaterId() < 1) {
            throw new InvalidInputException("Invalid TheaterID: " + theaterMovie.getTheaterId());
        }
        if (theaterMovie.getMovieId() < 1) {
            throw new InvalidInputException("Invalid MovieID: " + theaterMovie.getMovieId());
        }

        TheaterMovieEntity entity = mapper.apiToEntity(theaterMovie);
        return repository.save(entity)
                .log(LOG.getName(), Level.FINE)
                .map(mapper::entityToApi);
    }

    @Override
    public Flux<TheaterMovie> getMoviesByTheater(Integer theaterId) {
        if (theaterId < 1) {
            throw new InvalidInputException("Invalid TheaterID: " + theaterId);
        }

        LOG.info("Getting movies for theater ID={}", theaterId);
        return repository.findByTheaterId(theaterId)
                .log(LOG.getName(), Level.FINE)
                .map(mapper::entityToApi);
    }

    @Override
    public Flux<TheaterMovie> getTheatersByMovie(Integer movieId) {
        if (movieId < 1) {
            throw new InvalidInputException("Invalid MovieID: " + movieId);
        }

        LOG.info("Getting theaters for movie ID={}", movieId);
        return repository.findByMovieId(movieId)
                .log(LOG.getName(), Level.FINE)
                .map(mapper::entityToApi);
    }

    @Override
    public Flux<TheaterMovie> getTheaterMovieSchedule(Integer theaterId, Integer movieId) {
        if (theaterId < 1) {
            throw new InvalidInputException("Invalid TheaterID: " + theaterId);
        }
        if (movieId < 1) {
            throw new InvalidInputException("Invalid MovieID: " + movieId);
        }

        LOG.info("Getting schedule for theater ID={} and movie ID={}", theaterId, movieId);
        return repository.findByTheaterIdAndMovieId(theaterId, movieId)
                .log(LOG.getName(), Level.FINE)
                .map(mapper::entityToApi);
    }
}


