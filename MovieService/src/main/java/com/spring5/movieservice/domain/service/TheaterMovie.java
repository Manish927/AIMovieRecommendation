package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TheaterMovie {
    private Integer id;
    private Integer theaterId;
    private Integer movieId;
    private Integer screenNumber;
    private LocalDateTime showTime;
    private Double ticketPrice; // Base price
    private Double dynamicPrice; // Current dynamic price
    private Double basePrice; // Original base price
    private Double predictedDemand; // Predicted demand (0.0 to 1.0)
    private Integer availableSeats;
    private Integer totalSeats;
    private LocalDateTime lastPriceUpdate;

    public TheaterMovie() {
        id = 0;
        theaterId = 0;
        movieId = 0;
        screenNumber = 0;
        showTime = null;
        ticketPrice = 0.0;
        dynamicPrice = null;
        basePrice = null;
        predictedDemand = null;
        availableSeats = 0;
        totalSeats = 0;
        lastPriceUpdate = null;
    }
}

