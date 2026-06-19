package com.bharath.system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MarketInsight {
    @Id
    private String id;
    
    private String title;
    private String summary;
    private String relevance;
    private String action;
    private String source;
    private String date;
    private String category;
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getRelevance() { return relevance; }
    public void setRelevance(String relevance) { this.relevance = relevance; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
