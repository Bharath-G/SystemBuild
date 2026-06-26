package com.bharath.system.service;

import com.bharath.system.model.OllamaModel;
import com.bharath.system.model.UserProfile;
import com.bharath.system.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OllamaModelService {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model.default:mistral}")
    private String defaultModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserProfileRepository profileRepository;

    public OllamaModelService(UserProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<OllamaModel> getAvailableModels() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(ollamaUrl + "/api/tags", Map.class);
            List<Map> models = (List<Map>) response.getBody().get("models");
            return models.stream().map(m -> new OllamaModel(
                (String) m.get("name"),
                // Handle potentially different data types from Ollama API
                Long.valueOf(String.valueOf(((Map) m.get("details")).getOrDefault("parameter_size", "0").toString().replaceAll("[^0-9]", ""))),
                formatSize((Long) m.get("size"))
            )).collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    public String getSelectedModel() {
        return profileRepository.findById(1L)
            .map(UserProfile::getSelectedModel)
            .filter(m -> m != null && !m.isBlank())
            .orElse(defaultModel);
    }

    public void setSelectedModel(String modelName) {
        profileRepository.findById(1L).ifPresent(profile -> {
            profile.setSelectedModel(modelName);
            profileRepository.save(profile);
        });
    }

    private String formatSize(Long bytes) {
        if (bytes == null) return "unknown";
        return String.format("%.1f GB", bytes / 1_073_741_824.0);
    }
}