package com.example.dvdrental.rental;

import com.example.dvdrental.identity.AppUser;
import com.example.dvdrental.inventory.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Entity
@Table(name = "rental_items")
public class RentalItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private RentalOrder order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "copy_id", nullable = false)
    private DvdCopy copy;
    @Column(nullable = false)
    private LocalDate dueDate;
    private Instant returnedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private RentalItemStatus status = RentalItemStatus.ACTIVE;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRateSnapshot;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal overdueRateSnapshot;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal rentalCharge;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal overdueCharge = new BigDecimal("0.00");
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private AppUser confirmedBy;
    protected RentalItem() {}
    public RentalItem(RentalOrder order, DvdCopy copy, LocalDate dueDate, BigDecimal dailyRate,
            BigDecimal overdueRate, BigDecimal rentalCharge) {
        this.order = order; this.copy = copy; this.dueDate = dueDate; this.dailyRateSnapshot = dailyRate;
        this.overdueRateSnapshot = overdueRate; this.rentalCharge = rentalCharge;
    }
    public void complete(Instant returnedAt, BigDecimal overdueCharge, AppUser staff, CopyStatus inspection) {
        if (status != RentalItemStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rental item has already been resolved");
        }
        copy.confirmReturn(inspection);
        this.returnedAt = returnedAt; this.overdueCharge = overdueCharge; this.confirmedBy = staff;
        status = inspection == CopyStatus.LOST ? RentalItemStatus.LOST : RentalItemStatus.RETURNED;
    }
    public Long getId() { return id; }
    public RentalOrder getOrder() { return order; }
    public DvdCopy getCopy() { return copy; }
    public LocalDate getDueDate() { return dueDate; }
    public Instant getReturnedAt() { return returnedAt; }
    public RentalItemStatus getStatus() { return status; }
    public BigDecimal getDailyRateSnapshot() { return dailyRateSnapshot; }
    public BigDecimal getOverdueRateSnapshot() { return overdueRateSnapshot; }
    public BigDecimal getRentalCharge() { return rentalCharge; }
    public BigDecimal getOverdueCharge() { return overdueCharge; }
}
