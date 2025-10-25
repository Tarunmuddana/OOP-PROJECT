package com.example.movie.review.analyser.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String movieTitle;
    @Column(length = 2000) private String reviewText;
    private String sentiment;
    private double rating;
}