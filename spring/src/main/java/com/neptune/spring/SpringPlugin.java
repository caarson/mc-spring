package com.neptune.spring;

import org.bukkit.plugin.java.JavaPlugin;
import com.neptune.spring.command.SpringCommand;
import com.neptune.spring.config.ConfigManager;
import com.neptune.spring.integration.WorldGuardHook;
import com.neptune.spring.integration.LuckPermsHook;
import com.neptune.spring.bounce.BounceService;
import com.neptune.spring.safety.SafeLandingService;

public class SpringPlugin extends JavaPlugin {
    private ConfigManager configManager;
    
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
        
        // Register BounceService as listener
        getServer().getPluginManager().registerEvents(new BounceService(configManager), this);
        
        // Register SafeLandingService as listener
        SafeLandingService safeLandingService = new SafeLandingService();
        safeLandingService.setPlugin(this);
        getServer().getPluginManager().registerEvents(safeLandingService, this);
    }

    @Override
    public void onDisable() {
        configManager = null;
        // Close DBs if using SqliteStatsStore
        // Clean up any resources
    }
}
