package com.bharath.system.service;

import com.bharath.system.model.DailyInsight;
import com.bharath.system.model.UserProfile;
import com.bharath.system.repository.DailyInsightRepository;
import com.bharath.system.repository.UserProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Slf4j
public class InsightService {

    private final AIService aiService;
    private final UserProfileRepository userProfileRepository;
    private final DailyInsightRepository dailyInsightRepository;

    public InsightService(AIService aiService, UserProfileRepository userProfileRepository, DailyInsightRepository dailyInsightRepository) {
        this.aiService = aiService;
        this.userProfileRepository = userProfileRepository;
        this.dailyInsightRepository = dailyInsightRepository;
    }

    @Scheduled(cron = "0 0 6 * * ?") // Every day at 6 AM
    public void generateDailyInsight() {
        UserProfile profile = userProfileRepository.findById(1L).orElse(new UserProfile());
        String prompt = String.format(
            "Generate one sentence insight for %s today. Based on his level %d, primary goal: %s. Be direct. Be specific. Under 20 words.",
            profile.getName(), profile.getLevel(), profile.getPrimaryGoal());
        String insightText = aiService.chat(prompt); // Using general chat for now
        DailyInsight insight = new DailyInsight();
        insight.setInsightText(insightText);
        insight.setDate(LocalDate.now());
        dailyInsightRepository.save(insight);
    }

    public Optional<DailyInsight> getTodayInsight() {
        return dailyInsightRepository.findByDate(LocalDate.now());
    }
}