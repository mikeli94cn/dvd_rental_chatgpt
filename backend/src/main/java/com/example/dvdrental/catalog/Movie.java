package com.example.dvdrental.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "movies")
public class Movie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String synopsis;
    @Column(nullable = false)
    private int releaseYear;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    protected Movie() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSynopsis() { return synopsis; }
    public int getReleaseYear() { return releaseYear; }
    public BigDecimal getDailyRate() { return dailyRate; }
}
