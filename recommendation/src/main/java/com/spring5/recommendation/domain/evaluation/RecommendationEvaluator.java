package com.spring5.recommendation.domain.evaluation;

import com.spring5.recommendation.domain.service.Recommendation;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Evaluation framework for recommendation algorithms
 * Implements metrics commonly used in MovieLens research
 */
@Component
public class RecommendationEvaluator {

    /**
     * Evaluate recommendations using Mean Absolute Error (MAE)
     * Lower is better (0 = perfect predictions)
     */
    public double calculateMAE(Map<Integer, Double> predictedRatings, Map<Integer, Integer> actualRatings) {
        if (predictedRatings.isEmpty() || actualRatings.isEmpty()) {
            return Double.MAX_VALUE;
        }

        double totalError = 0.0;
        int count = 0;

        for (Map.Entry<Integer, Integer> actual : actualRatings.entrySet()) {
            Integer movieId = actual.getKey();
            Integer actualRating = actual.getValue();
            Double predictedRating = predictedRatings.get(movieId);

            if (predictedRating != null) {
                totalError += Math.abs(predictedRating - actualRating);
                count++;
            }
        }

        return count > 0 ? totalError / count : Double.MAX_VALUE;
    }

    /**
     * Evaluate recommendations using Root Mean Squared Error (RMSE)
     * Lower is better (0 = perfect predictions)
     */
    public double calculateRMSE(Map<Integer, Double> predictedRatings, Map<Integer, Integer> actualRatings) {
        if (predictedRatings.isEmpty() || actualRatings.isEmpty()) {
            return Double.MAX_VALUE;
        }

        double totalSquaredError = 0.0;
        int count = 0;

        for (Map.Entry<Integer, Integer> actual : actualRatings.entrySet()) {
            Integer movieId = actual.getKey();
            Integer actualRating = actual.getValue();
            Double predictedRating = predictedRatings.get(movieId);

            if (predictedRating != null) {
                double error = predictedRating - actualRating;
                totalSquaredError += error * error;
                count++;
            }
        }

        return count > 0 ? Math.sqrt(totalSquaredError / count) : Double.MAX_VALUE;
    }

    /**
     * Calculate Precision@K
     * Percentage of recommended items that are relevant
     */
    public double calculatePrecisionAtK(List<Recommendation> recommendations, Set<Integer> relevantMovies, int k) {
        if (recommendations.isEmpty() || relevantMovies.isEmpty()) {
            return 0.0;
        }

        List<Recommendation> topK = recommendations.stream()
                .limit(k)
                .collect(Collectors.toList());

        long relevantCount = topK.stream()
                .map(Recommendation::getMovieId)
                .filter(relevantMovies::contains)
                .count();

        return (double) relevantCount / Math.min(k, topK.size());
    }

    /**
     * Calculate Recall@K
     * Percentage of relevant items that were recommended
     */
    public double calculateRecallAtK(List<Recommendation> recommendations, Set<Integer> relevantMovies, int k) {
        if (recommendations.isEmpty() || relevantMovies.isEmpty()) {
            return 0.0;
        }

        List<Recommendation> topK = recommendations.stream()
                .limit(k)
                .collect(Collectors.toList());

        Set<Integer> recommendedMovies = topK.stream()
                .map(Recommendation::getMovieId)
                .collect(Collectors.toSet());

        long relevantRecommended = recommendedMovies.stream()
                .filter(relevantMovies::contains)
                .count();

        return (double) relevantRecommended / relevantMovies.size();
    }

    /**
     * Calculate F1 Score@K (harmonic mean of precision and recall)
     */
    public double calculateF1AtK(List<Recommendation> recommendations, Set<Integer> relevantMovies, int k) {
        double precision = calculatePrecisionAtK(recommendations, relevantMovies, k);
        double recall = calculateRecallAtK(recommendations, relevantMovies, k);

        if (precision + recall == 0) {
            return 0.0;
        }

        return 2 * (precision * recall) / (precision + recall);
    }

