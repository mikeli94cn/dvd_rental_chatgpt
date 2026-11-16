CREATE TABLE dvd_copies (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    barcode VARCHAR(80) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    CONSTRAINT ck_copy_status CHECK (status IN ('AVAILABLE', 'RENTED', 'MAINTENANCE', 'DAMAGED', 'LOST', 'RETIRED')),
    INDEX idx_copy_availability (movie_id, status)
);
