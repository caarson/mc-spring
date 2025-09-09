package com.neptune.spring.storage;

public interface StatsStore {
    void incrementBounce(Player player);
    // Periodized counters (daily/weekly/monthly/all-time) using configured timezone + week start
}
