package com.bharath.system.service;

import com.bharath.system.model.UserProfile;
import com.bharath.system.model.WorkTask;
import com.bharath.system.repository.WorkTaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkService {

    private final WorkTaskRepository workTaskRepository;
    private final AIService aiService;
    private final ProfileService profileService;

    public WorkService(WorkTaskRepository workTaskRepository, AIService aiService, ProfileService profileService) {
        this.workTaskRepository = workTaskRepository;
        this.aiService = aiService;
        this.profileService = profileService;
    }

    public List<WorkTask> getTodayTasks() {
        return workTaskRepository.findByDate(LocalDate.now());
    }

    public WorkTask addTask(WorkTask task) {
        task.setDate(LocalDate.now());
        task.setStatus("TODO");
        return workTaskRepository.save(task);
    }

    public Optional<WorkTask> updateTask(Long id, WorkTask updatedTask) {
        return workTaskRepository.findById(id).map(task -> {
            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setPriority(updatedTask.getPriority());
            task.setStatus(updatedTask.getStatus());
            task.setBlocker(updatedTask.getBlocker());
            task.setEstimatedHours(updatedTask.getEstimatedHours());
            if ("DONE".equals(updatedTask.getStatus())) {
                task.setCompletedAt(LocalDateTime.now());
            }
            return workTaskRepository.save(task);
        });
    }
}