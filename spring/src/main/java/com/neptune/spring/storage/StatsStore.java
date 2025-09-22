package com.neptune.spring.storage;

import org.bukkit.entity.Player;

public interface StatsStore {
    void incrementBounce(Player player);
    // Periodized counters (daily/weekly/monthly/all-time) using configured timezone + week start
}
