package com.bharath.system.service;

import com.bharath.system.model.FinancialInsight;
import com.bharath.system.model.FinancialLog;
import com.bharath.system.model.UserProfile;
import com.bharath.system.repository.FinancialInsightRepository;
import com.bharath.system.repository.FinancialLogRepository;
import com.bharath.system.repository.UserProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FinanceService {

    private final FinancialLogRepository financialLogRepository;
    private final FinancialInsightRepository financialInsightRepository;
    private final UserProfileRepository userProfileRepository;
    private final AIService aiService;

    public FinanceService(FinancialLogRepository financialLogRepository,
                          FinancialInsightRepository financialInsightRepository,
                          UserProfileRepository userProfileRepository,
                          AIService aiService) {
        this.financialLogRepository = financialLogRepository;
        this.financialInsightRepository = financialInsightRepository;
        this.userProfileRepository = userProfileRepository;
        this.aiService = aiService;
    }

    public FinancialLog logFinance(FinancialLog logData) {
        if (logData.getDate() == null) {
            logData.setDate(LocalDate.now());
        }
        return financialLogRepository.save(logData);
    }

    public List<FinancialLog> getMonthlyLogs(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return financialLogRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
    }

    public Map<String, Double> getMonthlySummary(int year, int month) {
        List<FinancialLog> logs = getMonthlyLogs(year, month);
        double totalIncome = logs.stream().filter(l -> "INCOME".equalsIgnoreCase(l.getType())).mapToDouble(FinancialLog::getAmount).sum();
        double totalExpense = logs.stream().filter(l -> "EXPENSE".equalsIgnoreCase(l.getType())).mapToDouble(FinancialLog::getAmount).sum();
        return Map.of("income", totalIncome, "expense", totalExpense, "net", totalIncome - totalExpense);
    }

    public List<FinancialInsight> getInsights() {
        return financialInsightRepository.findAllByOrderByGeneratedDateDesc();
    }

    public List<FinancialInsight> generateInsights() {
        UserProfile profile = userProfileRepository.findById(1L).orElse(new UserProfile());
        
        // 1. Get recent logs to give AI context
        LocalDate now = LocalDate.now();
        List<FinancialLog> recentLogs = getMonthlyLogs(now.getYear(), now.getMonthValue());
        
        String logsSummary = recentLogs.stream()
                .map(l -> l.getDate() + ": " + l.getType() + " - " + l.getAmount() + " (" + l.getCategory() + ")")
                .collect(Collectors.joining("\n"));

        String prompt = String.format("Analyze this financial data and generate 3 actionable insights.\n" +
                "User context: Goal: %s, Current Salary: %s LPA, Target Salary: %s LPA, Location: %s\n" +
                "Recent Logs:\n%s\n\n" +
                "Return exactly 3 insights separated by '---'. " +
                "For each insight, use this format:\n" +
                "Title: [Short title]\n" +
                "Summary: [1-2 sentences explaining the insight]\n" +
                "Relevance: [Why this matters for their goal]\n" +
                "Action: [One concrete step to take]\n" +
                "Category: [e.g. SAVINGS, SPENDING, INCOME]",
                profile.getPrimaryGoal(), profile.getCurrentSalaryLPA(), profile.getTargetSalaryLPA(), profile.getLocation(),
                logsSummary.isEmpty() ? "No recent logs" : logsSummary);

        try {
            String response = aiService.chatWithMode(prompt, "SYSTEM");
            
            // Delete old insights to keep it clean (optional, or just append)
            financialInsightRepository.deleteAll();
            
            String[] rawInsights = response.split("---");
            for (String raw : rawInsights) {
                if (raw.trim().isEmpty()) continue;
                
                FinancialInsight insight = new FinancialInsight();
                insight.setTitle(extractField(raw, "Title:"));
                insight.setSummary(extractField(raw, "Summary:"));
                insight.setRelevance(extractField(raw, "Relevance:"));
                insight.setAction(extractField(raw, "Action:"));
                insight.setCategory(extractField(raw, "Category:"));
                
                if (insight.getTitle() != null && !insight.getTitle().isEmpty()) {
                    financialInsightRepository.save(insight);
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate financial insights", e);
        }
        
        return getInsights();
    }

    private String extractField(String text, String fieldName) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith(fieldName)) {
                return line.substring(fieldName.length()).trim();
            }
        }
        return "";
    }
}
