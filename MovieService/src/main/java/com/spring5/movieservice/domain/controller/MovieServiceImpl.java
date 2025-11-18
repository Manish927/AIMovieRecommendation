package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.common.ServiceUtil;
import com.spring5.movieservice.domain.entity.MovieEntity;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.exception.NotFoundException;
import com.spring5.movieservice.domain.repository.MovieRepository;
import com.spring5.movieservice.domain.service.Movie;
import com.spring5.movieservice.domain.service.MovieMapper;
import com.spring5.movieservice.domain.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@RestController
public class MovieServiceImpl implements MovieService {

    private static final Logger LOG = LoggerFactory.getLogger(MovieServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final MovieRepository repository;
    private final MovieMapper movieMapper;

    @Autowired
    public MovieServiceImpl(MovieRepository repository, MovieMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.movieMapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Movie> createMovie(Movie movie) {
        if (movie.getMovieId() != null && movie.getMovieId() < 1) {
            throw new InvalidInputException("Invalid MovieID: " + movie.getMovieId());
        }

        MovieEntity entity = movieMapper.apiToEntity(movie);
        return repository.save(entity)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(DuplicateKeyException.class, 
                    ex -> new InvalidInputException("Duplicate Key, Movie ID: " + movie.getMovieId()))
                .map(movieMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Movie> getMovie(Integer movieId) {
        if (movieId < 1) {
            throw new InvalidInputException("Invalid MovieID: " + movieId);
        }

        LOG.info("Getting movie info for ID={}", movieId);
        return repository.findByMovieId(movieId)
                .switchIfEmpty(Mono.error(new NotFoundException("No Movie found for Movie ID: " + movieId)))
                .log(LOG.getName(), Level.FINE)
                .map(movieMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Flux<Movie> getAllMovies() {
        LOG.info("Getting all movies");
        return repository.findAll()
                .log(LOG.getName(), Level.FINE)
                .map(movieMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Flux<Movie> getMoviesByGenre(String genre) {
        LOG.info("Getting movies by genre: {}", genre);
        return repository.findByGenreContaining(genre)
                .log(LOG.getName(), Level.FINE)
                .map(movieMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Flux<Movie> searchMovies(String query) {
        LOG.info("Searching movies with query: {}", query);
        return repository.findByTitleContainingIgnoreCase(query)
                .log(LOG.getName(), Level.FINE)
                .map(movieMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Flux<Movie> getTopRatedMovies() {
        LOG.info("Getting top rated movies");
        return repository.findAllByOrderByRatingDesc()
                .log(LOG.getName(), Level.FINE)
                .map(movieMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Movie> updateMovie(Integer movieId, Movie movie) {
        if (movieId < 1) {
            throw new InvalidInputException("Invalid MovieID: " + movieId);
        }

        LOG.info("Updating movie with ID={}", movieId);
        return repository.findByMovieId(movieId)
                .switchIfEmpty(Mono.error(new NotFoundException("No Movie found for Movie ID: " + movieId)))
                .map(existing -> {
                    MovieEntity updated = movieMapper.apiToEntity(movie);
                    updated.setMovieId(movieId);
                    return updated;
                })
                .flatMap(repository::save)
                .log(LOG.getName(), Level.FINE)
                .map(movieMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Void> deleteMovie(Integer movieId) {
        if (movieId < 1) {
            throw new InvalidInputException("Invalid MovieID: " + movieId);
        }

        LOG.debug("deleteMovie: tried to delete an entity with movieId: {}", movieId);
        return repository.findByMovieId(movieId)
                .log(LOG.getName(), Level.FINE)
                .flatMap(repository::delete);
    }

    private Movie setServiceAddress(Movie movie) {
        movie.setServiceAddress(serviceUtil.getServiceAddress());
        if (movie.getPosterUrl() == null || movie.getPosterUrl().isEmpty()) {
            // Fallback placeholder poster
            movie.setPosterUrl("https://via.placeholder.com/300x450?text=" + movie.getTitle().replace(' ', '+'));
        }
        return movie;
    }
}


