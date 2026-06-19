package com.bharath.system.controller;

import com.bharath.system.model.WorkTask;
import com.bharath.system.service.WorkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work")
public class WorkController {

    private final WorkService workService;

    public WorkController(WorkService workService) {
        this.workService = workService;
    }

    @GetMapping("/today")
    public ResponseEntity<List<WorkTask>> getTodayTasks() {
        return ResponseEntity.ok(workService.getTodayTasks());
    }

    @PostMapping("/task")
    public ResponseEntity<WorkTask> addTask(@RequestBody WorkTask task) {
        return ResponseEntity.ok(workService.addTask(task));
    }

    @PutMapping("/task/{id}")
    public ResponseEntity<WorkTask> updateTask(@PathVariable Long id, @RequestBody WorkTask task) {
        return workService.updateTask(id, task)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // Placeholder for AI suggestions/plan
    @GetMapping("/suggest")
    public ResponseEntity<String> suggestWork() {
        return ResponseEntity.ok("[SYSTEM] Work suggestions not yet implemented.");
    }
}