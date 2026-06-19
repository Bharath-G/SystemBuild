package com.bharath.system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class LearningLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date = LocalDate.now();
    private String topic;
    private String source;
    private int minutesSpent;
    @Column(columnDefinition = "TEXT")
    private String summary;
    private int selfRating;
}