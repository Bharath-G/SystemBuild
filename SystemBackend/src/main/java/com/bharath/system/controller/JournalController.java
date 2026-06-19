package com.bharath.system.controller;

import com.bharath.system.model.JournalEntry;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/journal")
public class JournalController {

    @GetMapping
    public List<JournalEntry> getEntries() {
        return new ArrayList<>(); // Stub, usually reads from DB
    }

    @PostMapping
    public Map<String, Object> saveEntry(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        // Save to DB and Memory service logic goes here
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("status", "saved");
        return result;
    }
}
