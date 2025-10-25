package com.example.movie.review.analyser.repository;
import com.example.movie.review.analyser.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PersonRepository extends JpaRepository<Person, Integer> {}