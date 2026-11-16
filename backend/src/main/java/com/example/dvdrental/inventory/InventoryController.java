package com.example.dvdrental.inventory;

import com.example.dvdrental.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class InventoryController {
    private final InventoryService inventory;
    public InventoryController(InventoryService inventory) { this.inventory = inventory; }
    @GetMapping("/api/v1/movies/{id}/availability")
    public PageResponse<InventoryService.AvailableCopy> availability(@PathVariable long id,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        return inventory.availability(id, page, size);
    }
    @GetMapping("/api/v1/staff/copies")
    public PageResponse<InventoryService.CopyResponse> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) { return inventory.list(page, size); }
    @PostMapping("/api/v1/staff/copies") @ResponseStatus(HttpStatus.CREATED)
    public InventoryService.CopyResponse create(@Valid @RequestBody InventoryService.CopyRequest request) {
        return inventory.create(request);
    }
    @PutMapping("/api/v1/staff/copies/{id}/status")
    public InventoryService.CopyResponse status(@PathVariable long id,
            @Valid @RequestBody InventoryService.StatusRequest request) { return inventory.changeStatus(id, request.status()); }
}
