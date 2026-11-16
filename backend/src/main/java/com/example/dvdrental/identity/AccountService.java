package com.example.dvdrental.identity;

import com.example.dvdrental.customer.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AccountService {
    private final UserRepository users;
    private final CustomerRepository customers;
    private final PasswordEncoder passwords;

    public AccountService(UserRepository users, CustomerRepository customers, PasswordEncoder passwords) {
        this.users = users; this.customers = customers; this.passwords = passwords;
    }
    public AccountResponse register(RegistrationRequest request) {
        AppUser user = create(request.email(), request.password(), Role.CUSTOMER);
        customers.save(new Customer(user, request.name()));
        return AccountResponse.from(user);
    }
    public AccountResponse createStaff(StaffRequest request) {
        return AccountResponse.from(create(request.email(), request.password(), Role.STAFF));
    }
    public void bootstrapAdmin(String email, String password) {
        var existing = users.findByEmail(normalizeEmail(email));
        if (existing.isEmpty()) { create(email, password, Role.ADMIN); }
        else if (existing.get().getRole() != Role.ADMIN) {
            throw new IllegalStateException("Development administrator email belongs to a non-administrator account");
        }
    }
    private AppUser create(String email, String password, Role role) {
        if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must contain at least 12 characters and at most 72 UTF-8 bytes");
        }
        String normalized = normalizeEmail(email);
        if (users.existsByEmail(normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        return users.saveAndFlush(new AppUser(normalized, passwords.encode(password), role));
    }
    @Transactional(readOnly = true)
    public AccountResponse current(String email) {
        return users.findByEmail(email).map(AccountResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account unavailable"));
    }
    public static String normalizeEmail(String email) { return email.strip().toLowerCase(Locale.ROOT); }
    public record StaffRequest(
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email
        @jakarta.validation.constraints.Size(max = 254) String email,
        @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 12, max = 72) String password) {}
}
