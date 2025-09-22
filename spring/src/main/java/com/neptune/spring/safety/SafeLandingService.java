package com.neptune.spring.safety;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class SafeLandingService implements Listener {
    private final Map<UUID, Integer> safeLandingPlayers = new HashMap<>();
    private Plugin plugin;
    
    public SafeLandingService() {
        // This will be set when registered in SpringPlugin
    }
    
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }
    
    public void activateSafeLanding(Player player, int timeoutTicks) {
        UUID playerId = player.getUniqueId();
        safeLandingPlayers.put(playerId, timeoutTicks);
        
        // Set metadata for easy access
        player.setMetadata("spring-safe-landing", new FixedMetadataValue(plugin, true));
        
        // Schedule removal after timeout
        new BukkitRunnable() {
            @Override
            public void run() {
                safeLandingPlayers.remove(playerId);
                player.removeMetadata("spring-safe-landing", plugin);
            }
        }.runTaskLater(plugin, timeoutTicks);
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        
        // Check if damage is from falling and player has safe landing
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && 
            safeLandingPlayers.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            safeLandingPlayers.remove(player.getUniqueId());
            player.removeMetadata("spring-safe-landing", plugin);
        }
    }
    
    public boolean hasSafeLanding(Player player) {
        return safeLandingPlayers.containsKey(player.getUniqueId());
    }
}
