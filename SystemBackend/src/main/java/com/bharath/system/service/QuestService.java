package com.bharath.system.service;

import com.bharath.system.config.SystemProperties;
import com.bharath.system.model.Quest;
import com.bharath.system.repository.QuestRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestService {

    private static final Pattern JSON_ARRAY = Pattern.compile("\\[.*\\]", Pattern.DOTALL);

    private final QuestRepository questRepository;
    private final ProfileService profileService;
    private final AIService aiService;
    private final AchievementService achievementService;
    private final SystemProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Quest> getActiveQuests() {
        return questRepository.findByCompletedFalseOrderByDeadlineAsc();
    }

    public List<Quest> getCompletedQuests() {
        return questRepository.findByCompletedTrueOrderByCompletedAtDesc();
    }

    @Transactional
    public Map<String, Object> completeQuest(Long id) {
        Quest quest = questRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found: " + id));

        if (quest.isCompleted()) {
            throw new IllegalStateException("Quest already completed");
        }

        quest.setCompleted(true);
        quest.setCompletedAt(LocalDateTime.now());
        questRepository.save(quest);

        Map<String, Object> xpResult = profileService.addXp(quest.getXpReward());
        List<String> unlocked = achievementService.checkAndUnlock(quest);

        Map<String, Object> result = new HashMap<>(xpResult);
        result.put("status", "success");
        result.put("questId", id);
        result.put("unlockedAchievements", unlocked);
        return result;
    }

    @Transactional
    public List<Quest> generateQuests() {
        String prompt = String.format("""
            Generate 3 quests for %s as JSON array only. No preamble. No markdown.
            Each quest: title, description, xpReward(50-300), difficulty, category, type, consequence, deadlineHours(24/72/168)
            Base on his goals: AWS SAA cert, NeoBank completion, start SIP, LinkedIn visibility.
            Make them specific, measurable, slightly uncomfortable.
            difficulty must be one of: EASY, MEDIUM, HARD, LEGENDARY
            category must be one of: CAREER, FINANCE, HEALTH, SKILL, KNOWLEDGE
            type must be one of: DAILY, WEEKLY, MILESTONE
            """, props.getName());
        String raw = aiService.chatWithMode(prompt, "SYSTEM");
        List<Map<String, Object>> parsed = parseQuestJson(raw);

        if (parsed.isEmpty()) {
            parsed = fallbackQuests();
        }

        List<Quest> saved = new ArrayList<>();
        for (Map<String, Object> item : parsed) {
            Quest quest = new Quest();
            quest.setTitle(String.valueOf(item.get("title")));
            quest.setDescription(String.valueOf(item.get("description")));
            quest.setXpReward(clampXp(item.get("xpReward")));
            quest.setDifficulty(normalizeEnum(item.get("difficulty"), "MEDIUM"));
            quest.setCategory(normalizeEnum(item.get("category"), "CAREER"));
            quest.setType(normalizeEnum(item.get("type"), "DAILY"));
            quest.setConsequence(String.valueOf(item.getOrDefault("consequence", "Momentum lost.")));
            quest.setCompleted(false);
            quest.setDeadline(LocalDateTime.now().plusHours(parseDeadlineHours(item.get("deadlineHours"))));
            saved.add(questRepository.save(quest));
        }
        return saved;
    }

    private List<Map<String, Object>> parseQuestJson(String raw) {
        try {
            String json = extractJsonArray(raw);
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse quest JSON from Ollama: {}", e.getMessage());
            return List.of();
        }
    }

    private String extractJsonArray(String raw) {
        Matcher matcher = JSON_ARRAY.matcher(raw);
        if (matcher.find()) {
            return matcher.group();
        }
        return raw.trim();
    }

    private int clampXp(Object value) {
        int xp = 100;
        if (value instanceof Number n) {
            xp = n.intValue();
        } else if (value != null) {
            try {
                xp = Integer.parseInt(value.toString());
            } catch (NumberFormatException ignored) {}
        }
        return Math.max(50, Math.min(300, xp));
    }

    private long parseDeadlineHours(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException ignored) {}
        }
        return 24;
    }

    private String normalizeEnum(Object value, String fallback) {
        if (value == null) return fallback;
        return value.toString().trim().toUpperCase();
    }

    private List<Map<String, Object>> fallbackQuests() {
        List<Map<String, Object>> quests = new ArrayList<>();
        quests.add(Map.of(
                "title", "AWS SAA — 45 Min Study Block",
                "description", "Complete one practice exam section and log 3 weak areas.",
                "xpReward", 120,
                "difficulty", "MEDIUM",
                "category", "CAREER",
                "type", "DAILY",
                "consequence", "Cert timeline slips another week.",
                "deadlineHours", 24
        ));
        quests.add(Map.of(
                "title", "NeoBank README Push",
                "description", "Write architecture section and push to GitHub with deployment notes.",
                "xpReward", 180,
                "difficulty", "HARD",
                "category", "SKILL",
                "type", "WEEKLY",
                "consequence", "Portfolio remains invisible to recruiters.",
                "deadlineHours", 72
        ));
        quests.add(Map.of(
                "title", "Start ₹3,000 SIP",
                "description", "Open or verify mutual fund SIP and schedule first debit.",
                "xpReward", 200,
                "difficulty", "MEDIUM",
                "category", "FINANCE",
                "type", "MILESTONE",
                "consequence", "Wealth compounding delayed.",
                "deadlineHours", 168
        ));
        return quests;
    }
}
