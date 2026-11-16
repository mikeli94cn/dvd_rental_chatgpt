package com.example.dvdrental.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class DvdCopyTest {
    @Test void rentalCanOnlyResolveThroughInspection() {
        DvdCopy copy = new DvdCopy(null, "copy");
        copy.allocate();
        assertThrows(ResponseStatusException.class, copy::allocate);
        assertThrows(ResponseStatusException.class, () -> copy.changeStatus(CopyStatus.AVAILABLE));
        assertThrows(IllegalArgumentException.class, () -> copy.confirmReturn(CopyStatus.RENTED));
        copy.confirmReturn(CopyStatus.DAMAGED);
        assertThrows(ResponseStatusException.class, copy::allocate);
        copy.changeStatus(CopyStatus.MAINTENANCE);
        copy.changeStatus(CopyStatus.AVAILABLE);
        copy.allocate();
        assertEquals(CopyStatus.RENTED, copy.getStatus());
    }
    @Test void retiredCopiesCannotBeReactivated() {
        DvdCopy copy = new DvdCopy(null, "copy");
        copy.changeStatus(CopyStatus.RETIRED);
        assertThrows(ResponseStatusException.class, () -> copy.changeStatus(CopyStatus.AVAILABLE));
    }
}
