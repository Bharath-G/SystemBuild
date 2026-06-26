package com.bharath.system.controller;

import com.bharath.system.service.AIService;
import com.bharath.system.service.OllamaModelService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final AIService aiService;
    private final OllamaModelService ollamaModelService;

    public ChatController(AIService aiService, OllamaModelService ollamaModelService) {
        this.aiService = aiService;
        this.ollamaModelService = ollamaModelService;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String mode = request.getOrDefault("mode", "SYSTEM");
        String response = aiService.chatWithMode(message, mode);

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        result.put("mode", mode);
        result.put("model", ollamaModelService.getSelectedModel());
        result.put("provider", aiService.getCurrentProvider().name());
        return result;
    }

    @PostMapping("/analysis")
    public Map<String, String> analysis(@RequestBody Map<String, String> request) {
        String scenario = request.get("scenario");
        String analysis = aiService.analyse(scenario, null);

        Map<String, String> result = new HashMap<>();
        result.put("analysis", analysis);
        return result;
    }
}
