package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.domain.algorithm.DemandPredictionAlgorithm;
import com.spring5.movieservice.domain.entity.BookingEntity;
import com.spring5.movieservice.domain.entity.MovieEntity;
import com.spring5.movieservice.domain.entity.TheaterMovieEntity;
import com.spring5.movieservice.domain.repository.BookingRepository;
import com.spring5.movieservice.domain.repository.MovieRepository;
import com.spring5.movieservice.domain.repository.TheaterMovieRepository;
import com.spring5.movieservice.domain.service.DynamicPricing;
import com.spring5.movieservice.domain.service.DynamicPricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class DynamicPricingServiceImpl implements DynamicPricingService {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicPricingServiceImpl.class);

    private final TheaterMovieRepository theaterMovieRepository;
    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;
    private final DemandPredictionAlgorithm predictionAlgorithm;

    @Autowired
    public DynamicPricingServiceImpl(
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
    public Mono<DynamicPricing> getDynamicPricing(Integer theaterMovieId) {
        LOG.info("Getting dynamic pricing for theaterMovieId: {}", theaterMovieId);

        return theaterMovieRepository.findById(theaterMovieId)
                .flatMap(showtime -> {
                    double basePrice = showtime.getBasePrice() != null ? showtime.getBasePrice() : showtime.getTicketPrice();
                    double currentPrice = showtime.getDynamicPrice() != null ? showtime.getDynamicPrice() : basePrice;
                    double multiplier = basePrice > 0 ? currentPrice / basePrice : 1.0;

                    DynamicPricing pricing = new DynamicPricing();
                    pricing.setTheaterMovieId(theaterMovieId);
                    pricing.setBasePrice(basePrice);
                    pricing.setCurrentPrice(currentPrice);
                    pricing.setPriceMultiplier(multiplier);
                    pricing.setPredictedDemand(showtime.getPredictedDemand() != null ? showtime.getPredictedDemand() : 0.5);
                    pricing.setPricingStrategy("demand_based");
                    pricing.setLastUpdated(showtime.getLastPriceUpdate());

                    // Calculate revenue impact
                    if (showtime.getTotalSeats() != null && showtime.getTotalSeats() > 0) {
                        double revenueImpact = predictionAlgorithm.estimateRevenueImpact(
                                basePrice, currentPrice, showtime.getTotalSeats(), pricing.getPredictedDemand());
                        pricing.setRevenueImpact(revenueImpact);
                    }

                    return Mono.just(pricing);
                });
    }

    @Override
    public Mono<DynamicPricing> updatePricing(Integer theaterMovieId) {
        LOG.info("Updating pricing for theaterMovieId: {}", theaterMovieId);

        return theaterMovieRepository.findById(theaterMovieId)
                .flatMap(showtime -> 
                    movieRepository.findByMovieId(showtime.getMovieId())
                        .flatMap(movie -> {
                            // Get historical bookings
                            LocalDateTime startDate = showtime.getShowTime().minusDays(30);
                            LocalDateTime endDate = showtime.getShowTime().plusDays(1);
                            
                            return bookingRepository.findByBookingTimeBetween(startDate, endDate)
                                    .collectList()
                                    .flatMap(historicalBookings -> {
                                        int currentBookings = showtime.getTotalSeats() - showtime.getAvailableSeats();
                                        
                                        double predictedDemand = predictionAlgorithm.predictDemand(
                                                showtime, movie, historicalBookings, currentBookings);
                                        
                                        double priceMultiplier = predictionAlgorithm.calculatePriceMultiplier(predictedDemand);
                                        
                                        double basePrice = showtime.getBasePrice() != null ? showtime.getBasePrice() : showtime.getTicketPrice();
                                        double newPrice = basePrice * priceMultiplier;

                                        // Update showtime with new pricing
                                        showtime.setDynamicPrice(newPrice);
                                        showtime.setPredictedDemand(predictedDemand);
                                        showtime.setLastPriceUpdate(LocalDateTime.now());
                                        if (showtime.getBasePrice() == null) {
                                            showtime.setBasePrice(basePrice);
                                        }

                                        return theaterMovieRepository.save(showtime)
                                                .flatMap(updated -> {
                                                    DynamicPricing pricing = new DynamicPricing();
                                                    pricing.setTheaterMovieId(theaterMovieId);
                                                    pricing.setBasePrice(basePrice);
                                                    pricing.setCurrentPrice(newPrice);
                                                    pricing.setPriceMultiplier(priceMultiplier);
                                                    pricing.setPredictedDemand(predictedDemand);
                                                    pricing.setPricingStrategy("demand_based");
                                                    pricing.setLastUpdated(LocalDateTime.now());

                                                    if (updated.getTotalSeats() != null && updated.getTotalSeats() > 0) {
                                                        double revenueImpact = predictionAlgorithm.estimateRevenueImpact(
                                                                basePrice, newPrice, updated.getTotalSeats(), predictedDemand);
                                                        pricing.setRevenueImpact(revenueImpact);
                                                    }

                                                    return Mono.just(pricing);
                                                });
                                    });
                        })
                );
    }

    @Override
    public Mono<String> updateAllPricing() {
        LOG.info("Updating pricing for all showtimes");

        return theaterMovieRepository.findAll()
                .filter(showtime -> showtime.getShowTime().isAfter(LocalDateTime.now()))
                .flatMap(showtime -> updatePricing(showtime.getId()))
                .collectList()
                .map(updated -> String.format("Updated pricing for %d showtimes", updated.size()));
    }

    @Override
    public Flux<DynamicPricing> getPricingForTheater(Integer theaterId) {
        LOG.info("Getting pricing for theaterId: {}", theaterId);

        return theaterMovieRepository.findByTheaterId(theaterId)
                .flatMap(showtime -> getDynamicPricing(showtime.getId()));
    }

    @Override
    public Mono<Double> getTotalRevenueImpact() {
        LOG.info("Calculating total revenue impact");

        return theaterMovieRepository.findAll()
                .filter(showtime -> showtime.getShowTime().isAfter(LocalDateTime.now()))
                .flatMap(showtime -> getDynamicPricing(showtime.getId()))
                .map(DynamicPricing::getRevenueImpact)
                .reduce(0.0, Double::sum);
    }
}

