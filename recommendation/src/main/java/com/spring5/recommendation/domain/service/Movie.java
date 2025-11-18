package com.spring5.recommendation.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    private Integer movieId;
    private String title;
    private String description;
    private String genre;
    private String director;
    private String cast; // JSON string: ["Actor1", "Actor2"]
    private LocalDate releaseDate;
    private Integer duration;
    private String posterUrl;
    private Double rating;
    private Integer totalRatings;
    private String language;
    private String certification;
    private String serviceAddress;
}

