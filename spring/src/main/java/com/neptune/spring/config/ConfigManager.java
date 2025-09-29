package com.neptune.spring.config;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

public class ConfigManager {
    private final Plugin plugin;
    private FileConfiguration config;
    private Map<String, Object> configData;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        // Create plugin data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        File configFile = new File(plugin.getDataFolder(), "config.json");
        
        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            plugin.saveResource("config.json", false);
        }
        
        // Load JSON config
        try {
            configData = objectMapper.readValue(configFile, Map.class);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load config.json: " + e.getMessage());
            configData = new HashMap<>();
        }
        
        validateConfig();
    }
    
    public void reloadConfig() {
        loadConfig();
    }
    
    private void validateConfig() {
        // Validate levels
        List<Map<String, Object>> levels = (List<Map<String, Object>>) configData.get("levels");
        if (levels == null || levels.isEmpty()) {
            throw new IllegalArgumentException("Levels list cannot be empty");
        }
        
        // Validate materials
        List<String> materials = (List<String>) configData.get("materials");
        if (materials == null || materials.isEmpty()) {
            throw new IllegalArgumentException("Materials list cannot be empty");
        }
        
        // Validate bounce chains for each material
        Map<String, List<Integer>> bounceChains = (Map<String, List<Integer>>) configData.get("bounceChains");
        for (String material : materials) {
            if (!bounceChains.containsKey(material)) {
                throw new IllegalArgumentException("Bounce chain for material " + material + " not found");
            }
            List<Integer> chain = bounceChains.get(material);
            if (chain.size() != levels.size()) {
                throw new IllegalArgumentException("Chain length must match levels count for material " + material);
            }
        }
    }
    
    // Getters for configuration values
    public boolean isEnabled() {
        return (boolean) configData.getOrDefault("enabled", true);
    }
    
    public List<String> getMaterialsList() {
        return (List<String>) configData.get("materials");
    }
    
    public List<Map<String, Object>> getLevels() {
        return (List<Map<String, Object>>) configData.get("levels");
    }
    
    public Map<String, List<Integer>> getBounceChains() {
        return (Map<String, List<Integer>>) configData.get("bounceChains");
    }
    
    public Map<String, Object> getWorldsConfig() {
        return (Map<String, Object>) configData.get("worlds");
    }
    
    public Map<String, Object> getRegionConfig() {
        return (Map<String, Object>) configData.get("region");
    }
    
    public Map<String, Object> getSafetyConfig() {
        return (Map<String, Object>) configData.get("safety");
    }
    
    public int getSafetyTimeoutTicks() {
        Map<String, Object> safety = getSafetyConfig();
        return safety != null ? (int) safety.getOrDefault("timeoutTicks", 20) : 20;
    }
    
    public boolean getDebugLoggingEnabled() {
        Map<String, Object> debug = (Map<String, Object>) configData.get("debug");
        return debug != null ? (boolean) debug.getOrDefault("loggingEnabled", false) : false;
    }
    
    public String getListStatus() {
        StringBuilder status = new StringBuilder();
        status.append("=== Spring Plugin Status ===\n");
        status.append("Enabled: ").append(isEnabled()).append("\n");
        status.append("Materials: ").append(getMaterialsList()).append("\n");
        status.append("Levels: ").append(getLevels().size()).append("\n");
        status.append("Worlds Mode: ").append(getWorldsConfig().get("mode")).append("\n");
        status.append("Worlds List: ").append(getWorldsConfig().get("list")).append("\n");
        status.append("Debug Logging: ").append(getDebugLoggingEnabled()).append("\n");
        return status.toString();
    }
}
