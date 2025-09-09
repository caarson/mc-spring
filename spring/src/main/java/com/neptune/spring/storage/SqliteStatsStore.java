package com.neptune.spring.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.plugin.Plugin;

public class SqliteStatsStore implements StatsStore {
    private final Plugin plugin;
    private Connection connection;
    
    public SqliteStatsStore(Plugin plugin) {
        this.plugin = plugin;
        initializeDB();
    }
    
    private void initializeDB() {
        // Connect to plugins/Spring/spring.db
    }
    
    @Override
    public void incrementBounce(Player player) {
        // Increment count in DB for player
    }
}
