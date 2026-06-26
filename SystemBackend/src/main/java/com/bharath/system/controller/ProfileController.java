package com.bharath.system.controller;

import com.bharath.system.model.UserProfile;
import com.bharath.system.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile() {
        Map<String, Object> profile = profileService.getProfile();
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/setup")
    public ResponseEntity<UserProfile> setupProfile(@RequestBody UserProfile profile) {
        return ResponseEntity.ok(profileService.setupProfile(profile));
    }

    @PutMapping
    public ResponseEntity<UserProfile> updateProfile(@RequestBody UserProfile profile) {
        return ResponseEntity.ok(profileService.setupProfile(profile));
    }

    @GetMapping("/setup-status")
    public ResponseEntity<Map<String, Boolean>> getSetupStatus() {
        boolean complete = profileService.getSetupComplete();
        return ResponseEntity.ok(Map.of("complete", complete));
    }
}