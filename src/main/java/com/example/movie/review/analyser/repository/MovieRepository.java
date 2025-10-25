package com.example.movie.review.analyser.repository;
import com.example.movie.review.analyser.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findTop5ByTitleContainingIgnoreCaseOrderByVoteCountDesc(String title);
}