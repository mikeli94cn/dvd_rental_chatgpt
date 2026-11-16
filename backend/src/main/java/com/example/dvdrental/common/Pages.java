package com.example.dvdrental.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class Pages {
    private Pages() {}
    public static PageRequest of(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be nonnegative and size must be between 1 and 100");
        }
        return PageRequest.of(page, size, Sort.by("id"));
    }
}
