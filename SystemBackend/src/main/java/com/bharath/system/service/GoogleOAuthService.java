package com.bharath.system.service;

import com.bharath.system.model.GoogleConfig;
import com.bharath.system.repository.GoogleConfigRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
public class GoogleOAuthService {

    @Value("${google.client-id}")
    private String clientId;
    @Value("${google.client-secret}")
    private String clientSecret;
    @Value("${google.redirect-uri}")
    private String redirectUri;

    private final GoogleConfigRepository googleConfigRepository;

    private GoogleAuthorizationCodeFlow flow;

    public GoogleOAuthService(GoogleConfigRepository googleConfigRepository) {
        this.googleConfigRepository = googleConfigRepository;
        try {
            GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
            details.setClientId(clientId);
            details.setClientSecret(clientSecret);
            clientSecrets.setWeb(details);

            flow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientSecrets,
                Collections.singleton(SheetsScopes.SPREADSHEETS)
            ).setAccessType("offline").setApprovalPrompt("force").build();
        } catch (Exception e) {
            log.error("Error initializing GoogleAuthorizationCodeFlow", e);
        }
    }

    public String buildAuthUrl(String hint) {
        if (flow == null) {
            return "Error: Google OAuth not initialized.";
        }
        return flow.newAuthorizationUrl()
            .setRedirectUri(redirectUri)
            .set("login_hint", hint) // Pre-fills the email
            .build();
    }

    public void exchangeCodeForTokens(String code, String userEmail) throws IOException {
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
            .setRedirectUri(redirectUri)
            .execute();

        GoogleConfig config = googleConfigRepository.findById(1L).orElse(new GoogleConfig());
        config.setUserEmail(userEmail); // Save for display
        config.setAccessToken(tokenResponse.getAccessToken());
        config.setRefreshToken(tokenResponse.getRefreshToken());
        config.setTokenExpiry(LocalDateTime.ofEpochSecond(
            System.currentTimeMillis() / 1000 + tokenResponse.getExpiresInSeconds(), 0, ZoneOffset.UTC));
        googleConfigRepository.save(config);
    }

    public Optional<GoogleConfig> getGoogleConfig() {
        return googleConfigRepository.findById(1L);
    }

    public void disconnect() {
        googleConfigRepository.deleteById(1L);
    }

    public void saveSpreadsheetId(String spreadsheetId) {
        googleConfigRepository.findById(1L).ifPresent(config -> {
            config.setSpreadsheetId(spreadsheetId);
            googleConfigRepository.save(config);
        });
    }

    public Credential getCredential() {
        // This method would refresh tokens if needed and return a valid Credential
        // For brevity, actual token refresh logic is omitted but would go here.
        return null; // Placeholder
    }
}