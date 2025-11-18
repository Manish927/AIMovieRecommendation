package com.spring5.movieservice.domain.algorithm;

import com.spring5.movieservice.domain.entity.BookingEntity;
import com.spring5.movieservice.domain.entity.MovieEntity;
import com.spring5.movieservice.domain.entity.TheaterMovieEntity;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demand Prediction Algorithm
 * Predicts demand for movie showtimes based on multiple factors
 */
@Component
public class DemandPredictionAlgorithm {

    /**
     * Predict demand based on multiple factors
     */
    public double predictDemand(
            TheaterMovieEntity showtime,
            MovieEntity movie,
            List<BookingEntity> historicalBookings,
            int currentBookings) {

        double demandScore = 0.0;
        int factors = 0;

        // Factor 1: Movie Popularity (Rating and Total Ratings)
        if (movie.getRating() != null && movie.getTotalRatings() != null) {
            double popularityScore = calculateMoviePopularity(movie.getRating(), movie.getTotalRatings());
            demandScore += popularityScore * 0.3; // 30% weight
            factors++;
        }

        // Factor 2: Time of Day
        double timeOfDayScore = calculateTimeOfDayScore(showtime.getShowTime());
        demandScore += timeOfDayScore * 0.25; // 25% weight
        factors++;

        // Factor 3: Day of Week
        double dayOfWeekScore = calculateDayOfWeekScore(showtime.getShowTime());
        demandScore += dayOfWeekScore * 0.2; // 20% weight
        factors++;

        // Factor 4: Historical Booking Patterns
        if (!historicalBookings.isEmpty()) {
            double historicalScore = calculateHistoricalDemand(historicalBookings, showtime);
            demandScore += historicalScore * 0.15; // 15% weight
            factors++;
        }

        // Factor 5: Current Occupancy Rate
        if (showtime.getTotalSeats() != null && showtime.getTotalSeats() > 0) {
            double occupancyRate = 1.0 - ((double) showtime.getAvailableSeats() / showtime.getTotalSeats());
            double occupancyScore = occupancyRate; // Higher occupancy = higher demand
            demandScore += occupancyScore * 0.1; // 10% weight
            factors++;
        }

        // Normalize to 0.0 - 1.0 range
        return factors > 0 ? Math.min(demandScore / factors, 1.0) : 0.5; // Default to 0.5 if no factors
    }

    /**
     * Calculate movie popularity score based on rating and number of ratings
     */
    private double calculateMoviePopularity(Double rating, Integer totalRatings) {
        if (rating == null || totalRatings == null) {
            return 0.5; // Default score
        }

        // Normalize rating (0-5 scale to 0-1 scale)
        double normalizedRating = rating / 5.0;

        // Normalize total ratings (assuming max is 10000)
        double normalizedRatings = Math.min(totalRatings / 10000.0, 1.0);

        // Weighted combination: 70% rating, 30% number of ratings
        return (normalizedRating * 0.7) + (normalizedRatings * 0.3);
    }

    /**
     * Calculate time of day score (evening shows are more popular)
     */
    private double calculateTimeOfDayScore(LocalDateTime showTime) {
        LocalTime time = showTime.toLocalTime();
        int hour = time.getHour();

        if (hour >= 18 && hour <= 22) {
            return 1.0; // Peak evening hours
        } else if (hour >= 14 && hour < 18) {
            return 0.7; // Afternoon
        } else if (hour >= 10 && hour < 14) {
            return 0.5; // Late morning/early afternoon
        } else {
            return 0.3; // Early morning or late night
        }
    }

    /**
     * Calculate day of week score (weekends are more popular)
     */
    private double calculateDayOfWeekScore(LocalDateTime showTime) {
        DayOfWeek dayOfWeek = showTime.getDayOfWeek();

        switch (dayOfWeek) {
            case FRIDAY:
            case SATURDAY:
                return 1.0; // Peak weekend
            case SUNDAY:
                return 0.9; // Sunday is also popular
            case THURSDAY:
                return 0.7; // Thursday evening
            case WEDNESDAY:
                return 0.6;
            case TUESDAY:
                return 0.5;
            case MONDAY:
                return 0.4; // Least popular
            default:
                return 0.5;
        }
    }

    /**
     * Calculate historical demand based on similar showtimes
     */
    private double calculateHistoricalDemand(List<BookingEntity> historicalBookings, TheaterMovieEntity currentShowtime) {
        if (historicalBookings.isEmpty()) {
            return 0.5; // Default if no history
        }

        // Group bookings by similar time slots
        Map<Integer, Long> bookingsByHour = historicalBookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getBookingTime().getHour(),
                        Collectors.counting()
                ));

        int currentHour = currentShowtime.getShowTime().getHour();
        long bookingsForHour = bookingsByHour.getOrDefault(currentHour, 0L);

        // Normalize based on total bookings
        long maxBookings = bookingsByHour.values().stream().mapToLong(Long::longValue).max().orElse(1L);
        
        return maxBookings > 0 ? Math.min((double) bookingsForHour / maxBookings, 1.0) : 0.5;
    }

    /**
     * Calculate price multiplier based on predicted demand
     */
    public double calculatePriceMultiplier(double predictedDemand) {
        // Price multiplier ranges from 0.8 (low demand) to 1.5 (high demand)
        // This allows for 20% discount on low demand and 50% premium on high demand
        
        if (predictedDemand < 0.3) {
            return 0.8; // 20% discount for low demand
        } else if (predictedDemand < 0.5) {
            return 0.9; // 10% discount
        } else if (predictedDemand < 0.7) {
            return 1.0; // Base price
        } else if (predictedDemand < 0.85) {
            return 1.2; // 20% premium
        } else {
            return 1.5; // 50% premium for very high demand
        }
    }

    /**
     * Estimate revenue impact of dynamic pricing
     */
    public double estimateRevenueImpact(
            double basePrice,
            double currentPrice,
            int totalSeats,
            double predictedDemand) {
        
        // Estimate bookings at base price vs current price
        int estimatedBookingsBase = (int) (totalSeats * predictedDemand * 0.9); // Assume 90% conversion at base
        int estimatedBookingsDynamic = (int) (totalSeats * predictedDemand * (0.9 - (currentPrice - basePrice) / basePrice * 0.1)); // Lower conversion with higher price
        
        double revenueBase = estimatedBookingsBase * basePrice;
        double revenueDynamic = estimatedBookingsDynamic * currentPrice;
        
        return revenueDynamic - revenueBase;
    }
}


