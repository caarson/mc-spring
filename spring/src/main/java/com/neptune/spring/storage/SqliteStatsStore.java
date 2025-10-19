package com.neptune.spring.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.plugin.Plugin;

public class SqliteStatsStore implements StatsStore {
    private final Plugin plugin;
    private Connection connection;

    public SqliteStatsStore(Plugin plugin) {
        this.plugin = plugin;
        initializeDB();
    }

    private void initializeDB() {
        // TODO: implement DB initialization
    }

    @Override
    public void incrementBounce(UUID playerId, String playerName) {
        // TODO: Implement DB increment
    }

    @Override
    public int getAlltimeBounces(UUID playerId) {
        return 0; // TODO
    }

    @Override
    public int getMonthlyBounces(UUID playerId, ZoneId zone) {
        return 0; // TODO
    }

    @Override
    public int getWeeklyBounces(UUID playerId, ZoneId zone, DayOfWeek weekStart) {
        return 0; // TODO
    }

    @Override
    public int getDailyBounces(UUID playerId, ZoneId zone) {
        return 0; // TODO
    }

    @Override
    public List<Entry<UUID, Integer>> getTopAlltime(int topN) {
        return java.util.Collections.emptyList();
    }

    @Override
    public List<Entry<UUID, Integer>> getTopMonthly(int topN, ZoneId zone) {
        return java.util.Collections.emptyList();
    }

    @Override
    public List<Entry<UUID, Integer>> getTopWeekly(int topN, ZoneId zone, DayOfWeek weekStart) {
        return java.util.Collections.emptyList();
    }

    @Override
    public List<Entry<UUID, Integer>> getTopDaily(int topN, ZoneId zone) {
        return java.util.Collections.emptyList();
    }
}
