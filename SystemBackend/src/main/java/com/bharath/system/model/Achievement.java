package com.bharath.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private String icon;
    private boolean unlocked;
    private LocalDateTime unlockedAt;
    private String triggerCondition;
}
