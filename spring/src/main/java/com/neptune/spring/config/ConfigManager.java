package com.neptune.spring.config;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.*;

public class ConfigManager {
    private final Plugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        // Load from spring/src/main/resources/config.json
        config = plugin.getConfig();
        validateConfig();
    }
    
    private void validateConfig() {
        // Validate levels/materials/chains on load - empty stub for now
    }
}
