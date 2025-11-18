package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DynamicPricing {
    private Integer theaterMovieId;
    private Double basePrice;
    private Double currentPrice;
    private Double priceMultiplier; // Current multiplier (e.g., 1.2 = 20% increase)
    private Double predictedDemand;
    private String pricingStrategy; // "demand_based", "time_based", "hybrid"
    private LocalDateTime lastUpdated;
    private Double revenueImpact; // Estimated revenue impact

    public DynamicPricing() {
        theaterMovieId = 0;
        basePrice = 0.0;
        currentPrice = 0.0;
        priceMultiplier = 1.0;
        predictedDemand = 0.0;
        pricingStrategy = "";
        lastUpdated = null;
        revenueImpact = 0.0;
    }
}