    /**
     * Calculate Mean Average Precision (MAP)
     * Average of precision at each relevant item position
     */
    public double calculateMAP(List<Recommendation> recommendations, Set<Integer> relevantMovies) {
        if (recommendations.isEmpty() || relevantMovies.isEmpty()) {
            return 0.0;
        }

        double sumPrecision = 0.0;
        int relevantFound = 0;

        for (int i = 0; i < recommendations.size(); i++) {
            Recommendation rec = recommendations.get(i);
            if (relevantMovies.contains(rec.getMovieId())) {
                relevantFound++;
                double precisionAtI = (double) relevantFound / (i + 1);
                sumPrecision += precisionAtI;
            }
        }

        return relevantFound > 0 ? sumPrecision / relevantMovies.size() : 0.0;
    }

    /**
     * Calculate Coverage
     * Percentage of items that can be recommended
     */
    public double calculateCoverage(Set<Integer> recommendedMovies, Set<Integer> allMovies) {
        if (allMovies.isEmpty()) {
            return 0.0;
        }

        long recommendedCount = recommendedMovies.stream()
                .filter(allMovies::contains)
                .count();

        return (double) recommendedCount / allMovies.size();
    }

    /**
     * Calculate Diversity (average pairwise dissimilarity)
     * Higher diversity = more varied recommendations
     */
    public double calculateDiversity(List<Recommendation> recommendations, 
                                    Map<Integer, Set<String>> movieGenres) {
        if (recommendations.size() < 2) {
            return 0.0;
        }

        double totalDissimilarity = 0.0;
        int pairs = 0;

        for (int i = 0; i < recommendations.size(); i++) {
            for (int j = i + 1; j < recommendations.size(); j++) {
                Integer movieId1 = recommendations.get(i).getMovieId();
                Integer movieId2 = recommendations.get(j).getMovieId();

                Set<String> genres1 = movieGenres.getOrDefault(movieId1, Collections.emptySet());
                Set<String> genres2 = movieGenres.getOrDefault(movieId2, Collections.emptySet());

                // Jaccard dissimilarity
                Set<String> intersection = new HashSet<>(genres1);
                intersection.retainAll(genres2);
                Set<String> union = new HashSet<>(genres1);
                union.addAll(genres2);

                double dissimilarity = union.isEmpty() ? 1.0 : 1.0 - ((double) intersection.size() / union.size());
                totalDissimilarity += dissimilarity;
                pairs++;
            }
        }

        return pairs > 0 ? totalDissimilarity / pairs : 0.0;
    }

    /**
     * Comprehensive evaluation result
     */
    public static class EvaluationResult {
        private double mae;
        private double rmse;
        private double precisionAt10;
        private double recallAt10;
        private double f1At10;
        private double map;
        private double coverage;
        private double diversity;
        private int totalRecommendations;
        private int relevantItems;

        // Getters and setters
        public double getMae() { return mae; }
        public void setMae(double mae) { this.mae = mae; }

        public double getRmse() { return rmse; }
        public void setRmse(double rmse) { this.rmse = rmse; }

        public double getPrecisionAt10() { return precisionAt10; }
        public void setPrecisionAt10(double precisionAt10) { this.precisionAt10 = precisionAt10; }

        public double getRecallAt10() { return recallAt10; }
        public void setRecallAt10(double recallAt10) { this.recallAt10 = recallAt10; }

        public double getF1At10() { return f1At10; }
        public void setF1At10(double f1At10) { this.f1At10 = f1At10; }

        public double getMap() { return map; }
        public void setMap(double map) { this.map = map; }

        public double getCoverage() { return coverage; }
        public void setCoverage(double coverage) { this.coverage = coverage; }

        public double getDiversity() { return diversity; }
        public void setDiversity(double diversity) { this.diversity = diversity; }

        public int getTotalRecommendations() { return totalRecommendations; }
        public void setTotalRecommendations(int totalRecommendations) { this.totalRecommendations = totalRecommendations; }

        public int getRelevantItems() { return relevantItems; }
        public void setRelevantItems(int relevantItems) { this.relevantItems = relevantItems; }

        @Override
        public String toString() {
            return String.format(
                    "EvaluationResult{MAE=%.3f, RMSE=%.3f, Precision@10=%.3f, Recall@10=%.3f, " +
                    "F1@10=%.3f, MAP=%.3f, Coverage=%.3f, Diversity=%.3f, Recommendations=%d, Relevant=%d}",
                    mae, rmse, precisionAt10, recallAt10, f1At10, map, coverage, diversity,
                    totalRecommendations, relevantItems
            );
        }
    }
}

