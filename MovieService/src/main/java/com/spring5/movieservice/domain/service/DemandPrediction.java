package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DemandPrediction {
    private Integer theaterMovieId;
    private Double predictedDemand; // 0.0 to 1.0
    private Double confidence; // Prediction confidence (0.0 to 1.0)
    private String predictionMethod; // "time_series", "historical", "hybrid"
    private LocalDateTime predictedFor;
    private String factors; // JSON string of factors considered
    private Double recommendedPriceMultiplier; // Suggested price multiplier

    public DemandPrediction() {
        theaterMovieId = 0;
        predictedDemand = 0.0;
        confidence = 0.0;
        predictionMethod = "";
        predictedFor = null;
        factors = "";
        recommendedPriceMultiplier = 1.0;
    }
}


