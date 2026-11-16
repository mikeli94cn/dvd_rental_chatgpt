package com.example.dvdrental.rental;

import com.example.dvdrental.common.PageResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class RentalController {
    private final RentalService rentals;
    public RentalController(RentalService rentals) { this.rentals = rentals; }
    @PostMapping("/api/v1/rentals") @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse create(Principal principal, @Valid @RequestBody RentalService.CreateRequest request) {
        return rentals.create(principal.getName(), request);
    }
    @GetMapping("/api/v1/rentals")
    public PageResponse<RentalResponse> own(Principal principal, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) { return rentals.listOwn(principal.getName(), page, size); }
    @GetMapping("/api/v1/rentals/{id}")
    public RentalResponse find(@PathVariable long id, Principal principal) { return rentals.findOwn(id, principal.getName()); }
    @GetMapping("/api/v1/staff/rentals")
    public PageResponse<RentalResponse> all(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) { return rentals.listAll(page, size); }
    @PostMapping("/api/v1/staff/rentals/{orderId}/items/{itemId}/return")
    public RentalResponse confirm(@PathVariable long orderId, @PathVariable long itemId, Principal principal,
            @Valid @RequestBody RentalService.ReturnRequest request) {
        return rentals.confirmReturn(orderId, itemId, principal.getName(), request);
    }
}
