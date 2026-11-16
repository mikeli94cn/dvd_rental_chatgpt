package com.example.dvdrental.reporting;

import com.example.dvdrental.common.PageResponse;
import com.example.dvdrental.rental.RentalResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff/reports")
public class ReportController {
    private final ReportService reports;
    public ReportController(ReportService reports) { this.reports = reports; }
    @GetMapping("/dashboard")
    public ReportService.Dashboard dashboard() { return reports.dashboard(); }
    @GetMapping("/overdue")
    public PageResponse<RentalResponse.ItemResponse> overdue(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) { return reports.overdue(page, size); }
}
