package com.bharath.system.util;

public final class RankUtil {

    private RankUtil() {}

    public static String getRank(int level) {
        if (level >= 100) return "SS-Rank";
        if (level >= 75) return "S-Rank";
        if (level >= 50) return "A-Rank";
        if (level >= 35) return "B-Rank";
        if (level >= 20) return "C-Rank";
        if (level >= 10) return "D-Rank";
        return "E-Rank";
    }

    public static String getRankTagline(int level) {
        if (level >= 100) return "Transcendent";
        if (level >= 75) return "Elite";
        if (level >= 50) return "Architect";
        if (level >= 35) return "Builder";
        if (level >= 20) return "Performer";
        if (level >= 10) return "Disciplined";
        return "Initiating...";
    }
}
