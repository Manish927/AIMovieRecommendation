package com.spring5.movieservice.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
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

    public Movie() {
        movieId = 0;
        title = null;
        description = null;
        genre = null;
        director = null;
        cast = "";
        releaseDate = null;
        duration = 0;
        posterUrl = null;
        rating = 0.0;
        totalRatings = 0;
        language = null;
        certification = null;
        serviceAddress = null;
    }
}

