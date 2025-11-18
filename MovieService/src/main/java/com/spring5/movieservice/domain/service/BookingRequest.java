package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private Integer userId;
    private Integer theaterMovieId;
    private Integer numberOfSeats;
    private Double pricePerTicket;
    private String discountCode; // Optional discount code
}

