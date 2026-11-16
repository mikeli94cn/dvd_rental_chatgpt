package com.example.dvdrental.customer;

import com.example.dvdrental.common.PageResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
public class CustomerController {
    private final CustomerService customers;
    public CustomerController(CustomerService customers) { this.customers = customers; }
    @GetMapping("/api/v1/customers/me")
    public CustomerService.Profile profile(Principal principal) { return customers.current(principal.getName()); }
    @PutMapping("/api/v1/customers/me")
    public CustomerService.Profile update(Principal principal, @Valid @RequestBody CustomerService.ProfileRequest request) {
        return customers.update(principal.getName(), request);
    }
    @PutMapping("/api/v1/staff/customers/{id}")
    public CustomerService.Profile updateByStaff(@PathVariable long id,
            @Valid @RequestBody CustomerService.ProfileRequest request) { return customers.updateByStaff(id, request); }
    @GetMapping("/api/v1/staff/customers")
    public PageResponse<CustomerService.Profile> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) { return customers.list(page, size); }
}
