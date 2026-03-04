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
import java.util.HashMap;
import java.util.UUID;

public class BounceService implements Listener {
    private ConfigManager configManager;
    private SafeLandingService safeLandingService;
    private ChainTracker chainTracker;
    private com.neptune.spring.storage.StatsStore statsStore;
    private Map<UUID, Vector> previousVelocity = new HashMap<>();
    
    public BounceService(ConfigManager configManager, SafeLandingService safeLandingService, com.neptune.spring.storage.StatsStore statsStore) {
        this.configManager = configManager;
        this.safeLandingService = safeLandingService;
        this.chainTracker = new ChainTracker();
        this.statsStore = statsStore;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!configManager.isEnabled()) return;
        
        Player player = event.getPlayer();
        
        // Check WorldGuard region permissions if configured
        Map<String, Object> regionConfig = configManager.getRegionConfig();
        boolean requireFlag = regionConfig != null && (boolean) regionConfig.getOrDefault("requireBounceFlag", false);
        
        if (requireFlag && !com.neptune.spring.integration.WorldGuardHook.canBounceInRegion(player)) {
            UUID playerId = player.getUniqueId();
            Vector currentVelocity = player.getVelocity();
            previousVelocity.put(playerId, currentVelocity.clone());
            return;
        }
        
        UUID playerId = player.getUniqueId();
        Block blockUnderFeet = player.getLocation().subtract(0, 1, 0).getBlock();
        
        Vector currentVelocity = player.getVelocity();
        Vector prevVelocity = previousVelocity.get(playerId);
        double prevYVel = prevVelocity != null ? prevVelocity.getY() : 0.0;
        
        // Trigger bounce if player was falling last tick (even slowly) and is now on the ground.
        // Original threshold (-0.1) prevented bounces on materials that slow descent (e.g. honey).
        double minLandingYVelocity = -0.02; // Allow gentle landings to still bounce
        boolean justLanded = player.isOnGround() && prevVelocity != null && prevYVel < minLandingYVelocity;
        
        if (!justLanded) {
            previousVelocity.put(playerId, currentVelocity.clone());
            return;
        }
        
        String materialName = blockUnderFeet.getType().name();
        List<Map<String, Object>> materials = configManager.getMaterialsList();
        
        // Find if the material is configured
        String configuredMaterial = null;
        for (Map<String, Object> materialConfig : materials) {
            Object cfg = materialConfig.get("material");
            if (cfg == null) continue;
            String key = com.neptune.spring.config.ConfigManager.normalizeMaterialKey(cfg.toString());
            // Compare using Bukkit Material equality to be robust to case/namespacing
            Material cfgMat = Material.matchMaterial(key);
            if (cfgMat != null && cfgMat == blockUnderFeet.getType()) {
                configuredMaterial = cfgMat.name();
                break;
            }
            // Fallback string compare (normalized)
            if (key.equals(materialName)) {
                configuredMaterial = materialName;
                break;
            }
        }
        
        Vector velocityToStore = currentVelocity.clone();
        
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

            // Compute incoming horizontal magnitude
            double incomingHX = prevVelocity != null ? prevVelocity.getX() : 0.0;
            double incomingHZ = prevVelocity != null ? prevVelocity.getZ() : 0.0;
            double incomingHorizontalMagnitude = Math.sqrt(incomingHX * incomingHX + incomingHZ * incomingHZ);

            double targetHorizontalMagnitude = incomingHorizontalMagnitude * horizontalMultiplier;
            Vector newHorizontal = new Vector(0, 0, 0);

            if (incomingHorizontalMagnitude > 0.001 && targetHorizontalMagnitude > 0.0) {
                Vector direction = new Vector(incomingHX, 0, incomingHZ).normalize();

                if (!anglePreservation) {
                    Vector look = player.getLocation().getDirection();
                    Vector lookHoriz = new Vector(look.getX(), 0, look.getZ());
                    if (lookHoriz.lengthSquared() > 0.001) {
                        direction = lookHoriz.normalize();
                    }
                }

                newHorizontal = direction.multiply(targetHorizontalMagnitude);
            }

            Vector newVelocity = new Vector(newHorizontal.getX(), verticalVelocity, newHorizontal.getZ());
            
            player.setVelocity(newVelocity);
            velocityToStore = newVelocity.clone();
            
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

            // Record bounce in stats store if available
            try {
                if (statsStore != null) {
                    statsStore.incrementBounce(player.getUniqueId(), player.getName());
                }
            } catch (Exception ignored) {}
            
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
        previousVelocity.put(playerId, velocityToStore);
    }
    
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!configManager.isEnabled()) return;
        
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            Block blockUnderFeet = player.getLocation().subtract(0, 1, 0).getBlock();
            String current = blockUnderFeet.getType().name();

            boolean isConfigured = false;
            for (Map<String, Object> materialConfig : configManager.getMaterialsList()) {
                Object cfg = materialConfig.get("material");
                if (cfg == null) continue;
                String key = com.neptune.spring.config.ConfigManager.normalizeMaterialKey(cfg.toString());
                Material cfgMat = Material.matchMaterial(key);
                if ((cfgMat != null && cfgMat == blockUnderFeet.getType()) || key.equals(current)) {
                    isConfigured = true;
                    break;
                }
            }

            if (isConfigured) {
                // Reset chain when sneaking
                chainTracker.resetChain(player);
            }
        }
    }
}
