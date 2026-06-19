package com.bharath.system.service;

import com.bharath.system.config.SystemProperties;
import com.bharath.system.model.HealthLog;
import com.bharath.system.repository.HealthLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final HealthLogRepository healthLogRepository;
    private final AIService aiService;
    private final SystemProperties props;
    private final StreakService streakService;

    public Optional<HealthLog> getToday() {
        return healthLogRepository.findByDate(LocalDate.now());
    }

    public List<HealthLog> getWeek() {
        LocalDate today = LocalDate.now();
        return healthLogRepository.findByDateBetweenOrderByDateDesc(today.minusDays(6), today);
    }

    @Transactional
    public HealthLog saveLog(Map<String, Object> body) {
        LocalDate today = LocalDate.now();
        HealthLog log = healthLogRepository.findByDate(today).orElse(new HealthLog());
        log.setDate(today);
        log.setMorningStretch(boolVal(body.get("morningStretch")));
        log.setExerciseDone(boolVal(body.get("exerciseDone")));
        log.setExerciseMinutes(intVal(body.get("exerciseMinutes"), 0));
        log.setExerciseType(stringVal(body.get("exerciseType")));
        log.setPainLevel(stringVal(body.getOrDefault("painLevel", "NONE")).toUpperCase());
        log.setWaterGlasses(intVal(body.get("waterGlasses"), 0));
        log.setSleepHours(intVal(body.get("sleepHours"), 7));
        log.setNotes(stringVal(body.get("notes")));

        if (log.isExerciseDone() || log.isMorningStretch()) {
            streakService.checkIn("EXERCISE");
        }

        return healthLogRepository.save(log);
    }

    public String recommend() {
        HealthLog today = getToday().orElse(new HealthLog());
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String lastExercise = healthLogRepository.findByDate(yesterday)
                .map(l -> l.isExerciseDone() ? l.getExerciseType() + " (" + l.getExerciseMinutes() + " min)" : "none")
                .orElse("unknown");

        String prompt = String.format("""
            %s has Ankylosing Spondylitis. Low-impact only. No running, no heavy lifting.
            Today's pain level: %s. Sleep: %dh. Last exercise: %s.
            Generate a 15-20 minute mobility routine for today.
            Include: warm-up, 3-4 AS-friendly exercises, cool down.
            Be specific with duration per exercise. Under 120 words.
            """,
                props.getName(),
                today.getPainLevel() != null ? today.getPainLevel() : "NONE",
                today.getSleepHours() > 0 ? today.getSleepHours() : 7,
                lastExercise
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

    private boolean boolVal(Object value) {
        if (value instanceof Boolean b) return b;
        if (value != null) return Boolean.parseBoolean(value.toString());
        return false;
    }
}
