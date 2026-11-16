package com.example.dvdrental.catalog;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService movies;

    public MovieController(MovieService movies) { this.movies = movies; }

    public record MoviePage(List<MovieResponse> content, int page, int size, long totalElements, int totalPages) {}

    @GetMapping
    public MoviePage search(@RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sort) {
        var result = movies.search(query, page, size, sort);
        return new MoviePage(result.getContent(), page, size, result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/{id}")
    public MovieResponse find(@PathVariable long id) { return movies.find(id); }
}
