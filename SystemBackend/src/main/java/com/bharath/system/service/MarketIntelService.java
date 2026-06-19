package com.bharath.system.service;

import com.bharath.system.model.MarketInsight;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarketIntelService {

    public List<MarketInsight> getInsights() {
        List<MarketInsight> insights = new ArrayList<>();
        
        MarketInsight i1 = new MarketInsight();
        i1.setId("1");
        i1.setCategory("CAREER");
        i1.setDate(LocalDate.now().toString());
        i1.setTitle("Java + AI Integration Premium Is Real — 20-30% Salary Uplift");
        i1.setSummary("Engineers with Spring AI, LangChain4j, or vector DB integration experience command 20-30% above generalist Java developers at GCCs in 2026.");
        i1.setRelevance("Your NeoBank project already uses Spring AI with Ollama. This is a resume weapon most candidates don't have yet.");
        i1.setAction("Add 'Spring AI (Ollama integration, vector DB)' explicitly to your resume and LinkedIn. Mention NeoBank in interviews.");
        insights.add(i1);

        MarketInsight i2 = new MarketInsight();
        i2.setId("2");
        i2.setCategory("CAREER");
        i2.setDate(LocalDate.now().toString());
        i2.setTitle("Standard Chartered, Deloitte, Ford Tech GCCs Actively Hiring in Chennai");
        i2.setSummary("Chennai hosts 350+ GCCs in 2026. Standard Chartered GBS, Deloitte India, and Ford India Tech Center are among active employers with Java backend demand.");
        i2.setRelevance("You are already in Chennai. No relocation cost. These are tier-1 names that pay 50-80% above your current Cognizant band.");
        i2.setAction("Apply directly via LinkedIn to Standard Chartered GBS Chennai and JP Morgan Chennai. Employee referral is the strongest channel — find 1 connection.");
        insights.add(i2);
        
        return insights;
    }
}
