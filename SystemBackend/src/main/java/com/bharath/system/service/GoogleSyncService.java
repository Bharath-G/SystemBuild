package com.bharath.system.service;

import com.bharath.system.model.GoogleConfig;
import com.bharath.system.model.Quest;
import com.bharath.system.model.HealthLog;
import com.bharath.system.repository.GoogleConfigRepository;
import com.bharath.system.repository.QuestRepository;
import com.bharath.system.repository.HealthLogRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class GoogleSyncService {

    private final GoogleOAuthService googleOAuthService;
    private final GoogleConfigRepository googleConfigRepository;
    private final QuestRepository questRepository;
    private final HealthLogRepository healthLogRepository;

    public GoogleSyncService(GoogleOAuthService googleOAuthService, 
                             GoogleConfigRepository googleConfigRepository,
                             QuestRepository questRepository,
                             HealthLogRepository healthLogRepository) {
        this.googleOAuthService = googleOAuthService;
        this.googleConfigRepository = googleConfigRepository;
        this.questRepository = questRepository;
        this.healthLogRepository = healthLogRepository;
    }

    public String syncData(String type) throws GeneralSecurityException, IOException {
        GoogleConfig config = googleConfigRepository.findById(1L).orElse(null);
        if (config == null || config.getSpreadsheetId() == null || config.getSpreadsheetId().isEmpty()) {
            throw new RuntimeException("Spreadsheet ID not configured or Google account not connected.");
        }

        Credential credential = googleOAuthService.getCredential();
        if (credential == null) {
            throw new RuntimeException("Valid Google credentials not found.");
        }

        Sheets sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("The System")
                .build();

        String spreadsheetId = config.getSpreadsheetId();
        
        // 1. Sync Quests
        syncQuests(sheetsService, spreadsheetId);
        
        // 2. Sync Health Logs
        syncHealthLogs(sheetsService, spreadsheetId);
        
        return "Successfully synced data to Google Sheets.";
    }

    private void syncQuests(Sheets sheetsService, String spreadsheetId) throws IOException {
        List<Quest> quests = questRepository.findAll();
        List<List<Object>> values = new ArrayList<>();
        
        // Header
        values.add(Arrays.asList("ID", "Title", "Category", "Difficulty", "XP Reward", "Completed", "Deadline"));
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        for (Quest q : quests) {
            values.add(Arrays.asList(
                    q.getId(),
                    q.getTitle(),
                    q.getCategory(),
                    q.getDifficulty(),
                    q.getXpReward(),
                    q.isCompleted() ? "Yes" : "No",
                    q.getDeadline() != null ? q.getDeadline().format(formatter) : ""
            ));
        }

        ValueRange body = new ValueRange().setValues(values);
        try {
            sheetsService.spreadsheets().values()
                    .update(spreadsheetId, "Quests!A1", body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (Exception e) {
            log.error("Failed to sync Quests: {}", e.getMessage());
        }
    }

    private void syncHealthLogs(Sheets sheetsService, String spreadsheetId) throws IOException {
        List<HealthLog> logs = healthLogRepository.findAll();
        List<List<Object>> values = new ArrayList<>();
        
        // Header
        values.add(Arrays.asList("ID", "Date", "Exercise Done", "Type", "Minutes", "Water Glasses", "Sleep Hours", "Pain Level"));
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        for (HealthLog log : logs) {
            values.add(Arrays.asList(
                    log.getId(),
                    log.getDate() != null ? log.getDate().format(formatter) : "",
                    log.isExerciseDone() ? "Yes" : "No",
                    log.getExerciseType(),
                    log.getExerciseMinutes(),
                    log.getWaterGlasses(),
                    log.getSleepHours(),
                    log.getPainLevel()
            ));
        }

        ValueRange body = new ValueRange().setValues(values);
        try {
            sheetsService.spreadsheets().values()
                    .update(spreadsheetId, "HealthLogs!A1", body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (Exception e) {
            log.error("Failed to sync HealthLogs, tab may not exist: {}", e.getMessage());
        }
    }
}
