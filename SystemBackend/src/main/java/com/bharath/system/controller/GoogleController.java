package com.bharath.system.controller;

import com.bharath.system.model.GoogleConfig;
import com.bharath.system.service.GoogleOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/google")
public class GoogleController {

    private final GoogleOAuthService googleOAuthService;
    private final com.bharath.system.service.GoogleSyncService googleSyncService;

    public GoogleController(GoogleOAuthService googleOAuthService, com.bharath.system.service.GoogleSyncService googleSyncService) {
        this.googleOAuthService = googleOAuthService;
        this.googleSyncService = googleSyncService;
    }

    @GetMapping("/auth-url")
    public ResponseEntity<Map<String, String>> getAuthUrl(@RequestParam(required = false) String hint) {
        String url = googleOAuthService.buildAuthUrl(hint);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam String code, @RequestParam String state) {
        // 'state' parameter can be used for CSRF protection, here we assume it's handled or not critical for this example
        String userEmail = state; // Assuming state carries the email for simplicity, in real app use a proper state mechanism
        try {
            googleOAuthService.exchangeCodeForTokens(code, userEmail);
            return ResponseEntity.ok("Authentication successful! You can close this window.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Authentication failed: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<GoogleConfig> getGoogleStatus() {
        Optional<GoogleConfig> config = googleOAuthService.getGoogleConfig();
        return config.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/spreadsheet-id")
    public ResponseEntity<String> saveSpreadsheetId(@RequestBody Map<String, String> body) {
        String spreadsheetId = body.get("spreadsheetId");
        googleOAuthService.saveSpreadsheetId(spreadsheetId);
        return ResponseEntity.ok("Spreadsheet ID saved.");
    }

    @DeleteMapping("/disconnect")
    public ResponseEntity<String> disconnectGoogle() {
        googleOAuthService.disconnect();
        return ResponseEntity.ok("Disconnected from Google.");
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> syncData(@RequestParam(defaultValue = "WEEKLY") String type) {
        try {
            String result = googleSyncService.syncData(type);
            return ResponseEntity.ok(Map.of("status", "success", "message", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}