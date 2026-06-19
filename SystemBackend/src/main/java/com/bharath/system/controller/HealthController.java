package com.bharath.system.controller;

import com.bharath.system.model.HealthLog;
import com.bharath.system.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/today")
    public ResponseEntity<HealthLog> getToday() {
        return healthService.getToday()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/week")
    public List<HealthLog> getWeek() {
        return healthService.getWeek();
    }

    @PostMapping("/log")
    public HealthLog saveLog(@RequestBody Map<String, Object> body) {
        return healthService.saveLog(body);
    }

    @GetMapping("/recommend")
    public Map<String, String> recommend() {
        return Map.of("plan", healthService.recommend());
    }
}
