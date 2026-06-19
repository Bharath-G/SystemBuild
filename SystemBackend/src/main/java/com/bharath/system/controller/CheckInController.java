package com.bharath.system.controller;

import com.bharath.system.model.DailyCheckIn;
import com.bharath.system.service.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @GetMapping("/today")
    public ResponseEntity<DailyCheckIn> getToday() {
        return checkInService.getToday()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public DailyCheckIn submit(@RequestBody Map<String, Object> body) {
        return checkInService.submitCheckIn(body);
    }
}
