package com.neptune.spring;

import org.bukkit.plugin.java.JavaPlugin;
import com.neptune.spring.command.SpringCommand;
import com.neptune.spring.config.ConfigManager;
import com.neptune.spring.integration.WorldGuardHook;
import com.neptune.spring.integration.LuckPermsHook;
import com.neptune.spring.bounce.BounceService;
import com.neptune.spring.safety.SafeLandingService;
import com.neptune.spring.storage.JsonStatsStore;
import com.neptune.spring.storage.StatsStore;
import com.neptune.spring.storage.SqliteStatsStore;
import com.neptune.spring.integration.PapiExpansion;
import java.time.ZoneId;
import java.time.DayOfWeek;
import java.util.Map;

public class SpringPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private StatsStore statsStore;
    
    @Override
    public void onEnable() {
        // Initialize config manager first
        configManager = new ConfigManager(this);
        
        // Register command
        getCommand("spring").setExecutor(new SpringCommand(configManager));
        
        // Setup hooks for optional dependencies (WorldGuard, LuckPerms)
        // Use try-catch to handle missing optional dependencies gracefully
        try {
            if (WorldGuardHook.isAvailable()) {
                new WorldGuardHook(this);
            }
        } catch (NoClassDefFoundError e) {
            getLogger().warning("WorldGuard not found, skipping integration");
        }
        
        try {
            if (LuckPermsHook.isAvailable()) {
                new LuckPermsHook(this);
            }
        } catch (NoClassDefFoundError e) {
            getLogger().warning("LuckPerms not found, skipping integration");
        }
        
        // Setup stats store (PlaceholderAPI leaderboards)
        try {
            Map<String, Object> placeholderapi = configManager.getPlaceholderConfig();
            Map<String, Object> leaderboards = placeholderapi != null ? (Map<String, Object>) placeholderapi.get("leaderboards") : null;
            if (leaderboards != null) {
                Map<String, Object> storage = (Map<String, Object>) leaderboards.get("storage");
                if (storage != null && "sqlite".equalsIgnoreCase((String) storage.get("type"))) {
                    String sqliteFile = (String) storage.getOrDefault("sqliteFile", "plugins/Spring/spring.db");
                    statsStore = new SqliteStatsStore(this); // Sqlite implementation TODO
                } else {
                    String jsonFile = storage != null ? (String) storage.getOrDefault("jsonFile", "plugins/Spring/stats.json") : "plugins/Spring/stats.json";
                    statsStore = new JsonStatsStore(this, jsonFile);
                }
            }
        } catch (Exception e) {
            getLogger().warning("Failed to initialize stats store: " + e.getMessage());
            statsStore = null;
        }

        // Register SafeLandingService as a single shared listener
        SafeLandingService safeLandingService = new SafeLandingService();
        safeLandingService.setPlugin(this);
        getServer().getPluginManager().registerEvents(safeLandingService, this);

        // Register BounceService and pass the shared SafeLandingService
        getServer().getPluginManager().registerEvents(new BounceService(configManager, safeLandingService, statsStore), this);

        // PlaceholderAPI integration (register expansion only if available and enabled)
        try {
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null && configManager.isPlaceholderApiEnabled() && statsStore != null) {
                new PapiExpansion(this, statsStore, ZoneId.of((String) ((Map<String,Object>)configManager.getPlaceholderConfig().get("leaderboards")).getOrDefault("timezone", "UTC")), DayOfWeek.valueOf((String) ((Map<String,Object>)configManager.getPlaceholderConfig().get("leaderboards")).getOrDefault("weekStartsOn", "MONDAY")));
            }
        } catch (NoClassDefFoundError e) {
            getLogger().warning("PlaceholderAPI not found, skipping expansion registration");
        }
    }

    @Override
    public void onDisable() {
        configManager = null;
        // Close DBs if using SqliteStatsStore
        // Clean up any resources
    }
}
