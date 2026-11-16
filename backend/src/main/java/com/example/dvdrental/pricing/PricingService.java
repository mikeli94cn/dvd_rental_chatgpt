package com.example.dvdrental.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PricingService {
    private final BigDecimal overdueDailyRate;
    public PricingService(@Value("${rental.overdue-daily-rate:1.50}") BigDecimal overdueDailyRate) {
        if (overdueDailyRate == null || overdueDailyRate.signum() < 0 || overdueDailyRate.scale() > 2
                || overdueDailyRate.compareTo(new BigDecimal("9999.99")) > 0) {
            throw new IllegalArgumentException("Overdue daily rate must be between 0 and 9999.99 with at most two decimals");
        }
        this.overdueDailyRate = overdueDailyRate.setScale(2);
    }
    public BigDecimal overdueDailyRate() { return overdueDailyRate; }
    public BigDecimal rentalCharge(BigDecimal dailyRate, int days) {
        if (days < 1 || days > 30) { throw new IllegalArgumentException("Rental duration must be between 1 and 30 days"); }
        return dailyRate.multiply(BigDecimal.valueOf(days)).setScale(2);
    }
    public BigDecimal overdueCharge(BigDecimal rate, LocalDate due, LocalDate returned) {
        long days = Math.max(0, ChronoUnit.DAYS.between(due, returned));
        return rate.multiply(BigDecimal.valueOf(days)).setScale(2);
    }
}
