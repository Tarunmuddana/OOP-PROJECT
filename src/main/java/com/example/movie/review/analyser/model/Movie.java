package com.example.movie.review.analyser.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Movie {
    @Id private int filmid;
    private String title;
    @Column(length = 4000) private String overview;
    private String posterPath;
    private String releaseDate;
    private double voteAverage;
    private int voteCount;
}