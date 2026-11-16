package com.example.dvdrental.reporting;

import com.example.dvdrental.common.*;
import com.example.dvdrental.customer.CustomerRepository;
import com.example.dvdrental.inventory.*;
import com.example.dvdrental.pricing.PricingService;
import com.example.dvdrental.rental.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {
    private final CopyRepository copies;
    private final CustomerRepository customers;
    private final RentalItemRepository items;
    private final PricingService pricing;
    private final Clock clock;
    public ReportService(CopyRepository copies, CustomerRepository customers, RentalItemRepository items,
            PricingService pricing, Clock clock) {
        this.copies = copies; this.customers = customers; this.items = items; this.pricing = pricing; this.clock = clock;
    }
    public Dashboard dashboard() {
        return new Dashboard(copies.count(), copies.countByStatus(CopyStatus.AVAILABLE), customers.count(),
            items.countByStatus(RentalItemStatus.ACTIVE), items.countByStatusAndDueDateBefore(RentalItemStatus.ACTIVE, LocalDate.now(clock)));
    }
    public PageResponse<RentalResponse.ItemResponse> overdue(int page, int size) {
        LocalDate today = LocalDate.now(clock);
        return PageResponse.from(items.findByStatusAndDueDateBefore(RentalItemStatus.ACTIVE, today, Pages.of(page, size))
            .map(i -> RentalResponse.ItemResponse.from(i, pricing, today)));
    }
    public record Dashboard(long totalCopies, long availableCopies, long customers, long activeItems, long overdueItems) {}
}
