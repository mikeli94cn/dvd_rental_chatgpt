package com.example.dvdrental.rental;

import com.example.dvdrental.common.*;
import com.example.dvdrental.customer.CustomerService;
import com.example.dvdrental.identity.UserRepository;
import com.example.dvdrental.inventory.*;
import com.example.dvdrental.pricing.PricingService;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class RentalService {
    private final RentalOrderRepository orders;
    private final CustomerService customers;
    private final InventoryService inventory;
    private final UserRepository users;
    private final PricingService pricing;
    private final Clock clock;
    public RentalService(RentalOrderRepository orders, CustomerService customers, InventoryService inventory,
            UserRepository users, PricingService pricing, Clock clock) {
        this.orders = orders; this.customers = customers; this.inventory = inventory;
        this.users = users; this.pricing = pricing; this.clock = clock;
    }
    @Transactional
    public RentalResponse create(String email, CreateRequest request) {
        if (request.copyIds().size() != new HashSet<>(request.copyIds()).size()) {
            throw new IllegalArgumentException("Duplicate DVD copy IDs are not allowed");
        }
        RentalOrder order = new RentalOrder(customers.find(email), clock.instant());
        LocalDate due = LocalDate.now(clock).plusDays(request.days());
        // Lock in a stable order. Availability is checked only after the database lock is held.
        for (long id : request.copyIds().stream().sorted().toList()) {
            DvdCopy copy = inventory.locked(id);
            copy.allocate();
            order.add(new RentalItem(order, copy, due, copy.getMovie().getDailyRate(), pricing.overdueDailyRate(),
                pricing.rentalCharge(copy.getMovie().getDailyRate(), request.days())));
        }
        orders.saveAndFlush(order);
        return response(order);
    }
    public RentalResponse findOwn(long id, String email) {
        return response(orders.findByIdAndCustomerUserEmail(id, email).orElseThrow(RentalService::notFound));
    }
    public PageResponse<RentalResponse> listOwn(String email, int page, int size) {
        return PageResponse.from(orders.findByCustomerUserEmail(email, Pages.of(page, size)).map(this::response));
    }
    public PageResponse<RentalResponse> listAll(int page, int size) {
        return PageResponse.from(orders.findAll(Pages.of(page, size)).map(this::response));
    }
    @Transactional
    public RentalResponse confirmReturn(long orderId, long itemId, String staffEmail, ReturnRequest request) {
        RentalOrder order = orders.findForUpdate(orderId).orElseThrow(RentalService::notFound);
        RentalItem item = order.getItems().stream().filter(i -> i.getId() == itemId).findFirst()
            .orElseThrow(RentalService::notFound);
        // All inventory writers participate in the same copy-row locking protocol.
        inventory.locked(item.getCopy().getId());
        var staff = users.findByEmail(staffEmail).orElseThrow(RentalService::notFound);
        item.complete(clock.instant(), pricing.overdueCharge(item.getOverdueRateSnapshot(), item.getDueDate(),
            LocalDate.now(clock)), staff, request.inspectionResult());
        orders.flush();
        return response(order);
    }
    private RentalResponse response(RentalOrder order) { return RentalResponse.from(order, pricing, LocalDate.now(clock)); }
    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found");
    }
    public record CreateRequest(@NotEmpty @Size(max = 10) List<@NotNull @Positive Long> copyIds, @Min(1) @Max(30) int days) {}
    public record ReturnRequest(@NotNull CopyStatus inspectionResult) {}
}
