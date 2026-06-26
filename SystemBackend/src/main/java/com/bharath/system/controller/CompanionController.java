package com.bharath.system.controller;

import com.bharath.system.model.CompanionMemory;
import com.bharath.system.model.CompanionReport;
import com.bharath.system.repository.CompanionMemoryRepository;
import com.bharath.system.repository.CompanionReportRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/companion")
public class CompanionController {

    private final CompanionMemoryRepository memoryRepository;
    private final CompanionReportRepository reportRepository;

    public CompanionController(CompanionMemoryRepository memoryRepository,
                               CompanionReportRepository reportRepository) {
        this.memoryRepository = memoryRepository;
        this.reportRepository = reportRepository;
    }

    // GET /api/companion/memories — recent emotional memories (latest 20)
    @GetMapping("/memories")
    public ResponseEntity<List<CompanionMemory>> getRecentMemories() {
        return ResponseEntity.ok(memoryRepository.findTop20ByOrderByTimestampDesc());
    }

    // GET /api/companion/report — the latest weekly companion report
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> getLatestReport() {
        Optional<CompanionReport> report = reportRepository.findFirstByOrderByGeneratedDateDesc();
        if (report.isPresent()) {
            CompanionReport r = report.get();
            return ResponseEntity.ok(Map.of(
                "content", r.getContent(),
                "weekRange", r.getWeekRange() != null ? r.getWeekRange() : "",
                "generatedDate", r.getGeneratedDate().toString()
            ));
        }
        return ResponseEntity.ok(Map.of(
            "content", "",
            "weekRange", "",
            "generatedDate", ""
        ));
    }
}
