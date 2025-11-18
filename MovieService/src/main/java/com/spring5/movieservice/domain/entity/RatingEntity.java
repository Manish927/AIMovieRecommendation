package com.spring5.movieservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Table("ratings")
public class RatingEntity {

    @Id
    private Integer ratingId;

    @NotNull(message = "User ID is Required")
    private Integer userId;

    @NotNull(message = "Movie ID is Required")
    private Integer movieId;

    @NotNull(message = "Rating is Required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String review; // Optional review text
    private LocalDateTime createdAt;
}


