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

    // New method
    public Plugin getPlugin() {
        return plugin;
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
            plugin.getLogger().info("Default config.json copied to: " + configFile.getAbsolutePath());
        }

        // Load JSON config
        try {
            configData = objectMapper.readValue(configFile, Map.class);
            plugin.getLogger().info("Successfully loaded config from: " + configFile.getAbsolutePath());

            // Debug: Log worlds configuration
            if (configData.containsKey("worlds")) {
                Map<String, Object> worlds = (Map<String, Object>) configData.get("worlds");
                plugin.getLogger().info("Worlds config loaded - Mode: " + worlds.get("mode") + ", List: " + worlds.get("list"));
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load config.json: " + e.getMessage());
            configData = new HashMap<>();
        }

        // Validate config but catch any exceptions to prevent plugin disablement
        try {
            validateConfig();
            ensureBounceChainsPresent();
        } catch (Exception e) {
            plugin.getLogger().severe("Config validation failed: " + e.getMessage());
            plugin.getLogger().severe("Using default configuration instead");
            // Set default values to prevent plugin disablement
            setDefaultConfig();
            // Defaults may still miss bounce chains; ensure now
            ensureBounceChainsPresent();
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

    /**
     * Normalize a configured material string into a Bukkit Material key form (uppercase enum name).
     * Accepts forms like "tuff", "minecraft:tuff", and "TUFF".
     */
    public static String normalizeMaterialKey(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        int colon = v.indexOf(':');
        if (colon >= 0 && colon < v.length() - 1) {
            v = v.substring(colon + 1);
        }
        v = v.replace('-', '_');
        return v.toUpperCase(Locale.ROOT);
    }

    /**
     * Ensure every material listed has a bounce chain. If absent, create a single-step chain
     * using that material's initial level so materials like TUFF start working immediately.
     */
    @SuppressWarnings("unchecked")
    private void ensureBounceChainsPresent() {
        if (configData == null) return;
        List<Map<String, Object>> materials = (List<Map<String, Object>>) configData.get("materials");
        if (materials == null) return;

        Map<String, List<String>> bounceChains = (Map<String, List<String>>) configData.get("bounceChains");
        if (bounceChains == null) {
            bounceChains = new HashMap<>();
            configData.put("bounceChains", bounceChains);
        }

        for (Map<String, Object> matCfg : materials) {
            Object m = matCfg.get("material");
            Object lvl = matCfg.get("level");
            if (m == null || lvl == null) continue;
            String key = normalizeMaterialKey(m.toString());
            if (!bounceChains.containsKey(key)) {
                bounceChains.put(key, Collections.singletonList(lvl.toString()));
                plugin.getLogger().info("Added default bounce chain for material " + key + " -> [" + lvl + "]");
            }
        }
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

        // Validate bounce chains for each material, but do not fail hard; log and allow fallback.
        Map<String, List<String>> bounceChains = (Map<String, List<String>>) configData.get("bounceChains");
        if (bounceChains == null) bounceChains = new HashMap<>();
        for (Map<String, Object> material : materials) {
            String materialName = normalizeMaterialKey(String.valueOf(material.get("material")));
            if (!bounceChains.containsKey(materialName)) {
                getPlugin().getLogger().warning("Missing bounce chain for material " + materialName + "; will default to its initial level.");
                continue;
            }
            List<String> chain = bounceChains.get(materialName);
            for (String levelName : chain) {
                if (!levels.containsKey(levelName)) {
                    getPlugin().getLogger().warning("Unknown level '" + levelName + "' in chain for material " + materialName + "; this step will be ignored.");
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

    public Map<String, Object> getPlaceholderConfig() {
        return (Map<String, Object>) configData.get("placeholderapi");
    }

    public boolean isPlaceholderApiEnabled() {
        Map<String, Object> placeholderapi = getPlaceholderConfig();
        return placeholderapi != null ? (boolean) placeholderapi.getOrDefault("enabled", false) : false;
    }

    public int getSafetyTimeoutTicks() {
        Map<String, Object> safety = getSafetyConfig();
        if (safety == null) return 20;
        // support both legacy root value and nested safeLandingMode.timeoutTicks
        Object direct = safety.get("timeoutTicks");
        if (direct instanceof Number) return ((Number) direct).intValue();
        Object sl = safety.get("safeLandingMode");
        if (sl instanceof Map) {
            Object t = ((Map<?, ?>) sl).get("timeoutTicks");
            if (t instanceof Number) return ((Number) t).intValue();
        }
        return 20;
    }

    public boolean getDebugLoggingEnabled() {
        Map<String, Object> debug = (Map<String, Object>) configData.get("debug");
        // Align with keys used in config; treat any of these as enabling verbose logs
        if (debug == null) return false;
        Object v = debug.get("loggingEnabled");
        if (v instanceof Boolean) return (Boolean) v;
        Object ls = debug.get("logStartupSummary");
        Object le = debug.get("logValidationErrors");
        return Boolean.TRUE.equals(ls) || Boolean.TRUE.equals(le);
    }

    public String getListStatus() {
        StringBuilder status = new StringBuilder();
        status.append("§6=== Spring Plugin Status ===\n");
        status.append("§eEnabled: §f").append(isEnabled()).append("\n");
        status.append("§eLocale: §f").append(configData.getOrDefault("locale", "en_US")).append("\n");

        // Worlds configuration
        Map<String, Object> worlds = getWorldsConfig();
        if (worlds != null) {
            status.append("§eWorlds Mode: §f").append(worlds.get("mode")).append("\n");
            Object worldsList = worlds.get("list");
            if (worldsList instanceof List) {
                StringBuilder worldsStr = new StringBuilder();
                for (Object world : ((List<?>) worldsList)) {
                    if (worldsStr.length() > 0) {
                        worldsStr.append(", ");
                    }
                    worldsStr.append(world.toString());
                }
                status.append("§eWorlds List: §f").append(worldsStr.toString()).append("\n");
            } else {
                status.append("§eWorlds List: §f").append(worldsList).append("\n");
            }
        } else {
            status.append("§eWorlds: §fNot configured\n");
        }

        // Region configuration
        Map<String, Object> region = getRegionConfig();
        status.append("§eRequire Bounce Flag: §f").append(region.get("requireBounceFlag")).append("\n");

        // Safety configuration
        Map<String, Object> safety = getSafetyConfig();
        status.append("§eCancel Fall Damage: §f").append(safety.get("cancelFallDamage")).append("\n");
        status.append("§eSafe Landing Mode: §f").append(((Map<String, Object>) safety.get("safeLandingMode")).get("enabled")).append("\n");

        // PlaceholderAPI configuration
        Map<String, Object> placeholderapi = (Map<String, Object>) configData.get("placeholderapi");
        if (placeholderapi != null) {
            status.append("§ePlaceholderAPI Enabled: §f").append(placeholderapi.get("enabled")).append("\n");
            Map<String, Object> leaderboards = (Map<String, Object>) placeholderapi.get("leaderboards");
            if (leaderboards != null) {
                status.append("§eLeaderboards Tracking: §f").append(leaderboards.get("track")).append("\n");
                status.append("§eTop Size: §f").append(leaderboards.get("topSize")).append("\n");
            }
        }

        // Levels
        Map<String, Map<String, Object>> levels = getLevels();
            status.append("§eLevels (").append(levels.size()).append("):\n");
        for (String levelName : levels.keySet()) {
            Map<String, Object> level = levels.get(levelName);
            status.append("  §7- §f").append(levelName).append(": §7vertical=")
                  .append(level.get("verticalVelocity")).append(", horizontal=")
                .append(level.get("horizontalMultiplier")).append("\n");
        }

        // Materials
        List<Map<String, Object>> materials = getMaterialsList();
            status.append("§eMaterials (").append(materials.size()).append("):\n");
        for (Map<String, Object> material : materials) {
            status.append("  §7- §f").append(normalizeMaterialKey(String.valueOf(material.get("material")))).append(": §7initial=")
                .append(material.get("level")).append("\n");
        }

        // Bounce Chains
        Map<String, List<String>> bounceChains = getBounceChains();
        status.append("§eBounce Chains:\n");
        for (String material : bounceChains.keySet()) {
            status.append("  §7- §f").append(material).append(": §7")
                  .append(String.join(" → ", bounceChains.get(material))).append("\n");
        }

        // Debug
        Map<String, Object> debug = (Map<String, Object>) configData.get("debug");
        if (debug != null) {
            status.append("§eLog Startup Summary: §f").append(debug.get("logStartupSummary")).append("\n");
            status.append("§eLog Validation Errors: §f").append(debug.get("logValidationErrors")).append("\n");
        }

        return status.toString();
    }
}
