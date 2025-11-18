package com.spring5.recommendation.domain.algorithm;

import com.spring5.recommendation.domain.service.Movie;
import com.spring5.recommendation.domain.service.Rating;
import com.spring5.recommendation.domain.service.Recommendation;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Collaborative Filtering Algorithm (Enhanced with MovieLens-inspired improvements)
 * Uses user-based collaborative filtering with Pearson correlation coefficient
 * and improved similarity metrics
 */
@Component
public class CollaborativeFiltering {

    private static final int MIN_COMMON_ITEMS = 2; // Minimum movies both users must have rated (reduced from 3 for better coverage)
    private static final double MIN_SIMILARITY_THRESHOLD = 0.05; // Minimum similarity to consider (reduced from 0.1)

    /**
     * Calculate Pearson correlation coefficient (centered cosine similarity)
     * This is more accurate than cosine similarity as it accounts for user rating bias
     */
    public double calculateUserSimilarity(Map<Integer, Integer> user1Ratings, Map<Integer, Integer> user2Ratings) {
		// Logic is hidden as this repository is public. Contact To:  manish_srivastava1@yahoo.com
    }

    /**
     * Calculate cosine similarity (fallback method)
     */
    public double calculateCosineSimilarity(Map<Integer, Integer> user1Ratings, Map<Integer, Integer> user2Ratings) {
        // Logic is hidden as this repository is public. Contact To:  manish_srivastava1@yahoo.com

    }

    /**
     * Get recommendations using collaborative filtering
     */
    public List<Recommendation> getRecommendations(
            Integer userId,
            Map<Integer, Map<Integer, Integer>> allUserRatings,
            List<Movie> allMovies,
            List<Rating> userRatings,
            int limit) {

        	// Logic is hidden as this repository is public. Contact To:  manish_srivastava1@yahoo.com

  

        // Calculate weighted average scores
       
    }
}


