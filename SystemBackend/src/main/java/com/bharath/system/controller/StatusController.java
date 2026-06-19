package com.bharath.system.controller;

import com.bharath.system.service.GrowthTrackerService;
import com.bharath.system.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatusController {

    private final ProfileService profileService;
    private final GrowthTrackerService growthTrackerService;

    public StatusController(ProfileService profileService, GrowthTrackerService growthTrackerService) {
        this.profileService = profileService;
        this.growthTrackerService = growthTrackerService;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return profileService.getProfile();
    }

    @GetMapping("/roadmap")
    public Map<String, Object> getRoadmap() {
        return growthTrackerService.getRoadmap();
    }
}
