package com.neptune.spring.integration;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.plugin.Plugin;

public class PapiExpansion {
    private final Plugin plugin;
    
    public PapiExpansion(Plugin plugin) {
        this.plugin = plugin;
        registerPlaceholders();
    }
    
    private void registerPlaceholders() {
        // Register placeholders:
        // %spring_bounces_alltime%, %spring_bounces_month%, %spring_bounces_week%, %spring_bounces_day%
        // %spring_top_name_X_alltime% / %spring_top_count_X_alltime% (and for month/week/day) where X = 1..topSize
    }
}
