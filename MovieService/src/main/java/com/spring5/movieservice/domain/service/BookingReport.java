package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingReport {
    private Integer bookingId;
    private Integer userId;
    private String userName;
    private Integer movieId;
    private String movieTitle;
    private Integer theaterId;
    private String theaterName;
    private Integer numberOfSeats;
    private Double totalPrice;
    private Double pricePerTicket;
    private LocalDateTime bookingTime;
    private String status;
}

