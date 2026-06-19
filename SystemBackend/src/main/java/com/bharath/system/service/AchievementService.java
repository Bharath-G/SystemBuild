package com.bharath.system.service;

import com.bharath.system.model.Achievement;
import com.bharath.system.model.Quest;
import com.bharath.system.model.UserProfile;
import com.bharath.system.repository.AchievementRepository;
import com.bharath.system.repository.QuestRepository;
import com.bharath.system.repository.StreakRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final QuestRepository questRepository;
    private final StreakRepository streakRepository;
    private final ProfileService profileService;

    @PostConstruct
    @Transactional
    public void seedAchievements() {
        if (achievementRepository.count() > 0) return;

        List<Achievement> defaults = List.of(
                achievement("First Blood", "Complete your first quest.", "⚔", "first_quest"),
                achievement("Week Warrior", "Reach a 7-day streak.", "⚡", "streak_7"),
                achievement("Disciplined", "Reach level 10.", "▲", "level_10"),
                achievement("Quest Runner", "Complete 10 quests.", "◈", "quests_10"),
                achievement("Quest Master", "Complete 50 quests.", "◉", "quests_50"),
                achievement("Finance Initiate", "Complete your first FINANCE quest.", "₹", "finance_quest"),
                achievement("Health Warrior", "Complete 7 HEALTH quests.", "♦", "health_warrior")
        );
        achievementRepository.saveAll(defaults);
    }

    private Achievement achievement(String title, String description, String icon, String trigger) {
        Achievement a = new Achievement();
        a.setTitle(title);
        a.setDescription(description);
        a.setIcon(icon);
        a.setTriggerCondition(trigger);
        a.setUnlocked(false);
        return a;
    }

    public List<Achievement> getAll() {
        return achievementRepository.findAllByOrderByUnlockedDescTitleAsc();
    }

    @Transactional
    public List<String> checkAndUnlock(Quest completedQuest) {
        UserProfile profile = profileService.getOrCreateProfile();
        long completedCount = questRepository.countByCompletedTrue();
        long healthCount = questRepository.countByCategoryAndCompletedTrue("HEALTH");

        List<String> unlockedTitles = new ArrayList<>();
        unlockIf("first_quest", completedCount >= 1, unlockedTitles);
        unlockIf("quests_10", completedCount >= 10, unlockedTitles);
        unlockIf("quests_50", completedCount >= 50, unlockedTitles);
        unlockIf("level_10", profile.getLevel() >= 10, unlockedTitles);
        unlockIf("finance_quest", "FINANCE".equals(completedQuest.getCategory()), unlockedTitles);
        unlockIf("health_warrior", healthCount >= 7, unlockedTitles);

        streakRepository.findAll().stream()
                .filter(s -> s.getCurrentStreak() >= 7)
                .findAny()
                .ifPresent(s -> unlockIf("streak_7", true, unlockedTitles));

        return unlockedTitles;
    }

    private void unlockIf(String trigger, boolean condition, List<String> unlockedTitles) {
        if (!condition) return;
        achievementRepository.findByTriggerCondition(trigger).ifPresent(a -> {
            if (!a.isUnlocked()) {
                a.setUnlocked(true);
                a.setUnlockedAt(LocalDateTime.now());
                achievementRepository.save(a);
                unlockedTitles.add(a.getTitle());
            }
        });
    }
}
