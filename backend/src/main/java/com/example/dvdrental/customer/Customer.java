package com.example.dvdrental.customer;

import com.example.dvdrental.identity.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(length = 40)
    private String phone;

    protected Customer() {}
    public Customer(AppUser user, String name) { this.user = user; this.name = name.strip(); }
    public void update(String name, String phone) {
        this.name = name.strip(); this.phone = phone == null ? null : phone.strip();
    }
    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
}
