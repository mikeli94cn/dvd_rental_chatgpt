package com.example.dvdrental.identity;

import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AccountService accounts;
    public AuthController(AccountService accounts) { this.accounts = accounts; }

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse register(@Valid @RequestBody RegistrationRequest request) {
        return accounts.register(request);
    }
    @GetMapping("/me")
    public AccountResponse me(Principal principal) { return accounts.current(principal.getName()); }
    public record CsrfResponse(String headerName, String token) {}
}
