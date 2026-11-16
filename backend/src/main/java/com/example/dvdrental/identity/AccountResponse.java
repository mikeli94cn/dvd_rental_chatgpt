package com.example.dvdrental.identity;

public record AccountResponse(Long id, String email, Role role) {
    public static AccountResponse from(AppUser user) {
        return new AccountResponse(user.getId(), user.getEmail(), user.getRole());
    }
}
