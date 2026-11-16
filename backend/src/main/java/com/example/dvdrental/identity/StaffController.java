package com.example.dvdrental.identity;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/staff")
public class StaffController {
    private final AccountService accounts;
    public StaffController(AccountService accounts) { this.accounts = accounts; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody AccountService.StaffRequest request) {
        return accounts.createStaff(request);
    }
}
