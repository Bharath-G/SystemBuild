package com.bharath.system.service;

import com.bharath.system.model.Streak;
import com.bharath.system.repository.StreakRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StreakService {

    private static final List<String> STREAK_TYPES = List.of(
            "EXERCISE", "LEARNING", "WAKEUP", "PRODUCTIVITY"
    );

    private final StreakRepository streakRepository;

    @PostConstruct
    @Transactional
    public void seedStreaks() {
        if (streakRepository.count() > 0) return;
        for (String type : STREAK_TYPES) {
            Streak streak = new Streak();
            streak.setType(type);
            streak.setCurrentStreak(0);
            streak.setLongestStreak(0);
            streakRepository.save(streak);
        }
    }

    public List<Streak> getAll() {
        return streakRepository.findAllByOrderByTypeAsc();
    }

    @Transactional
    public Streak checkIn(String type) {
        Streak streak = streakRepository.findByType(type.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Unknown streak type: " + type));

        LocalDate today = LocalDate.now();
        LocalDate last = streak.getLastChecked();

        if (last != null && last.equals(today)) {
            return streak;
        }

        if (last != null && last.equals(today.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            streak.setCurrentStreak(1);
        }

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        streak.setLastChecked(today);
        return streakRepository.save(streak);
    }
}
