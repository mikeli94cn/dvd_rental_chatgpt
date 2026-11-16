package com.example.dvdrental.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.BatchSize;

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
    @ManyToMany @BatchSize(size = 100)
    @JoinTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();

    protected Movie() {}
    public Movie(String title, String synopsis, int releaseYear, BigDecimal dailyRate, Set<Genre> genres) {
        update(title, synopsis, releaseYear, dailyRate, genres);
    }
    public void update(String title, String synopsis, int releaseYear, BigDecimal dailyRate, Set<Genre> genres) {
        this.title = title.strip(); this.synopsis = synopsis.strip(); this.releaseYear = releaseYear;
        this.dailyRate = dailyRate; this.genres.clear(); this.genres.addAll(genres);
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSynopsis() { return synopsis; }
    public int getReleaseYear() { return releaseYear; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public Set<Genre> getGenres() { return java.util.Collections.unmodifiableSet(genres); }
}
