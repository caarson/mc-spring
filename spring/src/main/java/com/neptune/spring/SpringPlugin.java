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
    public void onLoad() {
        // Register WorldGuard flags during onLoad() - required for WG 7.0+
        try {
            if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
                com.neptune.spring.integration.WorldGuardHook.registerFlags(this);
                getLogger().info("WorldGuard detected - registering custom flags");
            }
        } catch (Exception e) {
            getLogger().warning("Failed to register WorldGuard flags: " + e.getMessage());
        }
    }
    
    @Override
    public void onEnable() {
        // Initialize config manager first
        configManager = new ConfigManager(this);
        
        // Register command
        SpringCommand springCommand = new SpringCommand(configManager);
        getCommand("spring").setExecutor(springCommand);
        getCommand("spring").setTabCompleter(springCommand);
        
        // WorldGuard hook is already initialized in onLoad()
        // Just log if it's available
        if (com.neptune.spring.integration.WorldGuardHook.isAvailable()) {
            getLogger().info("WorldGuard integration active");
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
