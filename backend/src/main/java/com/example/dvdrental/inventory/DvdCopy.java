package com.example.dvdrental.inventory;

import com.example.dvdrental.catalog.Movie;
import jakarta.persistence.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Entity
@Table(name = "dvd_copies")
public class DvdCopy {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
    @Column(nullable = false, unique = true, length = 80)
    private String barcode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private CopyStatus status = CopyStatus.AVAILABLE;
    protected DvdCopy() {}
    public DvdCopy(Movie movie, String barcode) { this.movie = movie; this.barcode = barcode.strip(); }
    public void allocate() {
        if (status != CopyStatus.AVAILABLE) { throw conflict("DVD copy is not available"); }
        status = CopyStatus.RENTED;
    }
    public void confirmReturn(CopyStatus inspectionResult) {
        if (status != CopyStatus.RENTED) { throw conflict("DVD copy is not rented"); }
        if (!java.util.Set.of(CopyStatus.AVAILABLE, CopyStatus.MAINTENANCE, CopyStatus.DAMAGED, CopyStatus.LOST).contains(inspectionResult)) {
            throw new IllegalArgumentException("Invalid inspection result");
        }
        status = inspectionResult;
    }
    public void changeStatus(CopyStatus next) {
        if (status == CopyStatus.RENTED || next == CopyStatus.RENTED || status == CopyStatus.RETIRED) {
            throw conflict("Rented or retired inventory cannot be changed directly");
        }
        status = next;
    }
    private static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
    public Long getId() { return id; }
    public Movie getMovie() { return movie; }
    public String getBarcode() { return barcode; }
    public CopyStatus getStatus() { return status; }
}
