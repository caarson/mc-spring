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
        
        // Validate config but catch any exceptions to prevent plugin disablement
        try {
            validateConfig();
        } catch (Exception e) {
            plugin.getLogger().severe("Config validation failed: " + e.getMessage());
            plugin.getLogger().severe("Using default configuration instead");
            // Set default values to prevent plugin disablement
            setDefaultConfig();
        }
    }
    
    private void setDefaultConfig() {
        configData = new HashMap<>();
        configData.put("enabled", true);
        configData.put("locale", "en_US");
        
        // Worlds config
        Map<String, Object> worlds = new HashMap<>();
        worlds.put("mode", "whitelist");
        worlds.put("list", Arrays.asList("spawn", "minigames"));
        configData.put("worlds", worlds);
        
        // Region config
        Map<String, Object> region = new HashMap<>();
        region.put("requireBounceFlag", true);
        region.put("allowRegionBypassPermission", false);
        configData.put("region", region);
        
        // Safety config
        Map<String, Object> safety = new HashMap<>();
        safety.put("cancelFallDamage", true);
        Map<String, Object> safeLandingMode = new HashMap<>();
        safeLandingMode.put("enabled", true);
        safeLandingMode.put("timeoutTicks", 60);
        safety.put("safeLandingMode", safeLandingMode);
        configData.put("safety", safety);
        
        // PlaceholderAPI config
        Map<String, Object> placeholderapi = new HashMap<>();
        placeholderapi.put("enabled", true);
        Map<String, Object> leaderboards = new HashMap<>();
        leaderboards.put("track", true);
        leaderboards.put("topSize", 5);
        leaderboards.put("weekStartsOn", "MONDAY");
        leaderboards.put("timezone", "America/New_York");
        Map<String, Object> storage = new HashMap<>();
        storage.put("type", "sqlite");
        storage.put("sqliteFile", "plugins/Spring/spring.db");
        storage.put("jsonFile", "plugins/Spring/stats.json");
        leaderboards.put("storage", storage);
        placeholderapi.put("leaderboards", leaderboards);
        configData.put("placeholderapi", placeholderapi);
        
        // Levels config (now as map)
        Map<String, Map<String, Object>> levels = new HashMap<>();
        
        Map<String, Object> gentle = new HashMap<>();
        gentle.put("verticalVelocity", 0.9);
        gentle.put("horizontalMultiplier", 1.0);
        gentle.put("anglePreservation", true);
        gentle.put("sound", "minecraft:block.slime_block.place");
        gentle.put("soundVolume", 1.0);
        gentle.put("soundPitch", 1.2);
        Map<String, Object> gentleParticles = new HashMap<>();
        gentleParticles.put("type", "minecraft:poof");
        gentleParticles.put("count", 10);
        gentleParticles.put("offset", Arrays.asList(0.3, 0.1, 0.3));
        gentle.put("particles", gentleParticles);
        levels.put("gentle", gentle);
        
        Map<String, Object> springy = new HashMap<>();
        springy.put("verticalVelocity", 1.4);
        springy.put("horizontalMultiplier", 1.1);
        springy.put("anglePreservation", true);
        springy.put("sound", "minecraft:block.honey_block.slide");
        springy.put("soundVolume", 1.0);
        springy.put("soundPitch", 0.9);
        Map<String, Object> springyParticles = new HashMap<>();
        springyParticles.put("type", "minecraft:slime");
        springyParticles.put("count", 12);
        springyParticles.put("offset", Arrays.asList(0.4, 0.15, 0.4));
        springy.put("particles", springyParticles);
        levels.put("springy", springy);
        
        Map<String, Object> rocket = new HashMap<>();
        rocket.put("verticalVelocity", 2.2);
        rocket.put("horizontalMultiplier", 0.0);
        rocket.put("anglePreservation", false);
        rocket.put("sound", "minecraft:entity.firework_rocket.launch");
        rocket.put("soundVolume", 1.0);
        rocket.put("soundPitch", 1.0);
        Map<String, Object> rocketParticles = new HashMap<>();
        rocketParticles.put("type", "minecraft:firework");
        rocketParticles.put("count", 6);
        rocketParticles.put("offset", Arrays.asList(0.0, 0.2, 0.0));
        rocket.put("particles", rocketParticles);
        levels.put("rocket", rocket);
        
        configData.put("levels", levels);
        
        // Materials config (now as list of objects)
        List<Map<String, Object>> materials = new ArrayList<>();
        Map<String, Object> slimeBlock = new HashMap<>();
        slimeBlock.put("material", "SLIME_BLOCK");
        slimeBlock.put("level", "springy");
        materials.add(slimeBlock);
        
        Map<String, Object> honeyBlock = new HashMap<>();
        honeyBlock.put("material", "HONEY_BLOCK");
        honeyBlock.put("level", "gentle");
        materials.add(honeyBlock);
        
        Map<String, Object> stone = new HashMap<>();
        stone.put("material", "STONE");
        stone.put("level", "rocket");
        materials.add(stone);
        
        configData.put("materials", materials);
        
        // Bounce chains config (now with level names as strings)
        Map<String, List<String>> bounceChains = new HashMap<>();
        bounceChains.put("SLIME_BLOCK", Arrays.asList("gentle", "springy", "rocket"));
        bounceChains.put("HONEY_BLOCK", Arrays.asList("gentle", "springy"));
        configData.put("bounceChains", bounceChains);
        
        // Debug config
        Map<String, Object> debug = new HashMap<>();
        debug.put("logStartupSummary", true);
        debug.put("logValidationErrors", true);
        configData.put("debug", debug);
    }
    
    public void reloadConfig() {
        loadConfig();
    }
    
    private void validateConfig() {
        // Validate levels
        Map<String, Map<String, Object>> levels = (Map<String, Map<String, Object>>) configData.get("levels");
        if (levels == null || levels.isEmpty()) {
            throw new IllegalArgumentException("Levels map cannot be empty");
        }
        
        // Validate materials
        List<Map<String, Object>> materials = (List<Map<String, Object>>) configData.get("materials");
        if (materials == null || materials.isEmpty()) {
            throw new IllegalArgumentException("Materials list cannot be empty");
        }
        
        // Validate bounce chains for each material
        Map<String, List<String>> bounceChains = (Map<String, List<String>>) configData.get("bounceChains");
        for (Map<String, Object> material : materials) {
            String materialName = (String) material.get("material");
            if (!bounceChains.containsKey(materialName)) {
                throw new IllegalArgumentException("Bounce chain for material " + materialName + " not found");
            }
            List<String> chain = bounceChains.get(materialName);
            for (String levelName : chain) {
                if (!levels.containsKey(levelName)) {
                    throw new IllegalArgumentException("Level " + levelName + " in chain for material " + materialName + " not found in levels");
                }
            }
        }
    }
    
    // Getters for configuration values
    public boolean isEnabled() {
        return (boolean) configData.getOrDefault("enabled", true);
    }
    
    public List<Map<String, Object>> getMaterialsList() {
        return (List<Map<String, Object>>) configData.get("materials");
    }
    
    public Map<String, Map<String, Object>> getLevels() {
        return (Map<String, Map<String, Object>>) configData.get("levels");
    }
    
    public Map<String, List<String>> getBounceChains() {
        return (Map<String, List<String>>) configData.get("bounceChains");
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
