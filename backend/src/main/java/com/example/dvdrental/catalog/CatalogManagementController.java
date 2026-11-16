package com.example.dvdrental.catalog;

import com.example.dvdrental.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class CatalogManagementController {
    private final CatalogManagementService catalog;
    public CatalogManagementController(CatalogManagementService catalog) { this.catalog = catalog; }
    @GetMapping("/api/v1/genres")
    public PageResponse<MovieResponse.GenreResponse> genres(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) { return catalog.genres(page, size); }
    @PostMapping("/api/v1/staff/movies") @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse create(@Valid @RequestBody CatalogManagementService.MovieRequest request) { return catalog.create(request); }
    @PutMapping("/api/v1/staff/movies/{id}")
    public MovieResponse update(@PathVariable long id, @Valid @RequestBody CatalogManagementService.MovieRequest request) {
        return catalog.update(id, request);
    }
    @DeleteMapping("/api/v1/staff/movies/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable long id) { catalog.deleteMovie(id); }
    @PostMapping("/api/v1/staff/genres") @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse.GenreResponse createGenre(@Valid @RequestBody CatalogManagementService.GenreRequest request) {
        return catalog.createGenre(request);
    }
    @PutMapping("/api/v1/staff/genres/{id}")
    public MovieResponse.GenreResponse rename(@PathVariable long id, @Valid @RequestBody CatalogManagementService.GenreRequest request) {
        return catalog.renameGenre(id, request);
    }
    @DeleteMapping("/api/v1/staff/genres/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGenre(@PathVariable long id) { catalog.deleteGenre(id); }
}
