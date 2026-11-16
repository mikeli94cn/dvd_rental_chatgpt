package com.example.dvdrental.rental;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface RentalOrderRepository extends JpaRepository<RentalOrder, Long> {
    Page<RentalOrder> findByCustomerUserEmail(String email, Pageable pageable);
    Optional<RentalOrder> findByIdAndCustomerUserEmail(long id, String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from RentalOrder o where o.id = :id")
    Optional<RentalOrder> findForUpdate(long id);
}
