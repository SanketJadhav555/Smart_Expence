package com.tracker.expensetracker.controller;

import com.tracker.expensetracker.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Monthly report
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();
        return ResponseEntity.ok(reportService.getMonthlyReport(month, year));
    }

    // Yearly summary
    @GetMapping("/yearly")
    public ResponseEntity<Map<String, Object>> getYearlySummary(
            @RequestParam(required = false) Integer year) {
        if (year == null) year = LocalDate.now().getYear();
        return ResponseEntity.ok(reportService.getYearlySummary(year));
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportReport() {
        return ResponseEntity.ok("Export initiated. In a full production app, this would use Apache POI/iText to stream a PDF/Excel file.");
    }
}
