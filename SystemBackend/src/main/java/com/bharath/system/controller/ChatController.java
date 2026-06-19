package com.bharath.system.controller;

import com.bharath.system.service.AIService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final AIService aiService;

    public ChatController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = aiService.chat(message);
        
        Map<String, String> result = new HashMap<>();
        result.put("response", response);
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
