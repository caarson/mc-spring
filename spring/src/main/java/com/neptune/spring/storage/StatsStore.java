package com.neptune.spring.storage;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StatsStore {
    // Increment bounce for a player (store last known name optionally)
    void incrementBounce(UUID playerId, String playerName);

    // Per-player getters for current period counts (uses ZoneId and week start when needed)
    int getAlltimeBounces(UUID playerId);
    int getMonthlyBounces(UUID playerId, ZoneId zone);
    int getWeeklyBounces(UUID playerId, ZoneId zone, DayOfWeek weekStart);
    int getDailyBounces(UUID playerId, ZoneId zone);

    // Top-N queries return list of (playerId -> count) entries ordered desc
    List<Map.Entry<UUID, Integer>> getTopAlltime(int topN);
    List<Map.Entry<UUID, Integer>> getTopMonthly(int topN, ZoneId zone);
    List<Map.Entry<UUID, Integer>> getTopWeekly(int topN, ZoneId zone, DayOfWeek weekStart);
    List<Map.Entry<UUID, Integer>> getTopDaily(int topN, ZoneId zone);
}
