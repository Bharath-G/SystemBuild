package com.bharath.system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @jakarta.persistence.Column(name = "\"value\"")
    private int value;
    private int maxValue;
    private String description;
    private String nextMilestone;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    
    public int getMaxValue() { return maxValue; }
    public void setMaxValue(int maxValue) { this.maxValue = maxValue; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getNextMilestone() { return nextMilestone; }
    public void setNextMilestone(String nextMilestone) { this.nextMilestone = nextMilestone; }
}
