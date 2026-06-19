package com.bharath.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "quests")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Quest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private int xpReward;
    private String difficulty;   
    private String category;     
    private String type;         
    private boolean completed;
    private String consequence;
    private LocalDateTime deadline;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;
}