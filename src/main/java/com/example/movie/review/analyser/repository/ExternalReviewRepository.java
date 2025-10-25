package com.example.movie.review.analyser.repository;
import com.example.movie.review.analyser.model.ExternalReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ExternalReviewRepository extends JpaRepository<ExternalReview, Long> {
    List<ExternalReview> findTop5ByFilmid(int filmid);
}