package com.neptune.spring.safety;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;
import java.util.*;

public class SafeLandingService {
    private final Map<Player, Integer> playerSafeLandingMap; // Player to expiration ticks
    
    public SafeLandingService() {
        playerSafeLandingMap = new HashMap<>();
    }
    
    public void activateSafeLanding(Player player, int timeoutTicks) {
        playerSafeLandingMap.put(player, timeoutTicks);
    }
    
    public boolean isSafeLandingActive(Player player) {
        return playerSafeLandingMap.containsKey(player);
    }
    
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && isSafeLandingActive(event.getEntity())) {
            event.setCancelled(true); // cancel fall damage
        }
    }
}
