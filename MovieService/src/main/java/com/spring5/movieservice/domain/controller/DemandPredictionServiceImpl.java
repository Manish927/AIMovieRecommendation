package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.domain.algorithm.DemandPredictionAlgorithm;
import com.spring5.movieservice.domain.entity.BookingEntity;
import com.spring5.movieservice.domain.entity.MovieEntity;
import com.spring5.movieservice.domain.entity.TheaterMovieEntity;
import com.spring5.movieservice.domain.repository.BookingRepository;
import com.spring5.movieservice.domain.repository.MovieRepository;
import com.spring5.movieservice.domain.repository.TheaterMovieRepository;
import com.spring5.movieservice.domain.service.DemandPrediction;
import com.spring5.movieservice.domain.service.DemandPredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DemandPredictionServiceImpl implements DemandPredictionService {

    private static final Logger LOG = LoggerFactory.getLogger(DemandPredictionServiceImpl.class);

    private final TheaterMovieRepository theaterMovieRepository;
    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;
    private final DemandPredictionAlgorithm predictionAlgorithm;

    @Autowired
    public DemandPredictionServiceImpl(
            TheaterMovieRepository theaterMovieRepository,
            MovieRepository movieRepository,
            BookingRepository bookingRepository,
            DemandPredictionAlgorithm predictionAlgorithm) {
        this.theaterMovieRepository = theaterMovieRepository;
        this.movieRepository = movieRepository;
        this.bookingRepository = bookingRepository;
        this.predictionAlgorithm = predictionAlgorithm;
    }

    @Override
    public Mono<DemandPrediction> predictDemand(Integer theaterMovieId) {
        LOG.info("Predicting demand for theaterMovieId: {}", theaterMovieId);

        return theaterMovieRepository.findById(theaterMovieId)
                .flatMap(showtime -> 
                    movieRepository.findByMovieId(showtime.getMovieId())
                        .flatMap(movie -> {
                            // Get historical bookings for similar showtimes
                            LocalDateTime startDate = showtime.getShowTime().minusDays(30);
                            LocalDateTime endDate = showtime.getShowTime().plusDays(1);
                            
                            return bookingRepository.findByBookingTimeBetween(startDate, endDate)
                                    .collectList()
                                    .flatMap(historicalBookings -> {
                                        int currentBookings = showtime.getTotalSeats() - showtime.getAvailableSeats();
                                        
                                        double predictedDemand = predictionAlgorithm.predictDemand(
                                                showtime, movie, historicalBookings, currentBookings);
                                        
                                        double priceMultiplier = predictionAlgorithm.calculatePriceMultiplier(predictedDemand);
                                        
                                        DemandPrediction prediction = new DemandPrediction();
                                        prediction.setTheaterMovieId(theaterMovieId);
                                        prediction.setPredictedDemand(predictedDemand);
                                        prediction.setConfidence(0.85); // Confidence score
                                        prediction.setPredictionMethod("hybrid");
                                        prediction.setPredictedFor(LocalDateTime.now());
                                        prediction.setRecommendedPriceMultiplier(priceMultiplier);
                                        
                                        // Build factors string
                                        String factors = String.format(
                                            "{\"movieRating\":%.2f,\"timeOfDay\":\"%s\",\"dayOfWeek\":\"%s\",\"occupancy\":%.2f}",
                                            movie.getRating(),
                                            showtime.getShowTime().toLocalTime().toString(),
                                            showtime.getShowTime().getDayOfWeek().toString(),
                                            (double)(showtime.getTotalSeats() - showtime.getAvailableSeats()) / showtime.getTotalSeats()
                                        );
                                        prediction.setFactors(factors);
                                        
                                        return Mono.just(prediction);
                                    });
                        })
                );
    }

    @Override
    public Mono<List<DemandPrediction>> predictDemandBatch(String theaterMovieIds) {
        List<Integer> ids = Arrays.stream(theaterMovieIds.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        
        LOG.info("Predicting demand for batch: {}", ids);
        
        return Flux.fromIterable(ids)
                .flatMap(this::predictDemand)
                .collectList();
    }
}


