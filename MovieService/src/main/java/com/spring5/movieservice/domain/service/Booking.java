package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private Integer bookingId;
    private Integer userId;
    private Integer theaterMovieId;
    private Integer numberOfSeats;
    private Double basePrice;
    private Double taxAmount;
    private Double serviceCharge;
    private Double discountAmount;
    private Double totalPrice;
    private Double pricePerTicket;
    private LocalDateTime bookingTime;
    private String status;
    private LocalDateTime reservationExpiresAt;
    private String serviceAddress;
}

