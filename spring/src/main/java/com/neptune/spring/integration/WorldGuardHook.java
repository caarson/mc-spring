package com.neptune.spring.integration;

import org.bukkit.plugin.Plugin;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;

public class WorldGuardHook {
    private static Plugin plugin;
    public static StateFlag SPRING_BOUNCE_FLAG;
    
    /**
     * Must be called during onLoad() for WorldGuard 7.0+
     */
    public static void registerFlags(Plugin pluginInstance) {
        plugin = pluginInstance;
        if (!isAvailable()) {
            return;
        }
        
        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            StateFlag flag = new StateFlag("spring-bounce", true);
            registry.register(flag);
            SPRING_BOUNCE_FLAG = flag;
            plugin.getLogger().info("Registered WorldGuard flag: spring-bounce (default: allow)");
        } catch (FlagConflictException e) {
            // Flag already registered
            SPRING_BOUNCE_FLAG = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("spring-bounce");
            plugin.getLogger().info("WorldGuard flag spring-bounce already exists");
        } catch (IllegalStateException e) {
            plugin.getLogger().severe("Failed to register flag - called too late! Must be in onLoad()");
            plugin.getLogger().severe(e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register WorldGuard flag: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static boolean isAvailable() {
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            return WorldGuard.getInstance() != null;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }
    
    public static boolean canBounceInRegion(org.bukkit.entity.Player player) {
        if (!isAvailable() || SPRING_BOUNCE_FLAG == null) return true;
        
        try {
            org.bukkit.plugin.Plugin wgPlugin = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard");
            if (wgPlugin instanceof WorldGuardPlugin) {
                WorldGuardPlugin worldGuard = (WorldGuardPlugin) wgPlugin;
                com.sk89q.worldguard.LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
                com.sk89q.worldguard.protection.regions.RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
                
                com.sk89q.worldguard.protection.flags.StateFlag.State state = query.queryState(
                    localPlayer.getLocation(),
                    localPlayer,
                    SPRING_BOUNCE_FLAG
                );
                
                // If state is null, use default (allow). Otherwise check if ALLOW
                return state == null || state == com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW;
            }
            return true;
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Error checking WorldGuard region: " + e.getMessage());
            }
            return true; // Default to allow on error
        }
    }
}
