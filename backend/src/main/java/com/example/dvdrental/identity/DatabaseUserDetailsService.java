package com.example.dvdrental.identity;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public DatabaseUserDetailsService(UserRepository users) { this.users = users; }

    @Override @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        AppUser user = users.findByEmail(AccountService.normalizeEmail(email))
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return User.withUsername(user.getEmail()).password(user.getPasswordHash())
            .roles(user.getRole().name()).disabled(!user.isEnabled()).build();
    }
}
