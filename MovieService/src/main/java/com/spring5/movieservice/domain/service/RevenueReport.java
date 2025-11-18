package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReport {
    private Double totalRevenue;
    private Double todayRevenue;
    private Double thisWeekRevenue;
    private Double thisMonthRevenue;
    private Map<String, Double> revenueByTheater;
    private Map<String, Double> revenueByMovie;
    private Integer totalBookings;
}

