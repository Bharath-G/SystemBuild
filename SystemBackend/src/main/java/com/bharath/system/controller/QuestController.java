package com.bharath.system.controller;

import com.bharath.system.model.Quest;
import com.bharath.system.service.QuestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quests")
public class QuestController {

    private final QuestService questService;

    public QuestController(QuestService questService) {
        this.questService = questService;
    }

    @GetMapping("/active")
    public List<Quest> getActiveQuests() {
        return questService.getActiveQuests();
    }

    @GetMapping("/completed")
    public List<Quest> getCompletedQuests() {
        return questService.getCompletedQuests();
    }

    @PostMapping("/{id}/complete")
    public Map<String, Object> completeQuest(@PathVariable Long id) {
        return questService.completeQuest(id);
    }

    @PostMapping("/generate")
    public List<Quest> generateQuests() {
        return questService.generateQuests();
    }
}
