package com.neptune.spring.integration;

import org.bukkit.plugin.Plugin;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class WorldGuardHook {
    private final Plugin plugin;
    public static final StateFlag SPRING_BOUNCE_FLAG = new StateFlag("spring-bounce", true);
    
    public WorldGuardHook(Plugin plugin) {
        this.plugin = plugin;
        if (isAvailable()) {
            registerFlag();
        }
    }
    
    public static boolean isAvailable() {
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            return WorldGuard.getInstance() != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private void registerFlag() {
        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            registry.register(SPRING_BOUNCE_FLAG);
            plugin.getLogger().info("Registered WorldGuard flag: spring-bounce");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register WorldGuard flag: " + e.getMessage());
        }
    }
    
    public static boolean canBounceInRegion(org.bukkit.entity.Player player) {
        if (!isAvailable()) return true; // Allow if WorldGuard not present
        
        try {
            // Get WorldGuard plugin instance
            org.bukkit.plugin.Plugin wgPlugin = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard");
            if (wgPlugin instanceof WorldGuardPlugin) {
                WorldGuardPlugin worldGuard = (WorldGuardPlugin) wgPlugin;
                com.sk89q.worldguard.LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
                com.sk89q.worldguard.protection.regions.RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
                
                return query.testState(localPlayer.getLocation(), localPlayer, SPRING_BOUNCE_FLAG);
            }
            return true; // Default to allow if WorldGuard instance not found
        } catch (Exception e) {
            return true; // Default to allow on error
        }
    }
}
