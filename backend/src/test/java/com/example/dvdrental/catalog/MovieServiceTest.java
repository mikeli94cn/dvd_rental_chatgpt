package com.example.dvdrental.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovieServiceTest {
    private final MovieRepository repository = mock(MovieRepository.class);
    private final MovieService service = new MovieService(repository);

    @Test
    void rejectsUnboundedPagesAndUnknownSortFields() {
        assertThrows(IllegalArgumentException.class, () -> service.search("", 0, 101, "title"));
        assertThrows(IllegalArgumentException.class, () -> service.search("", -1, 20, "title"));
        assertThrows(IllegalArgumentException.class, () -> service.search("", 0, 20, "synopsis"));
        verifyNoInteractions(repository);
    }

    @Test
    void searchesAndReturnsEmptyPage() {
        when(repository.findByTitleContainingIgnoreCase(eq("Alien"), any(Pageable.class))).thenReturn(Page.empty());
        assertEquals(0, service.search(" Alien ", 0, 20, "title").getTotalElements());
    }

    @Test
    void missingMovieIsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.find(99L)).getStatusCode().value());
    }
}
