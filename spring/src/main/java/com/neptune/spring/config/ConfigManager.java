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
        // Validate levels
        if (config.getList("levels") == null || config.getList("levels").isEmpty()) {
            throw new IllegalArgumentException("Levels list cannot be empty");
        }
        
        // Validate materials
        List<String> materials = config.getStringList("materials");
        if (materials == null || materials.isEmpty()) {
            throw new IllegalArgumentException("Materials list cannot be empty");
        }
        
        // Validate bounce chains for each material
        Map<String, List<Integer>> bounceChains = config.getMap("bounceChains");
        for (String material : materials) {
            if (!bounceChains.containsKey(material)) {
                throw new IllegalArgumentException("Bounce chain for material " + material + " not found");
            }
            List<Integer> chain = bounceChains.get(material);
            if (chain.size() != config.getList("levels").size()) {
                throw new IllegalArgumentException("Chain length must match levels count for material " + material);
            }
        }
    }
}
