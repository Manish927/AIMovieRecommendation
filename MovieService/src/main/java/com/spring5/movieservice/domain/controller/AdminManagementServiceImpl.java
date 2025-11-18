package com.spring5.movieservice.domain.controller;

import com.spring5.movieservice.domain.entity.BookingEntity;
import com.spring5.movieservice.domain.entity.MovieEntity;
import com.spring5.movieservice.domain.entity.TheaterEntity;
import com.spring5.movieservice.domain.entity.TheaterMovieEntity;
import com.spring5.movieservice.domain.exception.InvalidInputException;
import com.spring5.movieservice.domain.exception.NotFoundException;
import com.spring5.movieservice.domain.repository.BookingRepository;
import com.spring5.movieservice.domain.repository.MovieRepository;
import com.spring5.movieservice.domain.repository.TheaterMovieRepository;
import com.spring5.movieservice.domain.repository.TheaterRepository;
import com.spring5.movieservice.domain.repository.UserRepository;
import com.spring5.movieservice.domain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@RestController
public class AdminManagementServiceImpl implements AdminManagementService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminManagementServiceImpl.class);
    
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final TheaterRepository theaterRepository;
    private final TheaterMapper theaterMapper;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final TheaterMovieRepository theaterMovieRepository;

    @Autowired
    public AdminManagementServiceImpl(
            MovieRepository movieRepository,
            MovieMapper movieMapper,
            TheaterRepository theaterRepository,
            TheaterMapper theaterMapper,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            TheaterMovieRepository theaterMovieRepository) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.theaterRepository = theaterRepository;
        this.theaterMapper = theaterMapper;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.theaterMovieRepository = theaterMovieRepository;
    }

    private Mono<Admin> validateAdminToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Mono.error(new InvalidInputException("Invalid authorization token"));
        }
        String actualToken = token.substring(7);
        Admin admin = AdminServiceImpl.getAdminFromToken(actualToken);
        if (admin == null) {
            return Mono.error(new InvalidInputException("Invalid or expired token"));
        }
        return Mono.just(admin);
    }

    // Movie Management
    @Override
    public Mono<Movie> createMovie(String token, Movie movie) {
        return validateAdminToken(token)
                .flatMap(admin -> {
                    LOG.info("Admin {} creating movie: {}", admin.getUsername(), movie.getTitle());
                    MovieEntity entity = movieMapper.apiToEntity(movie);
                    return movieRepository.save(entity)
                            .log(LOG.getName(), Level.FINE)
                            .map(movieMapper::entityToApi);
                });
    }

    @Override
    public Mono<Movie> updateMovie(String token, Integer movieId, Movie movie) {
        return validateAdminToken(token)
                .flatMap(admin -> {
                    LOG.info("Admin {} updating movie: {}", admin.getUsername(), movieId);
                    return movieRepository.findByMovieId(movieId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Movie not found: " + movieId)))
                            .flatMap(existing -> {
                                MovieEntity entity = movieMapper.apiToEntity(movie);
                                entity.setMovieId(movieId);
                                return movieRepository.save(entity)
                                        .map(movieMapper::entityToApi);
                            });
                });
    }

    @Override
    public Mono<Void> deleteMovie(String token, Integer movieId) {
        return validateAdminToken(token)
                .flatMap(admin -> {
                    LOG.info("Admin {} deleting movie: {}", admin.getUsername(), movieId);
                    return movieRepository.findByMovieId(movieId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Movie not found: " + movieId)))
                            .flatMap(movieRepository::delete);
                });
    }

    // Theater Management
    @Override
    public Mono<Theater> createTheater(String token, Theater theater) {
        return validateAdminToken(token)
                .flatMap(admin -> {
                    LOG.info("Admin {} creating theater: {}", admin.getUsername(), theater.getName());
                    TheaterEntity entity = theaterMapper.apiToEntity(theater);
                    return theaterRepository.save(entity)
                            .log(LOG.getName(), Level.FINE)
                            .map(theaterMapper::entityToApi);
                });
    }

    @Override
    public Mono<Theater> updateTheater(String token, Integer theaterId, Theater theater) {
        return validateAdminToken(token)
                .flatMap(admin -> {
                    LOG.info("Admin {} updating theater: {}", admin.getUsername(), theaterId);
                    return theaterRepository.findByTheaterId(theaterId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Theater not found: " + theaterId)))
                            .flatMap(existing -> {
                                TheaterEntity entity = theaterMapper.apiToEntity(theater);
                                entity.setTheaterId(theaterId);
                                return theaterRepository.save(entity)
                                        .map(theaterMapper::entityToApi);
                            });
                });
    }

    @Override
    public Mono<Void> deleteTheater(String token, Integer theaterId) {
        return validateAdminToken(token)
                .flatMap(admin -> {
                    LOG.info("Admin {} deleting theater: {}", admin.getUsername(), theaterId);
                    return theaterRepository.findByTheaterId(theaterId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Theater not found: " + theaterId)))
                            .flatMap(theaterRepository::delete);
                });
    }

    // User Management
    @Override
    public Flux<User> getAllUsers(String token) {
        return validateAdminToken(token)
                .flatMapMany(admin -> {
                    LOG.info("Admin {} fetching all users", admin.getUsername());
                    return userRepository.findAll()
                            .map(user -> {
                                User userDto = new User();
                                userDto.setUserID(user.getUserId());
                                userDto.setName(user.getName());
                                userDto.setEmail(user.getEmail());
                                userDto.setPhone(user.getPhone());
                                return userDto;
                            });
                });
    }

    @Override
    public Mono<User> getUser(String token, Integer userId) {
        return validateAdminToken(token)
                .flatMap(admin -> {
                    LOG.info("Admin {} fetching user: {}", admin.getUsername(), userId);
                    return userRepository.findByUserId(userId)
                            .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                            .map(user -> {
                                User userDto = new User();
                                userDto.setUserID(user.getUserId());
                                userDto.setName(user.getName());
                                userDto.setEmail(user.getEmail());
                                userDto.setPhone(user.getPhone());
                                return userDto;
                            });
                });
    }

    @Override
    public Mono<Void> deleteUser(String token, Integer userId) {
        return validateAdminToken(token)
                .flatMap(admin -> {
                    LOG.info("Admin {} deleting user: {}", admin.getUsername(), userId);
                    return userRepository.findByUserId(userId)
                            .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + userId)))
                            .flatMap(userRepository::delete);
                });
    }

    // Booking Reports & Analytics
    @Override
    public Flux<BookingReport> getAllBookings(String token) {
        return validateAdminToken(token)
                .flatMapMany(admin -> {
                    LOG.info("Admin {} fetching all bookings", admin.getUsername());
                    return bookingRepository.findAll()
                            .flatMap(booking -> {
                                BookingReport report = new BookingReport();
                                report.setBookingId(booking.getBookingId());
                                report.setUserId(booking.getUserId());
                                report.setNumberOfSeats(booking.getNumberOfSeats());
                                report.setTotalPrice(booking.getTotalPrice());
                                report.setPricePerTicket(booking.getPricePerTicket());
                                report.setBookingTime(booking.getBookingTime());
                                report.setStatus(booking.getStatus());

                                // Fetch related data
                                Mono<String> userName = userRepository.findByUserId(booking.getUserId())
                                        .map(user -> user.getName())
                                        .defaultIfEmpty("Unknown");
                                
                                Mono<TheaterMovieEntity> theaterMovie = theaterMovieRepository.findById(booking.getTheaterMovieId())
                                        .defaultIfEmpty(TheaterMovieEntity.builder()
                                                .id(0)
                                                .theaterId(0)
                                                .movieId(0)
                                                .screenNumber(0)
                                                .showTime(LocalDateTime.now())
                                                .ticketPrice(0.0)
                                                .dynamicPrice(0.0)
                                                .basePrice(0.0)
                                                .predictedDemand(0.0)
                                                .availableSeats(0)
                                                .totalSeats(0)
                                                .lastPriceUpdate(LocalDateTime.now())
                                                .build());

                                return Mono.zip(userName, theaterMovie)
                                        .flatMap(tuple -> {
                                            report.setUserName(tuple.getT1());
                                            TheaterMovieEntity tm = tuple.getT2();
                                            
                                            Mono<String> movieTitle = movieRepository.findByMovieId(tm.getMovieId())
                                                    .map(MovieEntity::getTitle)
                                                    .defaultIfEmpty("Unknown");
                                            
                                            Mono<String> theaterName = theaterRepository.findByTheaterId(tm.getTheaterId())
                                                    .map(TheaterEntity::getName)
                                                    .defaultIfEmpty("Unknown");

                                            return Mono.zip(movieTitle, theaterName)
                                                    .map(t -> {
                                                        report.setMovieId(tm.getMovieId());
                                                        report.setMovieTitle(t.getT1());
                                                        report.setTheaterId(tm.getTheaterId());
                                                        report.setTheaterName(t.getT2());
                                                        return report;
                                                    });
                                        });
                            });
                });
    }

    @Override
    public Mono<BookingStats> getBookingStats(String token) {
        return validateAdminToken(token)
                .flatMap(admin -> {
                    LOG.info("Admin {} fetching booking stats", admin.getUsername());
                    return bookingRepository.findAll()
                            .collectList()
                            .map(bookings -> {
                                BookingStats stats = new BookingStats();
                                stats.setTotalBookings((long) bookings.size());
                                stats.setConfirmedBookings(bookings.stream()
                                        .filter(b -> "CONFIRMED".equals(b.getStatus()))
                                        .count());
                                stats.setCancelledBookings(bookings.stream()
                                        .filter(b -> "CANCELLED".equals(b.getStatus()))
                                        .count());
                                
                                double totalRevenue = bookings.stream()
                                        .mapToDouble(BookingEntity::getTotalPrice)
                                        .sum();
                                stats.setTotalRevenue(totalRevenue);
                                
                                if (!bookings.isEmpty()) {
                                    stats.setAverageBookingValue(totalRevenue / bookings.size());
                                } else {
                                    stats.setAverageBookingValue(0.0);
                                }
                                
                                stats.setTotalSeatsBooked(bookings.stream()
                                        .mapToLong(BookingEntity::getNumberOfSeats)
                                        .sum());
                                
                                return stats;
                            });
                });
    }

    @Override
    public Mono<RevenueReport> getRevenueReport(String token) {
        return validateAdminToken(token)
                .flatMap(admin -> {
                    LOG.info("Admin {} fetching revenue report", admin.getUsername());
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
                    LocalDateTime weekStart = now.minus(7, ChronoUnit.DAYS);
                    LocalDateTime monthStart = now.minus(30, ChronoUnit.DAYS);

                    return bookingRepository.findAll()
                            .collectList()
                            .map(bookings -> {
                                RevenueReport report = new RevenueReport();
                                
                                double totalRevenue = bookings.stream()
                                        .mapToDouble(BookingEntity::getTotalPrice)
                                        .sum();
                                report.setTotalRevenue(totalRevenue);
                                
                                double todayRevenue = bookings.stream()
                                        .filter(b -> b.getBookingTime() != null && 
                                                   b.getBookingTime().isAfter(todayStart))
                                        .mapToDouble(BookingEntity::getTotalPrice)
                                        .sum();
                                report.setTodayRevenue(todayRevenue);
                                
                                double weekRevenue = bookings.stream()
                                        .filter(b -> b.getBookingTime() != null && 
                                                   b.getBookingTime().isAfter(weekStart))
                                        .mapToDouble(BookingEntity::getTotalPrice)
                                        .sum();
                                report.setThisWeekRevenue(weekRevenue);
                                
                                double monthRevenue = bookings.stream()
                                        .filter(b -> b.getBookingTime() != null && 
                                                   b.getBookingTime().isAfter(monthStart))
                                        .mapToDouble(BookingEntity::getTotalPrice)
                                        .sum();
                                report.setThisMonthRevenue(monthRevenue);
                                
                                report.setTotalBookings(bookings.size());
                                
                                // Revenue by theater and movie (simplified)
                                Map<String, Double> revenueByTheater = new HashMap<>();
                                Map<String, Double> revenueByMovie = new HashMap<>();
                                report.setRevenueByTheater(revenueByTheater);
                                report.setRevenueByMovie(revenueByMovie);
                                
                                return report;
                            });
                });
    }
}

