package com.bharath.system.service;

import com.bharath.system.model.CompanionMemory;
import com.bharath.system.model.UserProfile;
import com.bharath.system.repository.CompanionMemoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class NotificationService {

    private final ProfileService profileService;
    private final CompanionMemoryRepository memoryRepository;
    private final AIService aiService;

    public NotificationService(ProfileService profileService,
                               CompanionMemoryRepository memoryRepository,
                               AIService aiService) {
        this.profileService = profileService;
        this.memoryRepository = memoryRepository;
        this.aiService = aiService;
    }

    // Every day at 9 AM, check if user has been quiet for 3+ days
    @Scheduled(cron = "0 0 9 * * *")
    public void checkSilence() {
        Optional<UserProfile> p = profileService.findProfile();
        if (p.isEmpty()) return;
        
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        List<CompanionMemory> recentMemories = memoryRepository.findByDateBetweenOrderByTimestampDesc(threeDaysAgo, LocalDate.now());
        
        if (recentMemories.isEmpty()) {
            String msg = String.format("[ SYSTEM ] You have been quiet, %s. 3 days without checking in.\nWhat are you avoiding?", p.get().getName());
            pushNotification(msg);
        }
    }

    // Every Sunday at 6 PM, week end check
    @Scheduled(cron = "0 0 18 * * SUN")
    public void weekEndCheck() {
        Optional<UserProfile> p = profileService.findProfile();
        if (p.isEmpty()) return;
        
        String msg = String.format("[ SYSTEM ] Week ends in a few hours, %s.\nDid you become who you said you would this week?", p.get().getName());
        pushNotification(msg);
    }

    public void triggerStreakBreakNotification(String reason) {
        Optional<UserProfile> p = profileService.findProfile();
        if (p.isEmpty()) return;
        
        String msg = String.format("[ SYSTEM ] Your streak broke today, %s. Not a judgment.\nA question: what happened?", p.get().getName());
        pushNotification(msg);
    }
    
    public void triggerLowEnergyFollowUp() {
        Optional<UserProfile> p = profileService.findProfile();
        if (p.isEmpty()) return;
        
        String msg = String.format("[ SYSTEM ] You started today at low energy, %s.\nIt is 3 PM. How are you now?", p.get().getName());
        pushNotification(msg);
    }

    private void pushNotification(String message) {
        // In a real app this would connect to Firebase Cloud Messaging or APNS.
        // For the local system, we log it, and we could also insert it as a pending system message.
        log.info("PUSH NOTIFICATION TO DEVICE: {}", message);
    }
}
