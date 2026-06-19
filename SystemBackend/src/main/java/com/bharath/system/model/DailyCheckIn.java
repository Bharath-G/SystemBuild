package com.bharath.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_checkins")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyCheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String workTasks;
    private String personalGoals;
    private String challenges;
    private int energyLevel;
    private int moodLevel;
    private int sleepHours;
    @Column(columnDefinition = "TEXT")
    private String aiDayPlan;
    private LocalDate date;
}
