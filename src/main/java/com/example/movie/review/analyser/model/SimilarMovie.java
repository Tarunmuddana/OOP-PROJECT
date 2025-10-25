package com.example.movie.review.analyser.model;
import jakarta.persistence.*;
import lombok.Data;
@Data @Entity
public class SimilarMovie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private int filmid;
    private int similarFilmid;
}