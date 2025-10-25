package com.example.movie.review.analyser.dto;
import com.example.movie.review.analyser.model.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data @AllArgsConstructor @NoArgsConstructor
public class ReviewStats {
    private List<Review> reviews;
    private long totalReviews;
    private double averageRating;
    private String dominantSentiment;
}