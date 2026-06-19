package com.bharath.system.controller;

import com.bharath.system.model.MarketInsight;
import com.bharath.system.service.MarketIntelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/intelligence")
public class IntelligenceController {

    private final MarketIntelService marketIntelService;

    public IntelligenceController(MarketIntelService marketIntelService) {
        this.marketIntelService = marketIntelService;
    }

    @GetMapping("/insights")
    public List<MarketInsight> getInsights() {
        return marketIntelService.getInsights();
    }
}
