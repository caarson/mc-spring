package com.neptune.spring.bounce;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.Particle;
import org.bukkit.Material;
import com.neptune.spring.config.ConfigManager;
import com.neptune.spring.safety.SafeLandingService;
import com.neptune.spring.bounce.ChainTracker;
import com.neptune.spring.util.ParticleUtil;
import java.util.List;
import java.util.Map;

public class BounceService implements Listener {
    private ConfigManager configManager;
    private SafeLandingService safeLandingService;
    private ChainTracker chainTracker;
    
    public BounceService(ConfigManager configManager) {
        this.configManager = configManager;
        this.safeLandingService = new SafeLandingService();
        this.chainTracker = new ChainTracker();
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!configManager.isEnabled()) return;
        
        Player player = event.getPlayer();
        Block blockUnderFeet = player.getLocation().subtract(0, 1, 0).getBlock();
        
        // Check if player is on ground and moving
        if (!player.isOnGround() || event.getFrom().distanceSquared(event.getTo()) < 0.01) {
            return;
        }
        
        String materialName = blockUnderFeet.getType().name();
        List<String> materials = configManager.getMaterialsList();
        
        if (materials.contains(materialName)) {
            // Get current chain level for this material
            int chainLevel = chainTracker.getChainLevel(player, materialName);
            List<Map<String, Object>> levels = configManager.getLevels();
            Map<String, List<Integer>> bounceChains = configManager.getBounceChains();
            
            if (chainLevel >= levels.size()) {
                chainLevel = levels.size() - 1; // Cap at max level
            }
            
            Map<String, Object> level = levels.get(chainLevel);
            double verticalVelocity = (double) level.get("verticalVelocity");
            double horizontalMultiplier = (double) level.get("horizontalMultiplier");
            boolean anglePreservation = (boolean) level.get("anglePreservation");
            
            // Calculate bounce velocity
            Vector velocity = player.getVelocity();
            Vector newVelocity = new Vector(
                velocity.getX() * horizontalMultiplier,
                verticalVelocity,
                velocity.getZ() * horizontalMultiplier
            );
            
            // Preserve incoming horizontal vector if anglePreservation is true
            if (anglePreservation) {
                newVelocity.setX(velocity.getX());
                newVelocity.setZ(velocity.getZ());
            }
            
            player.setVelocity(newVelocity);
            
            // Play particles
            ParticleUtil.playParticles(player, Particle.SPLASH);
            
            // Activate safe landing
            safeLandingService.activateSafeLanding(player, configManager.getSafetyTimeoutTicks());
            
            // Update chain level
            chainTracker.update(player, materialName);
        } else {
            // Reset chain if player steps off bounce material
            chainTracker.resetChain(player);
        }
    }
    
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!configManager.isEnabled()) return;
        
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            Block blockUnderFeet = player.getLocation().subtract(0, 1, 0).getBlock();
            String materialName = blockUnderFeet.getType().name();
            
            if (configManager.getMaterialsList().contains(materialName)) {
                // Reset chain when sneaking
                chainTracker.resetChain(player);
            }
        }
    }
}
