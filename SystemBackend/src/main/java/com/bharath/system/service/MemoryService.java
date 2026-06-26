package com.bharath.system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class MemoryService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${chroma.url:http://localhost:8001}")
    private String chromaUrl;

    public void storeMemory(String text, String type) {
        try {
            Map<String, Object> body = Map.of(
                "text", text,
                "metadata", Map.of("type", type, "timestamp", System.currentTimeMillis())
            );
            restTemplate.postForEntity(chromaUrl + "/memory/store", body, String.class);
        } catch (Exception e) {
            log.error("Failed to store semantic memory in ChromaDB: {}", e.getMessage());
        }
    }
    
    public String retrieveRelevant(String text) {
        try {
            Map<String, Object> body = Map.of(
                "query", text,
                "n_results", 3
            );
            Map response = restTemplate.postForObject(chromaUrl + "/memory/retrieve", body, Map.class);
            if (response != null && response.containsKey("memories")) {
                Object memoriesObj = response.get("memories");
                if (memoriesObj instanceof String) {
                    return (String) memoriesObj;
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve semantic memory from ChromaDB: {}", e.getMessage());
        }
        return "";
    }
}
