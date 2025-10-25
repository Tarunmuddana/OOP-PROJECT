package com.example.movie.review.analyser.service;

import com.example.movie.review.analyser.dto.CastMember;
import com.example.movie.review.analyser.model.*;
import com.example.movie.review.analyser.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocalMovieDataService {

    @Autowired private MovieRepository movieRepository;
    @Autowired private PersonRepository personRepository;
    @Autowired private CrewCreditRepository crewCreditRepository;
    @Autowired private ExternalReviewRepository externalReviewRepository;
    @Autowired private SimilarMovieRepository similarMovieRepository;
    
    public List<Movie> searchMovies(String query) {
        if (query == null || query.trim().length() < 3) {
            return Collections.emptyList();
        }
        return movieRepository.findTop5ByTitleContainingIgnoreCaseOrderByVoteCountDesc(query);
    }
    
    public Movie getMovieDetails(int movieId) {
        return movieRepository.findById(movieId).orElse(null);
    }

    public List<CastMember> getCastForMovie(int movieId) {
        // Find the top 10 credit entries for this movie
        List<CrewCredit> credits = crewCreditRepository.findTop10ByFilmid(movieId);
        
        // For each credit, find the corresponding person and create a CastMember DTO
        return credits.stream()
            .map(credit -> {
                Person person = personRepository.findById(credit.getPersonid()).orElse(null);
                if (person == null) {
                    return null; // Skip if the person isn't in our database
                }
                return new CastMember(person.getName(), credit.getCharacterName(), person.getProfilePath());
            })
            .filter(java.util.Objects::nonNull) // Filter out any nulls
            .collect(Collectors.toList());
    }

    public List<ExternalReview> getExternalReviews(int movieId) {
        return externalReviewRepository.findTop5ByFilmid(movieId);
    }

    public List<Movie> getSimilarMovies(int movieId) {
        // Find the top 5 links for similar movies
        List<SimilarMovie> similarLinks = similarMovieRepository.findTop5ByFilmid(movieId);
        
        // Extract the IDs of the similar movies
        List<Integer> similarMovieIds = similarLinks.stream()
            .map(SimilarMovie::getSimilarFilmid)
            .collect(Collectors.toList());
        
        // Find all those movies in our database
        if (similarMovieIds.isEmpty()) {
            return Collections.emptyList();
        }
        return movieRepository.findAllById(similarMovieIds);
    }
}