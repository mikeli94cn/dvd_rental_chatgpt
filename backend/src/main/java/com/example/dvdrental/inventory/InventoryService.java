package com.example.dvdrental.inventory;

import com.example.dvdrental.catalog.*;
import com.example.dvdrental.common.*;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class InventoryService {
    private final CopyRepository copies;
    private final MovieRepository movies;
    public InventoryService(CopyRepository copies, MovieRepository movies) { this.copies = copies; this.movies = movies; }
    @Transactional
    public CopyResponse create(CopyRequest request) {
        Movie movie = movies.findById(request.movieId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"));
        return CopyResponse.from(copies.saveAndFlush(new DvdCopy(movie, request.barcode())));
    }
    @Transactional
    public CopyResponse changeStatus(long id, CopyStatus status) {
        DvdCopy copy = locked(id); copy.changeStatus(status);
        return CopyResponse.from(copy);
    }
    public DvdCopy locked(long id) {
        return copies.findForUpdate(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DVD copy not found"));
    }
    public PageResponse<CopyResponse> list(int page, int size) {
        return PageResponse.from(copies.findAll(Pages.of(page, size)).map(CopyResponse::from));
    }
    public PageResponse<AvailableCopy> availability(long movieId, int page, int size) {
        if (!movies.existsById(movieId)) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found"); }
        return PageResponse.from(copies.findByMovieIdAndStatus(movieId, CopyStatus.AVAILABLE, Pages.of(page, size))
            .map(c -> new AvailableCopy(c.getId())));
    }
    public record CopyRequest(@NotNull @Positive Long movieId, @NotBlank @Size(max = 80) String barcode) {}
    public record StatusRequest(@NotNull CopyStatus status) {}
    public record AvailableCopy(Long id) {}
    public record CopyResponse(Long id, Long movieId, String movieTitle, String barcode, CopyStatus status) {
        public static CopyResponse from(DvdCopy c) {
            return new CopyResponse(c.getId(), c.getMovie().getId(), c.getMovie().getTitle(), c.getBarcode(), c.getStatus());
        }
    }
}
