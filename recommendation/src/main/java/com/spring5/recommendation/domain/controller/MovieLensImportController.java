package com.spring5.recommendation.domain.controller;

import com.spring5.recommendation.domain.movielens.MovieLensDataImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for MovieLens data import operations
 */
@RestController
@RequestMapping("/movielens")
public class MovieLensImportController {

    private static final Logger LOG = LoggerFactory.getLogger(MovieLensImportController.class);

    private final MovieLensDataImportService importService;

    @Autowired
    public MovieLensImportController(MovieLensDataImportService importService) {
        this.importService = importService;
    }

    /**
     * Import MovieLens dataset
     * POST /movielens/import
     * 
     * Request body:
     * {
     *   "datasetPath": "/path/to/movielens/dataset",
     *   "importMovies": true,
     *   "importRatings": true,
     *   "maxRatings": 10000  // 0 for all ratings
     * }
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importDataset(@RequestBody ImportRequest request) {
        LOG.info("Received import request: {}", request);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            MovieLensDataImportService.ImportResult result = importService.importDataset(
                    request.getDatasetPath(),
                    request.isImportRatings(),
                    request.isImportMovies(),
                    request.getMaxRatings()
            );
            
            response.put("success", result.isSuccess());
            response.put("result", result);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                response.put("error", result.getErrorMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            LOG.error("Error processing import request", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "MovieLens Import Service");
        return ResponseEntity.ok(response);
    }

    /**
     * Request model for import operation
     */
    public static class ImportRequest {
        private String datasetPath;
        private boolean importMovies = true;
        private boolean importRatings = true;
        private int maxRatings = 0; // 0 means import all

        public String getDatasetPath() { return datasetPath; }
        public void setDatasetPath(String datasetPath) { this.datasetPath = datasetPath; }

        public boolean isImportMovies() { return importMovies; }
        public void setImportMovies(boolean importMovies) { this.importMovies = importMovies; }

        public boolean isImportRatings() { return importRatings; }
        public void setImportRatings(boolean importRatings) { this.importRatings = importRatings; }

        public int getMaxRatings() { return maxRatings; }
        public void setMaxRatings(int maxRatings) { this.maxRatings = maxRatings; }

        @Override
        public String toString() {
            return String.format("ImportRequest{path='%s', movies=%s, ratings=%s, maxRatings=%d}",
                    datasetPath, importMovies, importRatings, maxRatings);
        }
    }
}


