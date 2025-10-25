package com.example.movie.review.analyser.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class CrewCredit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private int filmid;
    private int personid;
    private String characterName;
}