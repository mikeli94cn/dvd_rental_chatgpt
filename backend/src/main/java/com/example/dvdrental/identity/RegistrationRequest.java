package com.example.dvdrental.identity;

import jakarta.validation.constraints.*;

public record RegistrationRequest(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 12, max = 72) String password,
    @NotBlank @Size(max = 120) String name) {}
