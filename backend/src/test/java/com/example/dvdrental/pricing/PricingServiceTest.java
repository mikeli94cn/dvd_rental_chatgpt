package com.example.dvdrental.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {
    private final PricingService pricing = new PricingService(new BigDecimal("1.50"));
    @Test void calculatesExactDecimalCharges() {
        assertEquals(new BigDecimal("0.30"), pricing.rentalCharge(new BigDecimal("0.10"), 3));
        assertEquals(new BigDecimal("4.50"), pricing.overdueCharge(new BigDecimal("1.50"),
            LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4)));
    }
    @Test void earlyAndOnTimeReturnsHaveNoOverdueCharge() {
        LocalDate due = LocalDate.of(2026, 11, 1);
        assertEquals(new BigDecimal("0.00"), pricing.overdueCharge(new BigDecimal("1.50"), due, due));
        assertEquals(new BigDecimal("0.00"), pricing.overdueCharge(new BigDecimal("1.50"), due, due.minusDays(1)));
    }
    @Test void rejectsInvalidConfigurationAndPeriods() {
        assertThrows(IllegalArgumentException.class, () -> pricing.rentalCharge(BigDecimal.ONE, 0));
        assertThrows(IllegalArgumentException.class, () -> pricing.rentalCharge(BigDecimal.ONE, 31));
        assertThrows(IllegalArgumentException.class, () -> new PricingService(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> new PricingService(new BigDecimal("0.001")));
    }
}
