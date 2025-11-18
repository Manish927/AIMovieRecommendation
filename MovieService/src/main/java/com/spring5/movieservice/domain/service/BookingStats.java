package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingStats {
    private Long totalBookings;
    private Long confirmedBookings;
    private Long cancelledBookings;
    private Double totalRevenue;
    private Double averageBookingValue;
    private Long totalSeatsBooked;
}

