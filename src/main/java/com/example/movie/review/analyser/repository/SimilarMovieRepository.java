package com.example.movie.review.analyser.repository;
import com.example.movie.review.analyser.model.SimilarMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SimilarMovieRepository extends JpaRepository<SimilarMovie, Long> {
List<SimilarMovie> findTop5ByFilmid(int filmid);
}