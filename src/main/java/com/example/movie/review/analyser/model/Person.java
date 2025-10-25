package com.example.movie.review.analyser.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Person {
    @Id private int personid;
    private String name;
    private String profilePath;
}