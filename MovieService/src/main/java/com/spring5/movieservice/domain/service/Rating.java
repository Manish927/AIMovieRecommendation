package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Rating {
    private Integer ratingId;
    private Integer userId;
    private Integer movieId;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;

    public Rating() {
        ratingId = 0;
        userId = 0;
        movieId = 0;
        rating = 0;
        review = null;
        createdAt = null;
    }
}


