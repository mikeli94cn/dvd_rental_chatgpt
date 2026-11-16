package com.example.dvdrental.customer;

import com.example.dvdrental.common.PageResponse;
import com.example.dvdrental.common.Pages;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository customers;
    public CustomerService(CustomerRepository customers) { this.customers = customers; }
    public Profile current(String email) { return Profile.from(find(email)); }
    @Transactional
    public Profile update(String email, ProfileRequest request) {
        Customer customer = find(email); customer.update(request.name(), request.phone());
        return Profile.from(customer);
    }
    @Transactional
    public Profile updateByStaff(long id, ProfileRequest request) {
        Customer customer = customers.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        customer.update(request.name(), request.phone()); return Profile.from(customer);
    }
    public PageResponse<Profile> list(int page, int size) {
        return PageResponse.from(customers.findAll(Pages.of(page, size)).map(Profile::from));
    }
    public Customer find(String email) {
        return customers.findByUserEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }
    public record Profile(Long id, String email, String name, String phone) {
        static Profile from(Customer c) { return new Profile(c.getId(), c.getUser().getEmail(), c.getName(), c.getPhone()); }
    }
    public record ProfileRequest(
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 120) String name,
        @jakarta.validation.constraints.Size(max = 40) String phone) {}
}
