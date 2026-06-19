package com.bharath.system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class HealthLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date = LocalDate.now();
    private boolean exerciseDone;
    private int exerciseMinutes;
    private String exerciseType;
    private int waterGlasses;
    private int sleepHours;
    private boolean morningStretch; // AS-specific
    private String painLevel;       // NONE, MILD, MODERATE, HIGH
    @Column(columnDefinition = "TEXT")
    private String notes;
}