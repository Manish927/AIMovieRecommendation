package com.spring5.recommendation.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    private Integer ratingId;
    private Integer userId;
    private Integer movieId;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
}


