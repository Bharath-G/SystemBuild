package com.bharath.system.service;

import com.bharath.system.config.SystemProperties;
import com.bharath.system.model.DailyCheckIn;
import com.bharath.system.repository.DailyCheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final DailyCheckInRepository checkInRepository;
    private final AIService aiService;
    private final SystemProperties props;

    public Optional<DailyCheckIn> getToday() {
        return checkInRepository.findByDate(LocalDate.now());
    }

    @Transactional
    public DailyCheckIn submitCheckIn(Map<String, Object> body) {
        LocalDate today = LocalDate.now();
        DailyCheckIn checkIn = checkInRepository.findByDate(today).orElse(new DailyCheckIn());
        checkIn.setDate(today);
        checkIn.setWorkTasks(stringVal(body.get("workTasks")));
        checkIn.setPersonalGoals(stringVal(body.get("personalGoals")));
        checkIn.setChallenges(stringVal(body.get("challenges")));
        checkIn.setEnergyLevel(intVal(body.get("energyLevel"), 3));
        checkIn.setMoodLevel(intVal(body.get("moodLevel"), 3));
        checkIn.setSleepHours(intVal(body.get("sleepHours"), 7));

        String dayPlan = generateDayPlan(checkIn);
        checkIn.setAiDayPlan(dayPlan);
        return checkInRepository.save(checkIn);
    }

    private String generateDayPlan(DailyCheckIn checkIn) {
        String prompt = String.format("""
            %s's check-in for today:
            Work tasks: %s
            Personal goals: %s
            Challenges: %s
            Energy: %d/5, Mood: %d/5, Sleep: %dh

            Generate a prioritized day plan. Be specific. Name time blocks.
            If energy < 3: reduce cognitive load tasks, prioritize lighter work.
            If sleep < 6: flag it. Recommend one recovery action.
            Format: numbered list. Under 150 words.
            """,
                props.getName(),
                checkIn.getWorkTasks(),
                checkIn.getPersonalGoals(),
                checkIn.getChallenges(),
                checkIn.getEnergyLevel(),
                checkIn.getMoodLevel(),
                checkIn.getSleepHours()
        );
        return aiService.chatWithMode(prompt, "SYSTEM");
    }

    private String stringVal(Object value) {
        return value == null ? "" : value.toString();
    }

    private int intVal(Object value, int fallback) {
        if (value instanceof Number n) return n.intValue();
        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }
}
