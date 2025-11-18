package com.spring5.movieservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Table("theater_movies")
public class TheaterMovieEntity {

    @Id
    private Integer id;

    @NotNull(message = "Theater ID is Required")
    private Integer theaterId;

    @NotNull(message = "Movie ID is Required")
    private Integer movieId;

    @NotNull(message = "Screen Number is Required")
    private Integer screenNumber;

    @NotNull(message = "Show Time is Required")
    private LocalDateTime showTime;

    private Double ticketPrice; // Base price
    private Double dynamicPrice; // Current dynamic price
    private Double basePrice; // Original base price for reference
    private Double predictedDemand; // Predicted demand score (0.0 to 1.0)
    private Integer availableSeats;
    private Integer totalSeats;
    private LocalDateTime lastPriceUpdate; // When price was last updated
}

