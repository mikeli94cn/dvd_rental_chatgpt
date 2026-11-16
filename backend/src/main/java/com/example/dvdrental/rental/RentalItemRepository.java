package com.example.dvdrental.rental;

import java.time.LocalDate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalItemRepository extends JpaRepository<RentalItem, Long> {
    Page<RentalItem> findByStatusAndDueDateBefore(RentalItemStatus status, LocalDate date, Pageable pageable);
    long countByStatus(RentalItemStatus status);
    long countByStatusAndDueDateBefore(RentalItemStatus status, LocalDate date);
}
