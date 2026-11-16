package com.example.dvdrental.catalog;

import java.math.BigDecimal;
import java.util.List;

public record MovieResponse(Long id, String title, String synopsis, int releaseYear, BigDecimal dailyRate,
        List<GenreResponse> genres) {
    static MovieResponse from(Movie movie) {
        return new MovieResponse(movie.getId(), movie.getTitle(), movie.getSynopsis(), movie.getReleaseYear(),
            movie.getDailyRate(), movie.getGenres().stream().map(GenreResponse::from)
                .sorted(java.util.Comparator.comparing(GenreResponse::name)).toList());
    }
    public record GenreResponse(Long id, String name) {
        public static GenreResponse from(Genre genre) { return new GenreResponse(genre.getId(), genre.getName()); }
    }
}
