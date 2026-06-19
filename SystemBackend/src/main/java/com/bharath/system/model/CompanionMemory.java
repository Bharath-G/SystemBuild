package com.bharath.system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "companion_memory")
public class CompanionMemory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mode;
    @Column(columnDefinition = "TEXT")
    private String userMessage;
    @Column(columnDefinition = "TEXT")
    private String systemResponse;
    private String emotionalTone;     // STRESSED, MOTIVATED, CONFUSED, etc.
    private String keyInsight;
    private LocalDateTime timestamp = LocalDateTime.now();
    private LocalDate date = LocalDate.now();
}