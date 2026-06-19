package com.bharath.system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class GoogleConfig {
    @Id
    private Long id = 1L; // Single user application
    private String userEmail; // Display only
    private String accessToken;
    private String refreshToken;
    private String spreadsheetId;
    private LocalDateTime tokenExpiry;
    private LocalDateTime createdAt = LocalDateTime.now();
}