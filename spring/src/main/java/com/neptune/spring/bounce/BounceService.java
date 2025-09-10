package com.neptune.spring.bounce;

import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import org.bukkit.Material;
import com.neptune.spring.config.ConfigManager;
import com.neptune.spring.safety.SafeLandingService;
import com.neptune.spring.bounce.ChainTracker;
import com.neptune.spring.util.ParticleUtil;

public class BounceService {
    private ConfigManager configManager;
    private SafeLandingService safeLandingService;
    private ChainTracker chainTracker;
    
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block blockUnderFeet = player.getLocation().getBlockBelow(); // Get block under feet
        
        String materialName = blockUnderFeet.getType().name(); // Get material name
        
        if (configManager.getMaterialsList().contains(materialName)) {
            LevelSpec level = configManager.getLevelForMaterial(materialName);
            
            Vector velocity = new Vector(
                0, 
                level.verticalVelocity * event.getY(), 
                level.horizontalMultiplier * event.getZ()
            );
            
            // Preserve incoming horizontal vector if anglePreservation is true
            if (level.anglePreservation) {
                velocity.setX(event.getX());
                velocity.setZ(event.getZ());
            }
            
            player.setVelocity(velocity);
            
            ParticleUtil.playParticles(player, Particle.SLIME); // For example, SLIME particle
            
            safeLandingService.activateSafeLanding(player, configManager.getSafetyTimeoutTicks());
            
            chainTracker.update(player, materialName); // Update chain index
        }
    }
    
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        
        Block blockUnderFeet = player.getLocation().getBlockBelow();
        String materialName = blockUnderFeet.getType().name();
        
        if (configManager.getMaterialsList().contains(materialName)) {
            // Similar logic for sneaking detection
            // ... same as onPlayerMove but with sneaking conditions
            
            chainTracker.update(player, materialName);
        }
    }
}
