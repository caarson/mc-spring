package com.neptune.spring.bounce;

import org.bukkit.entity.Player;
import java.util.*;

public class ChainTracker {
    private final Map<Player, String> playerMaterialMap; // Player to current material
    private final Map<Player, Integer> playerChainIndexMap; // Player to current chain index
    
    public ChainTracker() {
        playerMaterialMap = new HashMap<>();
        playerChainIndexMap = new HashMap<>();
    }
    
    public void update(Player player, String material) {
        playerMaterialMap.put(player, material);
        // Update chain index based on material
        // Reset if step off or change block
    }
}
