package com.example.dvdrental.inventory;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface CopyRepository extends JpaRepository<DvdCopy, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from DvdCopy c where c.id = :id")
    Optional<DvdCopy> findForUpdate(long id);
    Page<DvdCopy> findByMovieIdAndStatus(long movieId, CopyStatus status, Pageable pageable);
    long countByStatus(CopyStatus status);
}
