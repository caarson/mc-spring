package com.neptune.spring.integration;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import com.neptune.spring.storage.StatsStore;
import java.time.ZoneId;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map.Entry;

public class PapiExpansion extends PlaceholderExpansion {
    private final Plugin plugin;
    private final StatsStore statsStore;
    private final ZoneId zone;
    private final DayOfWeek weekStart;

    public PapiExpansion(Plugin plugin, StatsStore statsStore, ZoneId zone, DayOfWeek weekStart) {
        this.plugin = plugin;
        this.statsStore = statsStore;
        this.zone = zone;
        this.weekStart = weekStart;
        this.register();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "spring";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().isEmpty() ? "unknown" : plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        try {
            if (identifier.equals("bounces_alltime")) {
                return String.valueOf(player == null ? 0 : statsStore.getAlltimeBounces(player.getUniqueId()));
            }
            if (identifier.equals("bounces_month")) {
                return String.valueOf(player == null ? 0 : statsStore.getMonthlyBounces(player.getUniqueId(), zone));
            }
            if (identifier.equals("bounces_week")) {
                return String.valueOf(player == null ? 0 : statsStore.getWeeklyBounces(player.getUniqueId(), zone, weekStart));
            }
            if (identifier.equals("bounces_day")) {
                return String.valueOf(player == null ? 0 : statsStore.getDailyBounces(player.getUniqueId(), zone));
            }

            // top placeholders: identifier like top_name_1_alltime, top_count_1_alltime
            if (identifier.startsWith("top_name_") || identifier.startsWith("top_count_")) {
                String[] parts = identifier.split("_");
                // parts: [top, name|count, X, period]
                if (parts.length >= 4) {
                    int idx = Integer.parseInt(parts[2]) - 1;
                    String period = parts[3];
                    List<Entry<java.util.UUID, Integer>> top = null;
                    switch (period) {
                        case "alltime":
                            top = statsStore.getTopAlltime(idx + 1);
                            break;
                        case "month":
                            top = statsStore.getTopMonthly(idx + 1, zone);
                            break;
                        case "week":
                            top = statsStore.getTopWeekly(idx + 1, zone, weekStart);
                            break;
                        case "day":
                            top = statsStore.getTopDaily(idx + 1, zone);
                            break;
                    }
                    if (top == null || idx < 0 || idx >= top.size()) return "";
                    Entry<java.util.UUID, Integer> entry = top.get(idx);
                    if (parts[1].equals("name")) {
                        return plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
                    } else {
                        return String.valueOf(entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            return "";
        }
        return null;
    }
}
