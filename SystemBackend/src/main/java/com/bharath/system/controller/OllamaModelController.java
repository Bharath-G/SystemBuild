package com.bharath.system.controller;

import com.bharath.system.model.OllamaModel;
import com.bharath.system.service.OllamaModelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/models")
public class OllamaModelController {

    private final OllamaModelService ollamaModelService;

    public OllamaModelController(OllamaModelService ollamaModelService) {
        this.ollamaModelService = ollamaModelService;
    }

    @GetMapping
    public ResponseEntity<List<OllamaModel>> getModelsAlias() {
        return getAvailableModels();
    }

    @GetMapping("/available")
    public ResponseEntity<List<OllamaModel>> getAvailableModels() {
        return ResponseEntity.ok(ollamaModelService.getAvailableModels());
    }

    @GetMapping("/selected")
    public ResponseEntity<Map<String, String>> getSelectedModel() {
        String model = ollamaModelService.getSelectedModel();
        return ResponseEntity.ok(Map.of("model", model, "modelName", model));
    }

    @PostMapping("/select")
    public ResponseEntity<String> selectModel(@RequestBody Map<String, String> body) {
        String modelName = body.get("modelName");
        ollamaModelService.setSelectedModel(modelName);
        return ResponseEntity.ok("Model selected: " + modelName);
    }
}