package com.spring5.recommendation.domain.movielens;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for MovieLens dataset files
 * Supports ratings.csv, movies.csv, and tags.csv formats
 */
@Component
public class MovieLensDataParser {

    private static final Logger LOG = LoggerFactory.getLogger(MovieLensDataParser.class);

    /**
     * Parse ratings file (ratings.csv)
     * Format: userId,movieId,rating,timestamp
     */
    public List<MovieLensRating> parseRatings(String filePath) throws IOException {
        List<MovieLensRating> ratings = new ArrayList<>();
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            LOG.warn("Ratings file not found: {}", filePath);
            return ratings;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        Integer userId = Integer.parseInt(parts[0].trim());
                        Integer movieId = Integer.parseInt(parts[1].trim());
                        Double rating = Double.parseDouble(parts[2].trim());
                        Long timestamp = parts.length > 3 ? Long.parseLong(parts[3].trim()) : null;
                        
                        // Convert MovieLens rating (0.5-5.0) to our scale (1-5)
                        Integer normalizedRating = (int) Math.round(rating);
                        if (normalizedRating < 1) normalizedRating = 1;
                        if (normalizedRating > 5) normalizedRating = 5;
                        
                        ratings.add(new MovieLensRating(userId, movieId, normalizedRating, timestamp));
                    } catch (NumberFormatException e) {
                        LOG.warn("Skipping invalid rating line: {}", line);
                    }
                }
            }
        }
        
        LOG.info("Parsed {} ratings from {}", ratings.size(), filePath);
        return ratings;
    }

    /**
     * Parse movies file (movies.csv)
     * Format: movieId,title,genres
     * Genres are pipe-separated: "Action|Adventure|Sci-Fi"
     */
    public Map<Integer, MovieLensMovie> parseMovies(String filePath) throws IOException {
        Map<Integer, MovieLensMovie> movies = new HashMap<>();
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            LOG.warn("Movies file not found: {}", filePath);
            return movies;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                // Handle CSV with quoted fields (title may contain commas)
                String[] parts = parseCSVLine(line);
                if (parts.length >= 2) {
                    try {
                        Integer movieId = Integer.parseInt(parts[0].trim());
                        String title = parts[1].trim();
                        String genres = parts.length > 2 ? parts[2].trim() : "";
                        
                        // Parse title (format: "Title (Year)")
                        String cleanTitle = title;
                        Integer year = null;
                        if (title.contains("(") && title.contains(")")) {
                            int start = title.lastIndexOf("(");
                            int end = title.lastIndexOf(")");
                            if (start < end) {
                                try {
                                    year = Integer.parseInt(title.substring(start + 1, end));
                                    cleanTitle = title.substring(0, start).trim();
                                } catch (NumberFormatException e) {
                                    // Year parsing failed, keep original title
                                }
                            }
                        }
                        
                        // Convert pipe-separated genres to comma-separated
                        String genreList = genres.replace("|", ",");
                        
                        movies.put(movieId, new MovieLensMovie(movieId, cleanTitle, genreList, year));
                    } catch (NumberFormatException e) {
                        LOG.warn("Skipping invalid movie line: {}", line);
                    }
                }
            }
        }
        
        LOG.info("Parsed {} movies from {}", movies.size(), filePath);
        return movies;
    }

    /**
     * Parse tags file (tags.csv) - optional
     * Format: userId,movieId,tag,timestamp
     */
    public Map<Integer, List<String>> parseTags(String filePath) throws IOException {
        Map<Integer, List<String>> movieTags = new HashMap<>();
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            LOG.info("Tags file not found (optional): {}", filePath);
            return movieTags;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        Integer movieId = Integer.parseInt(parts[1].trim());
                        String tag = parts[2].trim().toLowerCase();
                        
                        movieTags.computeIfAbsent(movieId, k -> new ArrayList<>()).add(tag);
                    } catch (NumberFormatException e) {
                        LOG.warn("Skipping invalid tag line: {}", line);
                    }
                }
            }
        }
        
        LOG.info("Parsed tags for {} movies from {}", movieTags.size(), filePath);
        return movieTags;
    }

    /**
     * Parse CSV line handling quoted fields
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }
}

