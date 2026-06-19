package com.bharath.system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class WorkTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String priority;      // P1, P2, P3
    private String status;        // TODO, IN_PROGRESS, BLOCKED, DONE
    private String blocker;
    private int estimatedHours;
    private LocalDate date = LocalDate.now();
    private LocalDateTime completedAt;
}