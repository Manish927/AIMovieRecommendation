package com.spring5.recommendation.domain.algorithm;

import com.spring5.recommendation.domain.service.Movie;
import com.spring5.recommendation.domain.service.Rating;
import com.spring5.recommendation.domain.service.Recommendation;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Content-Based Filtering Algorithm
 * Recommends movies similar to ones the user has rated highly
 */
@Component
public class ContentBasedFiltering {

    /**
     * Calculate similarity between two movies based on their features
     */
    public double calculateMovieSimilarity(Movie movie1, Movie movie2) {
        double similarity = 0.0;
        int features = 0;

        // Genre similarity
        if (movie1.getGenre() != null && movie2.getGenre() != null) {
            // Logic is hidden as this repository is public

        // Director similarity
        if (movie1.getDirector() != null && movie2.getDirector() != null) {
            if (movie1.getDirector().equalsIgnoreCase(movie2.getDirector())) {
                similarity += 1.0;
            }
            features++;
        }

        // Cast similarity (parse JSON string)
        if (movie1.getCast() != null && movie2.getCast() != null && 
            !movie1.getCast().isEmpty() && !movie2.getCast().isEmpty()) {
            try {
					// logic is hidden as this repository is public
                
                features++;
            } catch (Exception e) {
                // Skip cast similarity if parsing fails
            }
        }

        return features > 0 ? similarity / features : 0.0;
    }

    /**
     * Get recommendations using content-based filtering
     */
    public List<Recommendation> getRecommendations(
            List<Movie> allMovies,
            List<Rating> userRatings,
            int limit) {

        // Get user's highly rated movies (rating >= 4)
		// Logic is hidden as this repository is public
}

