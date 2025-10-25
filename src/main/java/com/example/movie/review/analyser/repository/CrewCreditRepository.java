package com.example.movie.review.analyser.repository;
import com.example.movie.review.analyser.model.CrewCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CrewCreditRepository extends JpaRepository<CrewCredit, Long> {
    List<CrewCredit> findTop10ByFilmid(int filmid);
}