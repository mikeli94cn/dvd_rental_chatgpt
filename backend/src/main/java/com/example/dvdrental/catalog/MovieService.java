package com.example.dvdrental.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class MovieService {
    private final MovieRepository movies;

    public MovieService(MovieRepository movies) { this.movies = movies; }

    public Page<MovieResponse> search(String query, int page, int size, String sort) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be nonnegative and size must be between 1 and 100");
        }
        if (!java.util.Set.of("title", "releaseYear", "dailyRate").contains(sort)) {
            throw new IllegalArgumentException("Unsupported sort field");
        }
        if (query.length() > 200) { throw new IllegalArgumentException("Search query exceeds 200 characters"); }
        return movies.findByTitleContainingIgnoreCase(query.strip(),
            PageRequest.of(page, size, Sort.by(sort).ascending().and(Sort.by("id")))).map(MovieResponse::from);
    }

    public MovieResponse find(long id) {
        return movies.findById(id).map(MovieResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
    }
}
