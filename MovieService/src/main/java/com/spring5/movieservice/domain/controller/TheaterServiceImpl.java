package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.common.ServiceUtil;
import com.spring5.movieservice.domain.entity.TheaterEntity;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.exception.NotFoundException;
import com.spring5.movieservice.domain.repository.TheaterRepository;
import com.spring5.movieservice.domain.service.Theater;
import com.spring5.movieservice.domain.service.TheaterMapper;
import com.spring5.movieservice.domain.service.TheaterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@RestController
public class TheaterServiceImpl implements TheaterService {

    private static final Logger LOG = LoggerFactory.getLogger(TheaterServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final TheaterRepository repository;
    private final TheaterMapper theaterMapper;

    @Autowired
    public TheaterServiceImpl(TheaterRepository repository, TheaterMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.theaterMapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Theater> createTheater(Theater theater) {
        if (theater.getTheaterId() != null && theater.getTheaterId() < 1) {
            throw new InvalidInputException("Invalid TheaterID: " + theater.getTheaterId());
        }

        TheaterEntity entity = theaterMapper.apiToEntity(theater);
        return repository.save(entity)
                .log(LOG.getName(), Level.FINE)
                .map(theaterMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Theater> getTheater(Integer theaterId) {
        if (theaterId < 1) {
            throw new InvalidInputException("Invalid TheaterID: " + theaterId);
        }

        LOG.info("Getting theater info for ID={}", theaterId);
        return repository.findByTheaterId(theaterId)
                .switchIfEmpty(Mono.error(new NotFoundException("No Theater found for Theater ID: " + theaterId)))
                .log(LOG.getName(), Level.FINE)
                .map(theaterMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Flux<Theater> getAllTheaters() {
        LOG.info("Getting all theaters");
        return repository.findAll()
                .log(LOG.getName(), Level.FINE)
                .map(theaterMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Flux<Theater> getTheatersByCity(String city) {
        LOG.info("Getting theaters by city: {}", city);
        return repository.findByCity(city)
                .log(LOG.getName(), Level.FINE)
                .map(theaterMapper::entityToApi)
                .map(this::setServiceAddress);
    }

    private Theater setServiceAddress(Theater theater) {
        theater.setServiceAddress(serviceUtil.getServiceAddress());
        return theater;
    }
}


