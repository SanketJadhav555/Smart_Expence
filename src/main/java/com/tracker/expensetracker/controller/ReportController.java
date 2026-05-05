package com.tracker.expensetracker.controller;

import com.tracker.expensetracker.service.ExportService;
import com.tracker.expensetracker.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ExportService exportService;

    // Monthly report data
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        if (month == null) month = LocalDate.now().getMonthValue();
        if (year  == null) year  = LocalDate.now().getYear();
        return ResponseEntity.ok(reportService.getMonthlyReport(month, year));
    }

    // Yearly summary
    @GetMapping("/yearly")
    public ResponseEntity<Map<String, Object>> getYearlySummary(
            @RequestParam(required = false) Integer year) {
        if (year == null) year = LocalDate.now().getYear();
        return ResponseEntity.ok(reportService.getYearlySummary(year));
    }

    // ── Excel export ──────────────────────────────────────────────────────────
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        if (month == null) month = LocalDate.now().getMonthValue();
        if (year  == null) year  = LocalDate.now().getYear();

        try {
            byte[] data = exportService.exportToExcel(month, year);
            String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            String filename  = "expense-report-" + monthName + "-" + year + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ── PDF export ────────────────────────────────────────────────────────────
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        if (month == null) month = LocalDate.now().getMonthValue();
        if (year  == null) year  = LocalDate.now().getYear();

        try {
            byte[] data = exportService.exportToPdf(month, year);
            String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            String filename  = "expense-report-" + monthName + "-" + year + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
