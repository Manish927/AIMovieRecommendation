package com.spring5.recommendation.domain.controller;

import com.spring5.recommendation.domain.algorithm.CollaborativeFiltering;
import com.spring5.recommendation.domain.algorithm.ContentBasedFiltering;
import com.spring5.recommendation.domain.service.Movie;
import com.spring5.recommendation.domain.service.Rating;
import com.spring5.recommendation.domain.service.Recommendation;
import com.spring5.recommendation.domain.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final WebClient webClient;
    private final CollaborativeFiltering collaborativeFiltering;
    private final ContentBasedFiltering contentBasedFiltering;

    @Value("${movie.service.url:http://localhost:8081}")
    private String movieServiceUrl;

    @Autowired
    public RecommendationServiceImpl(
            WebClient.Builder webClientBuilder,
            CollaborativeFiltering collaborativeFiltering,
            ContentBasedFiltering contentBasedFiltering) {
        this.webClient = webClientBuilder.build();
        this.collaborativeFiltering = collaborativeFiltering;
        this.contentBasedFiltering = contentBasedFiltering;
    }

    @Override
    public Flux<Recommendation> getRecommendationsForUser(Integer userId, Integer limit) {
        LOG.info("Getting hybrid recommendations for user: {}", userId);
        return getHybridRecommendations(userId, limit);
    }

    @Override
    public Flux<Recommendation> getCollaborativeFilteringRecommendations(Integer userId, Integer limit) {
        LOG.info("Getting collaborative filtering recommendations for user: {}", userId);
        
        return fetchUserRatings(userId)
                .collectList()
                .flatMapMany(userRatings -> {
                    if (userRatings.isEmpty()) {
                        return Flux.empty();
                    }
                    
                    return fetchAllRatings()
                            .collectList()
                            .flatMapMany(allRatings -> {
                                Map<Integer, Map<Integer, Integer>> allUserRatings = allRatings.stream()
                                        .collect(Collectors.groupingBy(
                                                Rating::getUserId,
                                                Collectors.toMap(Rating::getMovieId, Rating::getRating)
                                        ));
                                
                                return fetchAllMovies()
                                        .collectList()
                                        .flatMapMany(allMovies -> {
                                            List<Recommendation> recommendations = collaborativeFiltering
                                                    .getRecommendations(userId, allUserRatings, allMovies, userRatings, limit);
                                            return Flux.fromIterable(recommendations);
                                        });
                            });
                });
    }

    @Override
    public Flux<Recommendation> getContentBasedRecommendations(Integer userId, Integer limit) {
        LOG.info("Getting content-based recommendations for user: {}", userId);
        
        return fetchUserRatings(userId)
                .collectList()
                .flatMapMany(userRatings -> {
                    if (userRatings.isEmpty()) {
                        return Flux.empty();
                    }
                    
                    return fetchAllMovies()
                            .collectList()
                            .flatMapMany(allMovies -> {
                                List<Recommendation> recommendations = contentBasedFiltering
                                        .getRecommendations(allMovies, userRatings, limit);
                                return Flux.fromIterable(recommendations);
                            });
                });
    }

    @Override
    public Flux<Recommendation> getHybridRecommendations(Integer userId, Integer limit) {
        LOG.info("Getting hybrid recommendations for user: {}", userId);
        
        Flux<Recommendation> collaborative = getCollaborativeFilteringRecommendations(userId, limit);
        Flux<Recommendation> contentBased = getContentBasedRecommendations(userId, limit);
        
        return Flux.merge(collaborative, contentBased)
                .collectList()
                .flatMapMany(recommendations -> {
                    LOG.info("Hybrid recommendations: collaborative + content-based = {} total", recommendations.size());
                    
                    // Combine and deduplicate recommendations
                    Map<Integer, Recommendation> combined = new HashMap<>();
                    
                    for (Recommendation rec : recommendations) {
                        Integer movieId = rec.getMovieId();
                        if (combined.containsKey(movieId)) {
                            // Average the scores
                            Recommendation existing = combined.get(movieId);
                            double avgScore = (existing.getScore() + rec.getScore()) / 2.0;
                            existing.setScore(avgScore);
                            existing.setAlgorithm("hybrid");
                            existing.setReason("Combined collaborative and content-based filtering");
                        } else {
                            combined.put(movieId, rec);
                        }
                    }
                    
                    List<Recommendation> finalRecommendations = combined.values().stream()
                            .sorted(Comparator.comparing(Recommendation::getScore).reversed())
                            .limit(limit)
                            .collect(Collectors.toList());
                    
                    // If no recommendations from algorithms, fallback to popular movies
                    if (finalRecommendations.isEmpty()) {
                        LOG.info("No recommendations from algorithms, falling back to popular movies");
                        return getPopularMoviesFallback(userId, limit);
                    }
                    
                    LOG.info("Returning {} hybrid recommendations", finalRecommendations.size());
                    return Flux.fromIterable(finalRecommendations);
                });
    }
    
    /**
     * Fallback: Return popular/top-rated movies that user hasn't rated
     */
    private Flux<Recommendation> getPopularMoviesFallback(Integer userId, Integer limit) {
        LOG.info("Fallback: Fetching popular movies for user {}", userId);
        
        return fetchUserRatings(userId)
                .collectList()
                .flatMapMany(userRatings -> {
                    Set<Integer> ratedMovieIds = userRatings.stream()
                            .map(Rating::getMovieId)
                            .collect(Collectors.toSet());
                    
                    LOG.info("User {} has rated {} movies: {}", userId, ratedMovieIds.size(), ratedMovieIds);
                    
                    return fetchAllMovies()
                            .collectList()
                            .flatMapMany(allMovies -> {
                                LOG.info("Total movies available: {}", allMovies.size());
                                
                                List<Recommendation> fallbackRecs = allMovies.stream()
                                        .filter(movie -> !ratedMovieIds.contains(movie.getMovieId()))
                                        .sorted((m1, m2) -> {
                                            // Sort by rating (descending), then by totalRatings
                                            double rating1 = m1.getRating() != null ? m1.getRating() : 0.0;
                                            double rating2 = m2.getRating() != null ? m2.getRating() : 0.0;
                                            int ratingCompare = Double.compare(rating2, rating1);
                                            if (ratingCompare != 0) return ratingCompare;
                                            
                                            int totalRatings1 = m1.getTotalRatings() != null ? m1.getTotalRatings() : 0;
                                            int totalRatings2 = m2.getTotalRatings() != null ? m2.getTotalRatings() : 0;
                                            return Integer.compare(totalRatings2, totalRatings1);
                                        })
                                        .limit(limit)
                                        .map(movie -> {
                                            LOG.debug("Adding fallback recommendation: {} (rating: {}, totalRatings: {})", 
                                                    movie.getTitle(), movie.getRating(), movie.getTotalRatings());
                                            return new Recommendation(
                                                    movie.getMovieId(),
                                                    movie.getTitle(),
                                                    0.7, // Default score for popular movies
                                                    "Popular movie you might like",
                                                    "popular"
                                            );
                                        })
                                        .collect(Collectors.toList());
                                
                                LOG.info("Returning {} popular movie recommendations as fallback (user rated {} movies, {} total movies available)", 
                                        fallbackRecs.size(), ratedMovieIds.size(), allMovies.size());
                                
                                if (fallbackRecs.isEmpty() && !allMovies.isEmpty()) {
                                    LOG.warn("All {} movies have been rated by user {}", allMovies.size(), userId);
                                }
                                
                                return Flux.fromIterable(fallbackRecs);
                            })
                            .onErrorResume(error -> {
                                LOG.error("Error in fallback recommendation: ", error);
                                return Flux.empty();
                            });
                })
                .onErrorResume(error -> {
                    LOG.error("Error fetching user ratings for fallback: ", error);
                    return Flux.empty();
                });
    }

    private Flux<Movie> fetchAllMovies() {
        return webClient.get()
                .uri(movieServiceUrl + "/movies")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Movie.class)
                .doOnError(error -> LOG.error("Error fetching movies", error))
                .onErrorResume(error -> Flux.empty());
    }

    private Flux<Rating> fetchUserRatings(Integer userId) {
        return webClient.get()
                .uri(movieServiceUrl + "/ratings/user/" + userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Rating.class)
                .doOnError(error -> LOG.error("Error fetching user ratings", error))
                .onErrorResume(error -> Flux.empty());
    }

    private Flux<Rating> fetchAllRatings() {
        // Note: This assumes there's an endpoint to get all ratings
        // If not available, we'll need to fetch ratings per user
        return webClient.get()
                .uri(movieServiceUrl + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Rating.class)
                .doOnError(error -> {
                    LOG.warn("Could not fetch all ratings, will use user-specific approach");
                })
                .onErrorResume(error -> Flux.empty());
    }
}


