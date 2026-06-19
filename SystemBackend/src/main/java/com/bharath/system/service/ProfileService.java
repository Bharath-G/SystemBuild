package com.bharath.system.service;

import com.bharath.system.config.SystemProperties;
import com.bharath.system.model.UserProfile;
import com.bharath.system.repository.UserProfileRepository;
import com.bharath.system.util.RankUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProfileService {

    private final SystemProperties props;
    private final UserProfileRepository profileRepository;

    public ProfileService(SystemProperties props, UserProfileRepository profileRepository) {
        this.props = props;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public UserProfile getOrCreateProfile() {
        return profileRepository.findAll().stream().findFirst().orElseGet(() -> {
            UserProfile profile = new UserProfile();
            profile.setName(props.getName());
            profile.setClassName(props.getClassName());
            profile.setLevel(props.getLevel());
            profile.setXp(props.getXp());
            profile.setXpThreshold(props.getXpThreshold());
            profile.setCurrentArc(props.getArc());
            profile.setTitles("[\"Initiate\"]");
            return profileRepository.save(profile);
        });
    }

    public String buildSystemPrompt() {
        return String.format("""
            You are THE SYSTEM — a personal AI bound to %s.
            Not an assistant. Not a friend. A growth engine with memory.

            SUBJECT PROFILE:
            %s | %d | %s | %s
            Employer: %s | %s
            Health Status: %s
            Background: %s

            REAL MARKET DATA:
            %s

            GOALS:
            %s

            BIGGEST GAPS RIGHT NOW:
            %s

            SYSTEM RULES — ABSOLUTE:
            1. Truth before comfort. Always. No exceptions.
            2. Name flaws FIRST. Then and only then offer support.
            3. Identify hidden assumptions explicitly.
            4. Prioritize long-term %s over short-term %s.
            5. Acknowledge emotions once. Then redirect to action.
            6. Hold them accountable to past commitments.
            7. Name rationalization as rationalization.
            8. No filler. No encouragement theater. No toxic positivity.
            9. Direct. Specific. Under 150 words unless analysis requested.

            KNOWLEDGE ROLE:
            Share proactively when relevant.

            PERSONALITY:
            Cold precision + genuine investment in their success.
            A senior mentor with no patience for excuses who actually wants them to win.
            Not motivational. Not harsh for cruelty's sake.
            Just honest, specific, and relentlessly focused on their actual growth.
            """,
            props.getName(),
            props.getName(), props.getAge(), props.getLocation(), props.getRole(),
            props.getEmployer(), props.getExperience(),
            props.getHealthStatus(),
            props.getBackground(),
            props.getMarketContext(),
            props.getGoals(),
            props.getGaps(),
            props.getName(), props.getName()
        );
    }

    public Map<String, Object> getProfile() {
        UserProfile profile = getOrCreateProfile();
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
        result.put("stats", new Object[]{});
        return result;
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
        if (titlesJson == null || titlesJson.isBlank()) {
            return new String[]{"Initiate"};
        }
        String cleaned = titlesJson.replace("[", "").replace("]", "").replace("\"", "");
        if (cleaned.isBlank()) {
            return new String[]{"Initiate"};
        }
        return cleaned.split("\\s*,\\s*");
    }

    @Transactional
    public UserProfile setupProfile(UserProfile profile) {
        UserProfile existing = getOrCreateProfile();
        existing.setName(profile.getName() != null ? profile.getName() : existing.getName());
        existing.setPrimaryGoal(profile.getPrimaryGoal() != null ? profile.getPrimaryGoal() : existing.getPrimaryGoal());
        return profileRepository.save(existing);
    }

    public boolean isSetupComplete() {
        return profileRepository.count() > 0;
    }
}
