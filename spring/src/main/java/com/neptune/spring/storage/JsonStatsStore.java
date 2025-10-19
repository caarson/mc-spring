package com.neptune.spring.storage;

import org.bukkit.plugin.Plugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.*;
import java.time.temporal.IsoFields;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonStatsStore implements StatsStore {
    private final Plugin plugin;
    private final File statsFile;
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, Object> data = new HashMap<>();

    public JsonStatsStore(Plugin plugin, String jsonFilePath) {
        this.plugin = plugin;
        this.statsFile = new File(jsonFilePath);
        initStatsFile();
    }

    @SuppressWarnings("unchecked")
    private synchronized void initStatsFile() {
        try {
            if (!statsFile.exists()) {
                statsFile.getParentFile().mkdirs();
                statsFile.createNewFile();
                data = new HashMap<>();
                mapper.writerWithDefaultPrettyPrinter().writeValue(statsFile, data);
                return;
            }

            byte[] bytes = Files.readAllBytes(statsFile.toPath());
            if (bytes.length == 0) {
                data = new HashMap<>();
            } else {
                data = mapper.readValue(bytes, new TypeReference<Map<String, Object>>() {});
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to initialize stats file: " + e.getMessage());
            data = new HashMap<>();
        }
    }

    private synchronized void persist() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(statsFile, data);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write stats file: " + e.getMessage());
        }
    }

    private String uuidKey(UUID id) {
        return id.toString();
    }

    @Override
    public synchronized void incrementBounce(UUID playerId, String playerName) {
        String key = uuidKey(playerId);
        Map<String, Object> players = (Map<String, Object>) data.get("players");
        if (players == null) {
            players = new HashMap<>();
            data.put("players", players);
        }

        Map<String, Object> p = (Map<String, Object>) players.getOrDefault(key, new HashMap<>());
        p.put("name", playerName);

        // all-time
        int alltime = ((Number) ((Map<String, Object>) p).getOrDefault("alltime", 0)).intValue();
        p.put("alltime", alltime + 1);

        // date keys
        ZoneId zone = ZoneId.systemDefault();
        LocalDate now = LocalDate.now(zone);
        String dayKey = now.format(DateTimeFormatter.ISO_DATE);
        YearMonth month = YearMonth.from(now);
        String monthKey = month.toString();
        String weekKey = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + "-" + now.get(IsoFields.WEEK_BASED_YEAR);

        Map<String, Number> daily = (Map<String, Number>) p.getOrDefault("daily", new HashMap<>());
        daily.put(dayKey, daily.getOrDefault(dayKey, 0).intValue() + 1);
        p.put("daily", daily);

        Map<String, Number> monthly = (Map<String, Number>) p.getOrDefault("monthly", new HashMap<>());
        monthly.put(monthKey, monthly.getOrDefault(monthKey, 0).intValue() + 1);
        p.put("monthly", monthly);

        Map<String, Number> weekly = (Map<String, Number>) p.getOrDefault("weekly", new HashMap<>());
        weekly.put(weekKey, weekly.getOrDefault(weekKey, 0).intValue() + 1);
        p.put("weekly", weekly);

        players.put(key, p);
        data.put("players", players);
        persist();
    }

    @SuppressWarnings("unchecked")
    private synchronized Map<String, Object> getPlayerRecord(UUID playerId) {
        Map<String, Object> players = (Map<String, Object>) data.get("players");
        if (players == null) return null;
        return (Map<String, Object>) players.get(uuidKey(playerId));
    }

    @Override
    public synchronized int getAlltimeBounces(UUID playerId) {
        Map<String, Object> p = getPlayerRecord(playerId);
        if (p == null) return 0;
        return ((Number) p.getOrDefault("alltime", 0)).intValue();
    }

    @Override
    public synchronized int getMonthlyBounces(UUID playerId, ZoneId zone) {
        Map<String, Object> p = getPlayerRecord(playerId);
        if (p == null) return 0;
        YearMonth ym = YearMonth.now(zone);
        String key = ym.toString();
        Map<String, Number> monthly = (Map<String, Number>) p.getOrDefault("monthly", Collections.emptyMap());
        return monthly.getOrDefault(key, 0).intValue();
    }

    @Override
    public synchronized int getWeeklyBounces(UUID playerId, ZoneId zone, DayOfWeek weekStart) {
        Map<String, Object> p = getPlayerRecord(playerId);
        if (p == null) return 0;
        LocalDate now = LocalDate.now(zone);
        String weekKey = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + "-" + now.get(IsoFields.WEEK_BASED_YEAR);
        Map<String, Number> weekly = (Map<String, Number>) p.getOrDefault("weekly", Collections.emptyMap());
        return weekly.getOrDefault(weekKey, 0).intValue();
    }

    @Override
    public synchronized int getDailyBounces(UUID playerId, ZoneId zone) {
        Map<String, Object> p = getPlayerRecord(playerId);
        if (p == null) return 0;
        String dayKey = LocalDate.now(zone).format(DateTimeFormatter.ISO_DATE);
        Map<String, Number> daily = (Map<String, Number>) p.getOrDefault("daily", Collections.emptyMap());
        return daily.getOrDefault(dayKey, 0).intValue();
    }

    private List<Entry<UUID, Integer>> sortAndLimit(Map<UUID, Integer> map, int topN) {
        List<Entry<UUID, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        if (list.size() > topN) return list.subList(0, topN);
        return list;
    }

    @Override
    public synchronized List<Entry<UUID, Integer>> getTopAlltime(int topN) {
        Map<UUID, Integer> map = new HashMap<>();
        Map<String, Object> players = (Map<String, Object>) data.getOrDefault("players", Collections.emptyMap());
        for (String key : players.keySet()) {
            UUID id = UUID.fromString(key);
            Map<String, Object> p = (Map<String, Object>) players.get(key);
            int alltime = ((Number) p.getOrDefault("alltime", 0)).intValue();
            map.put(id, alltime);
        }
        return sortAndLimit(map, topN);
    }

    @Override
    public synchronized List<Entry<UUID, Integer>> getTopMonthly(int topN, ZoneId zone) {
        YearMonth ym = YearMonth.now(zone);
        String key = ym.toString();
        Map<UUID, Integer> map = new HashMap<>();
        Map<String, Object> players = (Map<String, Object>) data.getOrDefault("players", Collections.emptyMap());
        for (String k : players.keySet()) {
            Map<String, Object> p = (Map<String, Object>) players.get(k);
            Map<String, Number> monthly = (Map<String, Number>) p.getOrDefault("monthly", Collections.emptyMap());
            int v = monthly.getOrDefault(key, 0).intValue();
            map.put(UUID.fromString(k), v);
        }
        return sortAndLimit(map, topN);
    }

    @Override
    public synchronized List<Entry<UUID, Integer>> getTopWeekly(int topN, ZoneId zone, DayOfWeek weekStart) {
        LocalDate now = LocalDate.now(zone);
        String weekKey = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + "-" + now.get(IsoFields.WEEK_BASED_YEAR);
        Map<UUID, Integer> map = new HashMap<>();
        Map<String, Object> players = (Map<String, Object>) data.getOrDefault("players", Collections.emptyMap());
        for (String k : players.keySet()) {
            Map<String, Object> p = (Map<String, Object>) players.get(k);
            Map<String, Number> weekly = (Map<String, Number>) p.getOrDefault("weekly", Collections.emptyMap());
            int v = weekly.getOrDefault(weekKey, 0).intValue();
            map.put(UUID.fromString(k), v);
        }
        return sortAndLimit(map, topN);
    }

    @Override
    public synchronized List<Entry<UUID, Integer>> getTopDaily(int topN, ZoneId zone) {
        String dayKey = LocalDate.now(zone).format(DateTimeFormatter.ISO_DATE);
        Map<UUID, Integer> map = new HashMap<>();
        Map<String, Object> players = (Map<String, Object>) data.getOrDefault("players", Collections.emptyMap());
        for (String k : players.keySet()) {
            Map<String, Object> p = (Map<String, Object>) players.get(k);
            Map<String, Number> daily = (Map<String, Number>) p.getOrDefault("daily", Collections.emptyMap());
            int v = daily.getOrDefault(dayKey, 0).intValue();
            map.put(UUID.fromString(k), v);
        }
        return sortAndLimit(map, topN);
    }
}
