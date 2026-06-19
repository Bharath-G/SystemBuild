package com.bharath.system.controller;

import com.bharath.system.model.Streak;
import com.bharath.system.service.StreakService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/streaks")
public class StreakController {

    private final StreakService streakService;

    public StreakController(StreakService streakService) {
        this.streakService = streakService;
    }

    @GetMapping
    public List<Streak> getStreaks() {
        return streakService.getAll();
    }

    @PostMapping("/{type}/checkin")
    public Streak checkIn(@PathVariable String type) {
        return streakService.checkIn(type);
    }
}
