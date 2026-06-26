package com.bharath.system.service;

import com.bharath.system.model.UserProfile;
import com.bharath.system.model.CompanionMemory;
import com.bharath.system.repository.CompanionMemoryRepository;
import com.bharath.system.model.OllamaModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIService {

    private final ProfileService profileService;
    private final RestTemplate restTemplate;
    private final com.bharath.system.config.SystemProperties props;
    private final OllamaModelService ollamaModelService;
    private final CompanionMemoryRepository memoryRepository;

    @Value("${anthropic.api.key:}")
    private String claudeKey;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaUrl;

    public AIService(ProfileService profileService, com.bharath.system.config.SystemProperties props, OllamaModelService ollamaModelService, CompanionMemoryRepository memoryRepository, @Value("${ollama.base-url:http://localhost:11434}") String ollamaBaseUrl) {
        this.profileService = profileService;
        this.props = props;
        this.ollamaModelService = ollamaModelService;
        this.memoryRepository = memoryRepository;
        // Ensure ollamaUrl ends with /api/generate for the actual call
        this.ollamaUrl = ollamaBaseUrl.endsWith("/") ? ollamaBaseUrl + "api/generate" : ollamaBaseUrl + "/api/generate";
        this.restTemplate = new RestTemplate();
    }

    public String chat(String userMessage) {
        return chatWithMode(userMessage, "SYSTEM");
    }

    public String chatWithMode(String userMessage, String mode) {
        UserProfile p = profileService.findProfile().orElse(new UserProfile());
        String systemPrompt = buildModePrompt(mode, p);
        String model = ollamaModelService.getSelectedModel();

        String response = "[SYSTEM OFFLINE] No AI available.\nRun: ollama serve";

        if (isOllamaAvailable()) {
            try {
                response = callOllama(systemPrompt, userMessage, model);
            } catch (Exception e) {
                log.warn("Ollama failed: {}", e.getMessage());
                if (isClaudeConfigured()) {
                    try {
                        response = callClaude(systemPrompt, userMessage);
                    } catch (Exception claudeEx) {
                        log.warn("Claude also failed: {}", claudeEx.getMessage());
                    }
                }
            }
        } else if (isClaudeConfigured()) {
            try {
                response = callClaude(systemPrompt, userMessage);
            } catch (Exception e) {
                log.warn("Claude failed: {}", e.getMessage());
            }
        }

        saveCompanionMemory(mode, userMessage, response);
        return response;
    }

    private void saveCompanionMemory(String mode, String userMsg, String response) {
        String extractPrompt = String.format(
            "Analyze this interaction. User: \"%s\" | System: \"%s\"\n" +
            "Return JSON only: { \"emotionalTone\": \"ONE_WORD\", \"keyInsight\": \"one sentence\" }",
            userMsg, response
        );
        try {
            String json = callOllama(profileService.buildSystemPrompt(), extractPrompt, ollamaModelService.getSelectedModel());
            // Simple manual parse for now to avoid dependency bloat
            CompanionMemory memory = new CompanionMemory();
            memory.setMode(mode);
            memory.setUserMessage(userMsg);
            memory.setSystemResponse(response);
            memory.setEmotionalTone(json.contains("emotionalTone") ? extractJsonValue(json, "emotionalTone") : "NEUTRAL");
            memoryRepository.save(memory);
        } catch (Exception e) { log.error("Memory extraction failed"); }
    }

    @Scheduled(cron = "0 0 20 * * SUN")
    public void generateWeeklyReport() {
        List<CompanionMemory> weekData = memoryRepository.findAll(); // Should filter by date
        String insightSummary = weekData.stream().map(m -> m.getKeyInsight()).reduce("", (a, b) -> a + ". " + b);
        
        UserProfile p = profileService.getOrCreateProfile();
        String reportPrompt = String.format(
            "Generate a weekly companion report for %s based on these insights: %s\n" +
            "1. PATTERN — what emotional pattern emerged this week\n" +
            "2. GROWTH — one thing that showed genuine growth\n" +
            "3. CONCERN — one thing that needs attention\n" +
            "4. NEXT WEEK — one focus for the coming week\n" +
            "Be specific. Use their actual words where possible. This is private. Be fully honest. Under 200 words.",
            p.getName(), insightSummary
        );

        String report = chatWithMode(reportPrompt, "SYSTEM"); // Use SYSTEM mode for report generation
        // Save to CompanionReport entity logic here
    }

    private String buildModePrompt(String mode, UserProfile p) {
        String base = profileService.buildSystemPrompt();
        return switch (mode) {
            case "MENTOR" -> base + "\nMODE: MENTOR. Speak like a wise senior. Warm but honest. Under 120 words.";
            case "COACH"  -> base + "\nMODE: COACH. Pure performance. End with ONE instruction. Under 80 words.";
            case "REFLECT"-> base + "\nMODE: REFLECT. One question maximum per response. Under 40 words.";
            case "SUPPORT"-> base + "\nMODE: SUPPORT. Hear them first. Acknowledge fully. Then one direction. Under 150 words.";
            default       -> base;
        };
    }

    private String extractJsonValue(String json, String key) {
        // Basic JSON parsing for a single key-value pair
        return json.split("\"" + key + "\":\\s*\"")[1].split("\"")[0];
    }
    private String callClaude(String systemPrompt, String userMessage) {
        if (claudeKey != null && !claudeKey.isEmpty()) {
            return "[SYSTEM] Claude routing logic placeholder. Message: " + userMessage;
        }
        return "[SYSTEM OFFLINE]";
    }

    private boolean isOllamaAvailable() {
        try {
            restTemplate.getForEntity(ollamaUrl.replace("/api/generate", "/api/tags"), String.class);
            return true;
        } catch (Exception e) { return false; }
    } 

    private boolean isClaudeConfigured() {
        return claudeKey != null && !claudeKey.isEmpty();
    }

    public Map<String, Object> getProviderInfo() {
        String model = ollamaModelService.getSelectedModel();
        long latencyMs = -1;
        String provider = "NONE";

        if (isOllamaAvailable()) {
            provider = "OLLAMA";
            try {
                long start = System.currentTimeMillis();
                restTemplate.getForEntity(ollamaUrl.replace("/api/generate", "/api/tags"), String.class);
                latencyMs = System.currentTimeMillis() - start;
            } catch (Exception e) {
                latencyMs = -1;
            }
        } else if (isClaudeConfigured()) {
            provider = "CLAUDE";
        }

        return Map.of(
            "provider", provider,
            "model", model,
            "latencyMs", latencyMs
        );
    }

    public AIProvider getCurrentProvider() {
        if (isOllamaAvailable()) return AIProvider.OLLAMA;
        if (isClaudeConfigured()) return AIProvider.CLAUDE;
        return AIProvider.NONE;
    }

    public enum AIProvider { OLLAMA, CLAUDE, NONE }

    public String generateQuests(UserProfile profile) {
        String name = profile.getName() != null ? profile.getName() : "User";
        String goal = profile.getPrimaryGoal() != null ? profile.getPrimaryGoal() : "career growth";
        String prompt = String.format(
            "Generate 3 quests for %s as JSON array only. No preamble. No markdown.\n" +
            "Each quest: title, description, xpReward(50-300), difficulty, category, type, consequence, deadlineHours(24/72/168)\n" +
            "Base on their goals: %s. Make them specific, measurable, slightly uncomfortable. JSON array only.",
            name, goal
        );
        String model = ollamaModelService.getSelectedModel();
        return sanitizeJsonResponse(callOllama(profileService.buildSystemPrompt(), prompt, model));
    }

    public String generateDayPlan(UserProfile profile, String work, String goals, String challenges, int energy, int mood, int sleepHours) {
        String userPrompt = String.format(
            "Energy: %d/5, Mood: %d/5, Sleep: %dh. Work: %s. Goals: %s. Challenges: %s.",
            energy, mood, sleepHours, work, goals, challenges
        );
        String model = ollamaModelService.getSelectedModel();
        return callOllama(profileService.buildSystemPrompt() + "\nGenerate a prioritized day plan.", userPrompt, model); // Corrected call
    }

    private String sanitizeJsonResponse(String response) {
        if (response == null) return "[]";
        // Remove markdown code blocks if present
        String cleaned = response.replaceAll("```json", "").replaceAll("```", "").trim();
        // Find the first '[' and last ']' to isolate the JSON array
        int start = cleaned.indexOf('[');
        int end = cleaned.lastIndexOf(']');
        if (start != -1 && end != -1 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    public String analyse(String scenario, UserProfile profile) {
        String prompt = String.format(
            "DECISION ANALYSIS MODE.\nScenario from %s: %s\n\n" +
            "Analyse this with the following structure:\n" +
            "1. FLAWS/RISKS — name these first, be specific\n" +
            "2. HIDDEN ASSUMPTION — what is %s assuming that may not be true\n" +
            "3. MARKET REALITY — relevant data from current tech market\n" +
            "4. VERDICT: PROCEED / CAUTION / REJECT, with clear reason\n" +
            "5. RECOMMENDED ACTION — what to do next, specific and time-bound\n\n" +
            "Be direct. No comfort. No filler.",
            profile.getName(), scenario, profile.getName());
        return callOllama(profileService.buildSystemPrompt(), prompt, ollamaModelService.getSelectedModel()); // Corrected call
    }

    private String callOllama(String systemPrompt, String userPrompt, String model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", systemPrompt + "\n\nUser: " + userPrompt + "\n\nSystem:");
            requestBody.put("stream", false);
            requestBody.put("options", new HashMap<String, Object>() {{ put("temperature", 0.7); }});

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl, request, Map.class);
            
            if (response.getBody() != null && response.getBody().containsKey("response") && response.getBody().get("response") != null) {
                return ((String) response.getBody().get("response")).trim();
            }
            return "[SYSTEM ERROR] Invalid response format from Ollama.";
        } catch (Exception e) {
            return "[SYSTEM OFFLINE]\nOllama is not reachable. Ensure 'ollama serve' is running on this machine.\nActive model: " + ollamaModelService.getSelectedModel();
        }
    }
}
