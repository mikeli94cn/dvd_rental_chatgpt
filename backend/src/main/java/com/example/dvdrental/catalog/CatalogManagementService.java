package com.example.dvdrental.catalog;

import com.example.dvdrental.common.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CatalogManagementService {
    private final MovieRepository movies;
    private final GenreRepository genres;
    public CatalogManagementService(MovieRepository movies, GenreRepository genres) {
        this.movies = movies; this.genres = genres;
    }
    public MovieResponse create(MovieRequest request) {
        return MovieResponse.from(movies.save(new Movie(request.title(), request.synopsis(), request.releaseYear(),
            request.dailyRate(), resolveGenres(request.genreIds()))));
    }
    public MovieResponse update(long id, MovieRequest request) {
        Movie movie = movies.findById(id).orElseThrow(() -> notFound("Movie"));
        movie.update(request.title(), request.synopsis(), request.releaseYear(), request.dailyRate(), resolveGenres(request.genreIds()));
        return MovieResponse.from(movie);
    }
    public void deleteMovie(long id) {
        Movie movie = movies.findById(id).orElseThrow(() -> notFound("Movie"));
        movies.delete(movie); movies.flush();
    }
    public MovieResponse.GenreResponse createGenre(GenreRequest request) {
        return MovieResponse.GenreResponse.from(genres.saveAndFlush(new Genre(request.name())));
    }
    public MovieResponse.GenreResponse renameGenre(long id, GenreRequest request) {
        Genre genre = genres.findById(id).orElseThrow(() -> notFound("Genre"));
        genre.rename(request.name()); genres.flush();
        return MovieResponse.GenreResponse.from(genre);
    }
    public void deleteGenre(long id) {
        Genre genre = genres.findById(id).orElseThrow(() -> notFound("Genre"));
        genres.delete(genre); genres.flush();
    }
    @Transactional(readOnly = true)
    public PageResponse<MovieResponse.GenreResponse> genres(int page, int size) {
        return PageResponse.from(genres.findAll(Pages.of(page, size)).map(MovieResponse.GenreResponse::from));
    }
    private Set<Genre> resolveGenres(Set<Long> ids) {
        Set<Genre> result = new HashSet<>(genres.findAllById(ids));
        if (result.size() != ids.size()) { throw notFound("Genre"); }
        return result;
    }
    private static ResponseStatusException notFound(String type) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found");
    }
    public record MovieRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 10000) String synopsis,
        @Min(1888) @Max(2200) int releaseYear,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal dailyRate,
        @NotNull @Size(max = 20) Set<@NotNull @Positive Long> genreIds) {}
    public record GenreRequest(@NotBlank @Size(max = 80) String name) {}
}
