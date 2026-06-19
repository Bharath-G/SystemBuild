package com.bharath.system.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "streaks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Streak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastChecked;
}
