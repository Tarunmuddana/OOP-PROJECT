package com.example.movie.review.analyser.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ExternalReview {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private int filmid;
    private String author;
    @Column(length = 4000) private String content;
    private String sentiment; // The pre-computed sentiment
}