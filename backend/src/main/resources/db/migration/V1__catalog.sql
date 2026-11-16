CREATE TABLE movies (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    synopsis TEXT NOT NULL,
    release_year INT NOT NULL,
    daily_rate DECIMAL(10,2) NOT NULL,
    CONSTRAINT ck_movie_rate CHECK (daily_rate >= 0),
    CONSTRAINT ck_movie_year CHECK (release_year BETWEEN 1888 AND 2200),
    INDEX idx_movies_title (title)
);

CREATE TABLE genres (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE movie_genres (
    movie_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (movie_id, genre_id),
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (genre_id) REFERENCES genres(id)
);
