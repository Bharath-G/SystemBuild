package com.bharath.system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Data
public class UserProfile {
    @Id
    private Long id = 1L; // Always 1L for a single-user app
    private String name;
    private Integer age;
    private String location;
    @Column(name = "role_title")
    private String currentRole;
    private String employer;
    private Integer experienceYears;
    private String primaryGoal;
    private String secondaryGoals;          // comma separated
    private String healthConditions;        // e.g. "Ankylosing Spondylitis"
    private String financialStatus;         // BUILDING, STABLE, GROWING
    private Integer currentSalaryLPA;
    private Integer targetSalaryLPA;
    private String skills;                  // comma separated
    private String certifications;          // comma separated
    private Integer javaSkill;                  // 0-100
    private Integer systemDesignSkill;
    private Integer cloudSkill;
    private Integer communicationSkill;
    private Integer financialIQSkill;
    private Integer level = 1;
    private Long xp = 0L;
    private Long xpThreshold = 1000L;
    private String titles = "Initiate"; // Updated via Phase 0
    private String currentArc = "Arrival";  // Standard Phase 0 Arc
    private String selectedModel = "mistral"; 
    private boolean setupComplete = false;
    private String className = "Developer"; // Default class, can be updated during setup
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}