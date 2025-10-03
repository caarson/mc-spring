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
        
        // Check if player is on ground and moving downward to trigger bounce
        if (!player.isOnGround() || player.getVelocity().getY() >= 0) {
            return;
        }
        
        String materialName = blockUnderFeet.getType().name();
        List<Map<String, Object>> materials = configManager.getMaterialsList();
        
        // Find if the material is configured
        String configuredMaterial = null;
        for (Map<String, Object> materialConfig : materials) {
            if (materialConfig.get("material").equals(materialName)) {
                configuredMaterial = materialName;
                break;
            }
        }
        
        if (configuredMaterial != null) {
            // Get current chain level for this material
            int chainLevel = chainTracker.getChainLevel(player, configuredMaterial);
            Map<String, Map<String, Object>> levels = configManager.getLevels();
            Map<String, List<String>> bounceChains = configManager.getBounceChains();
            
            // Get the chain for this material
            List<String> chain = bounceChains.get(configuredMaterial);
            if (chain == null || chain.isEmpty()) {
                return;
            }
            
            // Get the level name for current chain position
            String levelName = chain.get(chainLevel % chain.size());
            Map<String, Object> level = levels.get(levelName);
            
            if (level == null) {
                return;
            }
            
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
            try {
                String particleType = (String) ((Map<String, Object>) level.get("particles")).get("type");
                int particleCount = (int) ((Map<String, Object>) level.get("particles")).get("count");
                ParticleUtil.playCustomParticles(player, particleType, particleCount);
            } catch (Exception e) {
                ParticleUtil.playParticles(player, Particle.ITEM_SLIME);
            }
            
            // Play sound
            try {
                String sound = (String) level.get("sound");
                float volume = ((Number) level.get("soundVolume")).floatValue();
                float pitch = ((Number) level.get("soundPitch")).floatValue();
                player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception e) {
                // Use default sound if specified sound fails
                player.getWorld().playSound(player.getLocation(), "block.slime_block.place", 1.0f, 1.2f);
            }
            
            // Activate safe landing
            safeLandingService.activateSafeLanding(player, configManager.getSafetyTimeoutTicks());
            
            // Update chain level
            chainTracker.update(player, configuredMaterial);
            
            // Debug logging
            if (configManager.getDebugLoggingEnabled()) {
                player.sendMessage("§aBounce! Level: " + levelName + ", Material: " + configuredMaterial + ", Velocity: " + verticalVelocity);
            }
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
