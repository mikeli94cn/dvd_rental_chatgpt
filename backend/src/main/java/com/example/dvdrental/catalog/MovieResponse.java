package com.example.dvdrental.catalog;

import java.math.BigDecimal;

public record MovieResponse(Long id, String title, String synopsis, int releaseYear, BigDecimal dailyRate) {
    static MovieResponse from(Movie movie) {
        return new MovieResponse(movie.getId(), movie.getTitle(), movie.getSynopsis(), movie.getReleaseYear(), movie.getDailyRate());
    }
}
