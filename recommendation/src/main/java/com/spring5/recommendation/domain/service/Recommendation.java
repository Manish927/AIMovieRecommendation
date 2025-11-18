package com.spring5.recommendation.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Recommendation {
    private Integer movieId;
    private String title;
    private Double score; // Recommendation score (0.0 to 1.0)
    private String reason; // Why this movie is recommended
    private String algorithm; // Which algorithm was used: "collaborative", "content-based", "hybrid"

    public Recommendation() {
        movieId = 0;
        title = null;
        score = 0.0;
        reason = null;
        algorithm = null;
    }
}


