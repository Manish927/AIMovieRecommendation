package com.spring5.recommendation.domain.movielens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a rating from MovieLens dataset
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieLensRating {
    private Integer userId;
    private Integer movieId;
    private Integer rating; // 1-5 scale
    private Long timestamp; // Unix timestamp (optional)
}


