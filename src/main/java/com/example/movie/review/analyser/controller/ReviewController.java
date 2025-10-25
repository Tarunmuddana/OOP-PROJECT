package com.example.movie.review.analyser.controller;

import com.example.movie.review.analyser.dto.CastMember;
import com.example.movie.review.analyser.dto.ReviewStats;
import com.example.movie.review.analyser.model.ExternalReview;
import com.example.movie.review.analyser.model.Movie;
import com.example.movie.review.analyser.model.Review;
import com.example.movie.review.analyser.repository.ReviewRepository;
import com.example.movie.review.analyser.service.LocalMovieDataService;
import com.example.movie.review.analyser.service.SentimentAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin // Base path for all API endpoints
public class ReviewController {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private SentimentAnalysisService sentimentAnalysisService;
    @Autowired private LocalMovieDataService localMovieDataService;

    // --- Movie Data Endpoints ---

    @GetMapping("/movies/search")
    public List<Movie> searchMovies(@RequestParam String query) {
        return localMovieDataService.searchMovies(query);
    }

    @GetMapping("/movies/{movieId}/details")
    public Movie getMovieDetails(@PathVariable int movieId) {
        return localMovieDataService.getMovieDetails(movieId);
    }

    @GetMapping("/movies/{movieId}/cast")
    public List<CastMember> getCastForMovie(@PathVariable int movieId) {
        return localMovieDataService.getCastForMovie(movieId);
    }
    
    @GetMapping("/movies/{movieId}/external-reviews")
    public List<ExternalReview> getExternalReviews(@PathVariable int movieId) {
        return localMovieDataService.getExternalReviews(movieId);
    }
    
    @GetMapping("/movies/{movieId}/similar")
    public List<Movie> getSimilarMovies(@PathVariable int movieId) {
        return localMovieDataService.getSimilarMovies(movieId);
    }

    // --- User Review Endpoints ---

    @PostMapping("/reviews")
    public Review createReview(@RequestBody Review review) {
        String sentiment = sentimentAnalysisService.analyzeSentiment(review.getReviewText());
        review.setSentiment(sentiment);
        return reviewRepository.save(review);
    }

    @GetMapping("/reviews/stats")
    public ReviewStats getReviewStats(@RequestParam String title) {
        List<Review> reviews = reviewRepository.findByMovieTitleIgnoreCase(title);
        long total = reviews.size();
        
        if (total == 0) {
            return new ReviewStats(reviews, 0, 0.0, "N/A");
        }
        
        double avgRating = reviews.stream()
            .mapToDouble(Review::getRating)
            .average()
            .orElse(0.0);
        
        Map<String, Long> sentimentCounts = reviews.stream()
            .collect(Collectors.groupingBy(Review::getSentiment, Collectors.counting()));
        
        String dominantSentiment = sentimentCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Mixed");

        return new ReviewStats(reviews, total, avgRating, dominantSentiment);
    }
}