package com.spring5.recommendation.domain.movielens;

import com.spring5.recommendation.domain.service.Movie;
import com.spring5.recommendation.domain.service.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service to import MovieLens dataset into the system
 */
@Service
public class MovieLensDataImportService {

    private static final Logger LOG = LoggerFactory.getLogger(MovieLensDataImportService.class);

    private final MovieLensDataParser parser;
    private final WebClient webClient;

    @Value("${movie.service.url:http://localhost:8081}")
    private String movieServiceUrl;

    @Autowired
    public MovieLensDataImportService(MovieLensDataParser parser, WebClient.Builder webClientBuilder) {
        this.parser = parser;
        this.webClient = webClientBuilder.build();
    }

    /**
     * Import MovieLens dataset from directory
     * Expected files: ratings.csv, movies.csv, tags.csv (optional)
     */
    public ImportResult importDataset(String datasetDirectory, boolean importRatings, boolean importMovies, int maxRatings) {
        ImportResult result = new ImportResult();
        
        try {
            // Parse movies first
            if (importMovies) {
                LOG.info("Parsing movies from {}", datasetDirectory);
                Map<Integer, MovieLensMovie> movies = parser.parseMovies(datasetDirectory + "/movies.csv");
                result.setTotalMovies(movies.size());
                
                // Import movies
                AtomicInteger importedMovies = new AtomicInteger(0);
                AtomicInteger skippedMovies = new AtomicInteger(0);
                
                movies.values().forEach(movielensMovie -> {
                    try {
                        Movie movie = convertToMovie(movielensMovie);
                        createMovie(movie)
                                .doOnSuccess(m -> importedMovies.incrementAndGet())
                                .doOnError(e -> {
                                    skippedMovies.incrementAndGet();
                                    LOG.debug("Failed to import movie {}: {}", movielensMovie.getMovieId(), e.getMessage());
                                })
                                .subscribe();
                    } catch (Exception e) {
                        skippedMovies.incrementAndGet();
                        LOG.warn("Error converting movie {}: {}", movielensMovie.getMovieId(), e.getMessage());
                    }
                });
                
                // Wait a bit for async operations
                Thread.sleep(2000);
                
                result.setImportedMovies(importedMovies.get());
                result.setSkippedMovies(skippedMovies.get());
                LOG.info("Imported {} movies, skipped {}", importedMovies.get(), skippedMovies.get());
            }

            // Parse and import ratings
            if (importRatings) {
                LOG.info("Parsing ratings from {}", datasetDirectory);
                List<MovieLensRating> ratings = parser.parseRatings(datasetDirectory + "/ratings.csv");
                
                // Limit ratings if specified
                if (maxRatings > 0 && ratings.size() > maxRatings) {
                    ratings = ratings.subList(0, maxRatings);
                    LOG.info("Limited to first {} ratings", maxRatings);
                }
                
                result.setTotalRatings(ratings.size());
                
                // Import ratings in batches
                AtomicInteger importedRatings = new AtomicInteger(0);
                AtomicInteger skippedRatings = new AtomicInteger(0);
                
                Flux.fromIterable(ratings)
                        .buffer(100) // Process in batches of 100
                        .flatMap(batch -> {
                            return Flux.fromIterable(batch)
                                    .flatMap(movielensRating -> {
                                        Rating rating = convertToRating(movielensRating);
                                        return createRating(rating)
                                                .doOnSuccess(r -> importedRatings.incrementAndGet())
                                                .doOnError(e -> {
                                                    skippedRatings.incrementAndGet();
                                                    LOG.debug("Failed to import rating: {}", e.getMessage());
                                                })
                                                .then(Mono.just(rating));
                                    })
                                    .then(Mono.just(batch));
                        })
                        .blockLast(); // Wait for completion
                
                result.setImportedRatings(importedRatings.get());
                result.setSkippedRatings(skippedRatings.get());
                LOG.info("Imported {} ratings, skipped {}", importedRatings.get(), skippedRatings.get());
            }

            result.setSuccess(true);
            LOG.info("Import completed successfully: {}", result);
            
        } catch (Exception e) {
            LOG.error("Error importing MovieLens dataset", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }

    /**
     * Convert MovieLens movie to our Movie model
     */
    private Movie convertToMovie(MovieLensMovie movielensMovie) {
        return new Movie(
                movielensMovie.getMovieId(),
                movielensMovie.getTitle(),
                "Movie from MovieLens dataset", // Description
                movielensMovie.getGenres(),
                null, // Director (not in MovieLens)
                "[]", // Cast (empty JSON array)
                null, // releaseDate
                null, // duration
                null, // posterUrl
                0.0, // Rating (will be calculated from ratings)
                0, // Total ratings
                null, // language
                null, // certification
                null // serviceAddress
        );
    }

    /**
     * Convert MovieLens rating to our Rating model
     */
    private Rating convertToRating(MovieLensRating movielensRating) {
        return new Rating(
                null, // ratingId (will be auto-generated)
                movielensRating.getUserId(),
                movielensRating.getMovieId(),
                movielensRating.getRating(),
                null, // review
                null // createdAt (will be set by database)
        );
    }

    /**
     * Create movie via MovieService API
     */
    private Mono<Movie> createMovie(Movie movie) {
        return webClient.post()
                .uri(movieServiceUrl + "/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movie)
                .retrieve()
                .bodyToMono(Movie.class)
                .onErrorResume(e -> {
                    // Movie might already exist, that's okay
                    LOG.debug("Movie creation failed (might already exist): {}", e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Create rating via MovieService API
     */
    private Mono<Rating> createRating(Rating rating) {
        return webClient.post()
                .uri(movieServiceUrl + "/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(rating)
                .retrieve()
                .bodyToMono(Rating.class)
                .onErrorResume(e -> {
                    // Rating might already exist, that's okay
                    LOG.debug("Rating creation failed (might already exist): {}", e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Result of import operation
     */
    public static class ImportResult {
        private boolean success;
        private String errorMessage;
        private int totalMovies;
        private int importedMovies;
        private int skippedMovies;
        private int totalRatings;
        private int importedRatings;
        private int skippedRatings;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public int getTotalMovies() { return totalMovies; }
        public void setTotalMovies(int totalMovies) { this.totalMovies = totalMovies; }
        
        public int getImportedMovies() { return importedMovies; }
        public void setImportedMovies(int importedMovies) { this.importedMovies = importedMovies; }
        
        public int getSkippedMovies() { return skippedMovies; }
        public void setSkippedMovies(int skippedMovies) { this.skippedMovies = skippedMovies; }
        
        public int getTotalRatings() { return totalRatings; }
        public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }
        
        public int getImportedRatings() { return importedRatings; }
        public void setImportedRatings(int importedRatings) { this.importedRatings = importedRatings; }
        
        public int getSkippedRatings() { return skippedRatings; }
        public void setSkippedRatings(int skippedRatings) { this.skippedRatings = skippedRatings; }

        @Override
        public String toString() {
            return String.format(
                    "ImportResult{success=%s, movies=%d/%d (skipped: %d), ratings=%d/%d (skipped: %d)}",
                    success, importedMovies, totalMovies, skippedMovies,
                    importedRatings, totalRatings, skippedRatings
            );
        }
    }
}

