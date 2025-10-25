package com.example.movie.review.analyser.repository;
import com.example.movie.review.analyser.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMovieTitleIgnoreCase(String movieTitle);
}