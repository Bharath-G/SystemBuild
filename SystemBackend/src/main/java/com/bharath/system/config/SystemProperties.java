package com.bharath.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "system.user")
public class SystemProperties {
    private String name = "User";
    private int age = 25;
    private String location = "Unknown";
    private String role = "Developer";
    private String employer = "Unknown";
    private String experience = "0 years";
    private String healthStatus = "None";
    private String background = "Building from zero.";
    private String goals = "Define your goals here.";
    private String marketContext = "Update market context.";
    private String gaps = "No known gaps.";
    
    // Derived properties for UI profile
    private String className = "Developer";
    private int level = 1;
    private Long xp = 0L;
    private Long xpThreshold = 1000L;
    private String arc = "Beginning Arc";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmployer() { return employer; }
    public void setEmployer(String employer) { this.employer = employer; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }

    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }

    public String getMarketContext() { return marketContext; }
    public void setMarketContext(String marketContext) { this.marketContext = marketContext; }

    public String getGaps() { return gaps; }
    public void setGaps(String gaps) { this.gaps = gaps; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public Long getXp() { return xp; }
    public void setXp(Long xp) { this.xp = xp; }

    public Long getXpThreshold() { return xpThreshold; }
    public void setXpThreshold(Long xpThreshold) { this.xpThreshold = xpThreshold; }

    public String getArc() { return arc; }
    public void setArc(String arc) { this.arc = arc; }
}
