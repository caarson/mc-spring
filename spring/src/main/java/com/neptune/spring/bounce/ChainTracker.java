package com.neptune.spring.bounce;

import org.bukkit.entity.Player;
import java.util.*;

public class ChainTracker {
    private final Map<UUID, Map<String, Integer>> playerChains = new HashMap<>();
    private final Map<UUID, String> currentMaterial = new HashMap<>();
    
    public int getChainLevel(Player player, String materialName) {
        UUID playerId = player.getUniqueId();
        Map<String, Integer> chains = playerChains.get(playerId);
        
        // Ensure chains is never null
        if (chains == null) {
            chains = playerChains.computeIfAbsent(playerId, k -> new HashMap<>());
        }
        
        if (!materialName.equals(currentMaterial.get(playerId))) {
            // Reset chain if material changed
            resetChain(player);
            chains = playerChains.computeIfAbsent(playerId, k -> new HashMap<>());
        }
        
        // Final null check to be safe
        if (chains == null) {
            return 0;
        }
        
        return chains.getOrDefault(materialName, 0);
    }
    
    public void update(Player player, String materialName) {
        UUID playerId = player.getUniqueId();
        Map<String, Integer> chains = playerChains.computeIfAbsent(playerId, k -> new HashMap<>());
        
        // Reset chain if material changed
        if (!materialName.equals(currentMaterial.get(playerId))) {
            chains.put(materialName, 0);
            currentMaterial.put(playerId, materialName);
        }
        
        // Increment chain level
        int currentLevel = chains.getOrDefault(materialName, 0);
        chains.put(materialName, currentLevel + 1);
    }
    
    public void resetChain(Player player) {
        UUID playerId = player.getUniqueId();
        playerChains.remove(playerId);
        currentMaterial.remove(playerId);
    }
}
