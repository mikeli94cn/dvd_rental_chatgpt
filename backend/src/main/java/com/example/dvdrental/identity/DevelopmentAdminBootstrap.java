package com.example.dvdrental.identity;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevelopmentAdminBootstrap implements ApplicationRunner {
    private final AccountService accounts;
    private final String email;
    private final String password;
    public DevelopmentAdminBootstrap(AccountService accounts,
            @Value("${DEV_ADMIN_EMAIL:}") String email, @Value("${DEV_ADMIN_PASSWORD:}") String password) {
        this.accounts = accounts; this.email = email; this.password = password;
    }
    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) { return; }
        var validator = jakarta.validation.Validation.buildDefaultValidatorFactory();
        try (validator) {
            var request = new AccountService.StaffRequest(email, password);
            if (!validator.getValidator().validate(request).isEmpty()) {
                throw new IllegalArgumentException("Valid DEV_ADMIN_EMAIL and DEV_ADMIN_PASSWORD are required");
            }
        }
        accounts.bootstrapAdmin(email, password);
    }
}
