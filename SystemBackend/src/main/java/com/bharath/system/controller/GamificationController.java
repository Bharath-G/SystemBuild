package com.bharath.system.controller;

import com.bharath.system.model.Achievement;
import com.bharath.system.service.AchievementService;
import com.bharath.system.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GamificationController {

    private final AchievementService achievementService;
    private final ProfileService profileService;

    public GamificationController(AchievementService achievementService, ProfileService profileService) {
        this.achievementService = achievementService;
        this.profileService = profileService;
    }

    @GetMapping("/achievements")
    public List<Achievement> getAchievements() {
        return achievementService.getAll();
    }

    @GetMapping("/profile/rank")
    public Map<String, Object> getRank() {
        return profileService.getRankInfo();
    }
}
