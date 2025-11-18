package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.common.ServiceUtil;
import com.spring5.movieservice.common.TaxCalculator;
import com.spring5.movieservice.domain.entity.BookingEntity;
import com.spring5.movieservice.domain.entity.TheaterMovieEntity;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.exception.NotFoundException;
import com.spring5.movieservice.domain.repository.BookingRepository;
import com.spring5.movieservice.domain.repository.TheaterMovieRepository;
import com.spring5.movieservice.domain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.logging.Level;

@RestController
public class BookingServiceImpl implements BookingService {

    private static final Logger LOG = LoggerFactory.getLogger(BookingServiceImpl.class);
    private final BookingRepository bookingRepository;
    private final TheaterMovieRepository theaterMovieRepository;
    private final BookingMapper bookingMapper;
    private final TaxCalculator taxCalculator;
    private final ServiceUtil serviceUtil;

    @Autowired
    public BookingServiceImpl(
            BookingRepository bookingRepository,
            TheaterMovieRepository theaterMovieRepository,
            BookingMapper bookingMapper,
            TaxCalculator taxCalculator,
            ServiceUtil serviceUtil) {
        this.bookingRepository = bookingRepository;
        this.theaterMovieRepository = theaterMovieRepository;
        this.bookingMapper = bookingMapper;
        this.taxCalculator = taxCalculator;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Booking> createBooking(BookingRequest request) {
        if (request.getUserId() == null || request.getUserId() < 1) {
            return Mono.error(new InvalidInputException("Invalid User ID"));
        }
        if (request.getTheaterMovieId() == null || request.getTheaterMovieId() < 1) {
            return Mono.error(new InvalidInputException("Invalid Theater Movie ID"));
        }
        if (request.getNumberOfSeats() == null || request.getNumberOfSeats() < 1) {
            return Mono.error(new InvalidInputException("Invalid number of seats"));
        }
        if (request.getPricePerTicket() == null || request.getPricePerTicket() <= 0) {
            return Mono.error(new InvalidInputException("Invalid price per ticket"));
        }

        LOG.info("Creating booking for user: {}, theaterMovie: {}, seats: {}", 
                request.getUserId(), request.getTheaterMovieId(), request.getNumberOfSeats());

        return theaterMovieRepository.findById(request.getTheaterMovieId())
                .switchIfEmpty(Mono.error(new NotFoundException("Showtime not found: " + request.getTheaterMovieId())))
                .flatMap(theaterMovie -> {
                    // Check seat availability
                    int availableSeats = theaterMovie.getAvailableSeats() != null ? theaterMovie.getAvailableSeats() : 0;
                    if (availableSeats < request.getNumberOfSeats()) {
                        return Mono.error(new InvalidInputException("Not enough seats available. Available: " + availableSeats));
                    }

                    // Calculate base price
                    Double basePrice = request.getPricePerTicket() * request.getNumberOfSeats();
                    
                    // Apply discount if provided (simplified - in production, validate discount code)
                    Double discountAmount = 0.0;
                    if (request.getDiscountCode() != null && !request.getDiscountCode().isEmpty()) {
                        // In production, validate discount code from database
                        // For demo, apply 10% discount if code is "SAVE10"
                        if ("SAVE10".equals(request.getDiscountCode())) {
                            discountAmount = basePrice * 0.10;
                        }
                    }

                    // Calculate taxes using TaxCalculator
                    TaxCalculator.TaxCalculationResult taxResult = taxCalculator.calculateTaxesWithDiscount(basePrice, discountAmount);

                    // Create booking entity
                    BookingEntity bookingEntity = BookingEntity.builder()
                            .userId(request.getUserId())
                            .theaterMovieId(request.getTheaterMovieId())
                            .numberOfSeats(request.getNumberOfSeats())
                            .basePrice(taxResult.getBasePrice())
                            .taxAmount(taxResult.getTaxAmount())
                            .serviceCharge(taxResult.getServiceCharge())
                            .discountAmount(discountAmount)
                            .totalPrice(taxResult.getTotalPrice())
                            .pricePerTicket(request.getPricePerTicket())
                            .bookingTime(LocalDateTime.now())
                            .status("PENDING")
                            .reservationExpiresAt(LocalDateTime.now().plusMinutes(10)) // 10 minutes reservation
                            .build();

                    return bookingRepository.save(bookingEntity)
                            .flatMap(savedBooking -> {
                                // Update available seats
                                theaterMovie.setAvailableSeats(availableSeats - request.getNumberOfSeats());
                                return theaterMovieRepository.save(theaterMovie)
                                        .then(Mono.just(savedBooking));
                            })
                            .map((BookingEntity be) -> bookingMapper.entityToApi(be))
                            .log(LOG.getName(), Level.FINE);
                });
    }

    @Override
    public Mono<Booking> getBooking(Integer bookingId) {
        if (bookingId < 1) {
            throw new InvalidInputException("Invalid Booking ID: " + bookingId);
        }
        return bookingRepository.findByBookingId(bookingId)
                .switchIfEmpty(Mono.error(new NotFoundException("Booking not found: " + bookingId)))
                .map(bookingMapper::entityToApi);
    }

    @Override
    public Flux<Booking> getBookingsByUser(Integer userId) {
        if (userId < 1) {
            return Flux.error(new InvalidInputException("Invalid User ID: " + userId));
        }
        return bookingRepository.findByUserId(userId)
                .map(bookingMapper::entityToApi);
    }

    @Override
    public Mono<Booking> cancelBooking(Integer bookingId) {
        if (bookingId < 1) {
            return Mono.error(new InvalidInputException("Invalid Booking ID: " + bookingId));
        }
        
        return bookingRepository.findByBookingId(bookingId)
                .switchIfEmpty(Mono.error(new NotFoundException("Booking not found: " + bookingId)))
                .flatMap(booking -> {
                    if ("CANCELLED".equals(booking.getStatus()) || "COMPLETED".equals(booking.getStatus())) {
                        return Mono.error(new InvalidInputException("Booking cannot be cancelled"));
                    }
                    
                    booking.setStatus("CANCELLED");
                    
                    // Restore available seats
                    return theaterMovieRepository.findById(booking.getTheaterMovieId())
                            .flatMap(theaterMovie -> {
                                int currentAvailable = theaterMovie.getAvailableSeats() != null ? theaterMovie.getAvailableSeats() : 0;
                                theaterMovie.setAvailableSeats(currentAvailable + booking.getNumberOfSeats());
                                return theaterMovieRepository.save(theaterMovie);
                            })
                            .then(bookingRepository.save(booking))
                            .map(bookingMapper::entityToApi);
                })
                .log(LOG.getName(), Level.FINE);
    }
}

