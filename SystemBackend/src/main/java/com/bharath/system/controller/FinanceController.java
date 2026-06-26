package com.bharath.system.controller;

import com.bharath.system.model.FinancialInsight;
import com.bharath.system.model.FinancialLog;
import com.bharath.system.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/log")
    public ResponseEntity<FinancialLog> logFinance(@RequestBody FinancialLog log) {
        return ResponseEntity.ok(financeService.logFinance(log));
    }

    @GetMapping("/logs/month")
    public ResponseEntity<Map<String, Object>> getMonthlyLogs(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();

        List<FinancialLog> logs = financeService.getMonthlyLogs(y, m);
        Map<String, Double> summary = financeService.getMonthlySummary(y, m);

        return ResponseEntity.ok(Map.of("logs", logs, "summary", summary));
    }

    @GetMapping("/insights")
    public ResponseEntity<List<FinancialInsight>> getInsights() {
        return ResponseEntity.ok(financeService.getInsights());
    }

    @PostMapping("/insights/generate")
    public ResponseEntity<List<FinancialInsight>> generateInsights() {
        return ResponseEntity.ok(financeService.generateInsights());
    }
}
