package com.bharath.system.service;

import com.bharath.system.config.SystemProperties;
import com.bharath.system.model.UserProfile;
import com.bharath.system.repository.UserProfileRepository;
import com.bharath.system.util.RankUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfileService {

    private final SystemProperties props;
    private final UserProfileRepository profileRepository;

    public ProfileService(SystemProperties props, UserProfileRepository profileRepository) {
        this.props = props;
        this.profileRepository = profileRepository;
    }

    public Optional<UserProfile> findProfile() {
        return profileRepository.findById(1L);
    }

    public UserProfile getOrCreateProfile() {
        return findProfile()
            .filter(UserProfile::isSetupComplete)
            .orElseThrow(() -> new IllegalStateException("Profile not initialized. Complete setup first."));
    }

    // PHASE 2: Load from DB dynamically — no hardcoded values
    public String buildSystemPrompt() {
        UserProfile p = getOrCreateProfile();
        String name = p.getName() != null ? p.getName() : "User";
        String role = p.getCurrentRole() != null ? p.getCurrentRole() : "Developer";
        String employer = p.getEmployer() != null ? p.getEmployer() : "Unknown";
        int exp = p.getExperienceYears() != null ? p.getExperienceYears() : 0;
        String location = p.getLocation() != null ? p.getLocation() : "Unknown";
        int level = p.getLevel() != null ? p.getLevel() : 1;
        String className = p.getClassName() != null ? p.getClassName() : "Developer";
        int java = p.getJavaSkill() != null ? p.getJavaSkill() : 0;
        int sd = p.getSystemDesignSkill() != null ? p.getSystemDesignSkill() : 0;
        int cloud = p.getCloudSkill() != null ? p.getCloudSkill() : 0;
        int finance = p.getFinancialIQSkill() != null ? p.getFinancialIQSkill() : 0;
        int comm = p.getCommunicationSkill() != null ? p.getCommunicationSkill() : 0;
        String goal = p.getPrimaryGoal() != null ? p.getPrimaryGoal() : "Define your goals.";
        String health = p.getHealthConditions() != null ? p.getHealthConditions() : "None reported";

        return String.format(
            "You are THE SYSTEM bound to %s.\n" +
            "Role: %s at %s | Experience: %d years\n" +
            "Location: %s | Level: %d | Class: %s\n" +
            "Stats: Java %d, SystemDesign %d, Cloud %d, Finance %d, Comm %d\n" +
            "Goals: %s\n" +
            "Health: %s\n" +
            "RULES: Truth first. Flaws before support. Under 100 words unless asked.",
            name, role, employer, exp, location, level, className,
            java, sd, cloud, finance, comm, goal, health
        );
    }

    public Map<String, Object> getProfile() {
        Optional<UserProfile> profileOpt = findProfile().filter(UserProfile::isSetupComplete);
        if (profileOpt.isEmpty()) {
            return null;
        }
        UserProfile profile = profileOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("name", profile.getName());
        result.put("className", profile.getClassName());
        result.put("level", profile.getLevel());
        result.put("xp", profile.getXp());
        result.put("xpThreshold", profile.getXpThreshold());
        result.put("arc", profile.getCurrentArc());
        result.put("rank", RankUtil.getRank(profile.getLevel()));
        result.put("rankTagline", RankUtil.getRankTagline(profile.getLevel()));
        result.put("titles", parseTitles(profile.getTitles()));
        result.put("primaryGoal", profile.getPrimaryGoal());
        result.put("healthConditions", profile.getHealthConditions());
        result.put("javaSkill", profile.getJavaSkill());
        result.put("systemDesignSkill", profile.getSystemDesignSkill());
        result.put("cloudSkill", profile.getCloudSkill());
        result.put("communicationSkill", profile.getCommunicationSkill());
        result.put("financialIQSkill", profile.getFinancialIQSkill());
        result.put("selectedModel", profile.getSelectedModel());
        result.put("setupComplete", profile.isSetupComplete());
        result.put("stats", buildStats(profile));
        return result;
    }

    private Object[] buildStats(UserProfile p) {
        if (p.getJavaSkill() == null) return new Object[]{};
        return new Object[]{
            Map.of("name", "JAVA / SPRING", "value", p.getJavaSkill(), "maxValue", 100,
                   "description", "Java & Spring Boot proficiency", "nextMilestone", "90+ for Senior"),
            Map.of("name", "SYSTEM DESIGN", "value", p.getSystemDesignSkill() != null ? p.getSystemDesignSkill() : 0, "maxValue", 100,
                   "description", "Architecture & design skills", "nextMilestone", "80+ for Lead"),
            Map.of("name", "CLOUD / AWS", "value", p.getCloudSkill() != null ? p.getCloudSkill() : 0, "maxValue", 100,
                   "description", "Cloud infrastructure skills", "nextMilestone", "AWS SAA cert"),
            Map.of("name", "COMMUNICATION", "value", p.getCommunicationSkill() != null ? p.getCommunicationSkill() : 0, "maxValue", 100,
                   "description", "Communication & leadership", "nextMilestone", "Lead a project"),
            Map.of("name", "FINANCIAL IQ", "value", p.getFinancialIQSkill() != null ? p.getFinancialIQSkill() : 0, "maxValue", 100,
                   "description", "Financial intelligence", "nextMilestone", "Start SIP investment"),
        };
    }

    public Map<String, Object> getRankInfo() {
        UserProfile profile = getOrCreateProfile();
        Map<String, Object> result = new HashMap<>();
        result.put("level", profile.getLevel());
        result.put("xp", profile.getXp());
        result.put("xpThreshold", profile.getXpThreshold());
        result.put("rank", RankUtil.getRank(profile.getLevel()));
        result.put("rankTagline", RankUtil.getRankTagline(profile.getLevel()));
        return result;
    }

    @Transactional
    public Map<String, Object> addXp(int amount) {
        UserProfile profile = getOrCreateProfile();
        long newXp = profile.getXp() + amount;
        int level = profile.getLevel();
        long threshold = profile.getXpThreshold();
        boolean leveledUp = false;

        while (newXp >= threshold) {
            newXp -= threshold;
            level++;
            threshold = (long) Math.floor(threshold * 1.2);
            leveledUp = true;
        }

        profile.setXp(newXp);
        profile.setLevel(level);
        profile.setXpThreshold(threshold);
        profileRepository.save(profile);

        Map<String, Object> result = new HashMap<>();
        result.put("xpGained", amount);
        result.put("level", level);
        result.put("xp", newXp);
        result.put("xpThreshold", threshold);
        result.put("leveledUp", leveledUp);
        result.put("rank", RankUtil.getRank(level));
        return result;
    }

    private String[] parseTitles(String titlesJson) {
        if (titlesJson == null || titlesJson.isBlank()) return new String[]{"Initiate"};
        String cleaned = titlesJson.replace("[", "").replace("]", "").replace("\"", "");
        if (cleaned.isBlank()) return new String[]{"Initiate"};
        return cleaned.split("\\s*,\\s*");
    }

    @Transactional
    public UserProfile setupProfile(UserProfile incoming) {
        UserProfile existing = findProfile().orElseGet(UserProfile::new);
        existing.setId(1L);
        if (existing.getLevel() == null) existing.setLevel(1);
        if (existing.getXp() == null) existing.setXp(0L);
        if (existing.getXpThreshold() == null) existing.setXpThreshold(1000L);
        if (existing.getClassName() == null) existing.setClassName("Initiate");
        if (existing.getCurrentArc() == null) existing.setCurrentArc("Arrival");
        if (existing.getTitles() == null) existing.setTitles("Initiate");
        if (incoming.getName() != null) existing.setName(incoming.getName());
        if (incoming.getAge() != null) existing.setAge(incoming.getAge());
        if (incoming.getLocation() != null) existing.setLocation(incoming.getLocation());
        if (incoming.getCurrentRole() != null) existing.setCurrentRole(incoming.getCurrentRole());
        if (incoming.getEmployer() != null) existing.setEmployer(incoming.getEmployer());
        if (incoming.getExperienceYears() != null) existing.setExperienceYears(incoming.getExperienceYears());
        if (incoming.getPrimaryGoal() != null) existing.setPrimaryGoal(incoming.getPrimaryGoal());
        if (incoming.getSecondaryGoals() != null) existing.setSecondaryGoals(incoming.getSecondaryGoals());
        if (incoming.getHealthConditions() != null) existing.setHealthConditions(incoming.getHealthConditions());
        if (incoming.getFinancialStatus() != null) existing.setFinancialStatus(incoming.getFinancialStatus());
        if (incoming.getCurrentSalaryLPA() != null) existing.setCurrentSalaryLPA(incoming.getCurrentSalaryLPA());
        if (incoming.getTargetSalaryLPA() != null) existing.setTargetSalaryLPA(incoming.getTargetSalaryLPA());
        if (incoming.getSkills() != null) existing.setSkills(incoming.getSkills());
        if (incoming.getCertifications() != null) existing.setCertifications(incoming.getCertifications());
        if (incoming.getJavaSkill() != null) existing.setJavaSkill(incoming.getJavaSkill());
        if (incoming.getSystemDesignSkill() != null) existing.setSystemDesignSkill(incoming.getSystemDesignSkill());
        if (incoming.getCloudSkill() != null) existing.setCloudSkill(incoming.getCloudSkill());
        if (incoming.getCommunicationSkill() != null) existing.setCommunicationSkill(incoming.getCommunicationSkill());
        if (incoming.getFinancialIQSkill() != null) existing.setFinancialIQSkill(incoming.getFinancialIQSkill());
        if (incoming.getSelectedModel() != null) existing.setSelectedModel(incoming.getSelectedModel());
        if (incoming.getClassName() != null) existing.setClassName(incoming.getClassName());
        existing.setSetupComplete(true);
        return profileRepository.save(existing);
    }

    public boolean isSetupComplete() {
        return findProfile()
            .map(UserProfile::isSetupComplete)
            .orElse(false);
    }
}
