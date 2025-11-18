package com.spring5.recommendation.domain.movielens;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a movie from MovieLens dataset
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieLensMovie {
    private Integer movieId;
    private String title;
    private String genres; // Comma-separated
    private Integer year; // Release year
}


