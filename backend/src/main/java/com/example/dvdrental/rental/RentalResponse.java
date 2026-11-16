package com.example.dvdrental.rental;

import com.example.dvdrental.pricing.PricingService;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public record RentalResponse(Long id, Long customerId, Instant createdAt, String status, String currency,
        BigDecimal rentalTotal, BigDecimal overdueTotal, List<ItemResponse> items) {
    public static RentalResponse from(RentalOrder order, PricingService pricing, LocalDate today) {
        List<ItemResponse> items = order.getItems().stream().map(i -> ItemResponse.from(i, pricing, today)).toList();
        return new RentalResponse(order.getId(), order.getCustomer().getId(), order.getCreatedAt(), order.getStatus(), "GBP",
            items.stream().map(ItemResponse::rentalCharge).reduce(new BigDecimal("0.00"), BigDecimal::add),
            items.stream().map(ItemResponse::overdueCharge).reduce(new BigDecimal("0.00"), BigDecimal::add), items);
    }
    public record ItemResponse(Long id, Long orderId, Long copyId, String movieTitle, LocalDate dueDate, Instant returnedAt,
            RentalItemStatus status, BigDecimal dailyRate, BigDecimal overdueDailyRate, BigDecimal rentalCharge,
            BigDecimal overdueCharge, boolean overdueEstimate) {
        public static ItemResponse from(RentalItem item, PricingService pricing, LocalDate today) {
            boolean active = item.getStatus() == RentalItemStatus.ACTIVE;
            return new ItemResponse(item.getId(), item.getOrder().getId(), item.getCopy().getId(), item.getCopy().getMovie().getTitle(),
                item.getDueDate(), item.getReturnedAt(), item.getStatus(), item.getDailyRateSnapshot(), item.getOverdueRateSnapshot(),
                item.getRentalCharge(), active ? pricing.overdueCharge(item.getOverdueRateSnapshot(), item.getDueDate(), today)
                    : item.getOverdueCharge(), active);
        }
    }
}
