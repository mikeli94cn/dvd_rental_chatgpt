package com.example.dvdrental.rental;

import com.example.dvdrental.customer.Customer;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "rental_orders")
public class RentalOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Column(nullable = false)
    private Instant createdAt;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) @BatchSize(size = 100)
    @OrderBy("id ASC")
    private List<RentalItem> items = new ArrayList<>();
    protected RentalOrder() {}
    public RentalOrder(Customer customer, Instant createdAt) { this.customer = customer; this.createdAt = createdAt; }
    public void add(RentalItem item) { items.add(item); }
    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Instant getCreatedAt() { return createdAt; }
    public List<RentalItem> getItems() { return Collections.unmodifiableList(items); }
    public String getStatus() {
        return items.stream().anyMatch(i -> i.getStatus() == RentalItemStatus.ACTIVE) ? "ACTIVE" : "CLOSED";
    }
}
