package com.bharath.system.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GrowthTrackerService {

    public Map<String, Object> getRoadmap() {
        Map<String, Object> roadmap = new HashMap<>();
        List<Map<String, Object>> months = new ArrayList<>();
        
        Map<String, Object> m1 = new HashMap<>();
        m1.put("month", "June–July 2026");
        m1.put("focus", "AWS SAA + NeoBank visibility");
        m1.put("milestones", new String[]{"Pass AWS SAA exam", "Push NeoBank to GitHub", "Start ₹3,000 SIP"});
        m1.put("xpReward", 1500);
        m1.put("currentMonth", true);
        months.add(m1);
        
        Map<String, Object> m2 = new HashMap<>();
        m2.put("month", "August–September 2026");
        m2.put("focus", "Job switch execution");
        m2.put("milestones", new String[]{"Apply to GCCs", "Prep system design", "Start freelance"});
        m2.put("xpReward", 2000);
        m2.put("currentMonth", false);
        months.add(m2);
        
        roadmap.put("months", months);
        return roadmap;
    }
}
