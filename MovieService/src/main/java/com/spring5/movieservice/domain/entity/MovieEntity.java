package com.spring5.movieservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Table("movies")
public class MovieEntity {

    @Id
    private Integer movieId;

    @NotNull(message = "Movie Title is Required")
    private String title;
    
    private String description;
    private String genre; // Comma-separated genres: "Action,Adventure,Sci-Fi"
    private String director;
    private String cast; // JSON array stored as text: ["Actor1", "Actor2"]
    private LocalDate releaseDate;
    private Integer duration; // in minutes
    private String posterUrl;
    private Double rating; // Average rating
    private Integer totalRatings; // Number of ratings
    private String language;
    private String certification; // PG, PG-13, R, etc.
}

