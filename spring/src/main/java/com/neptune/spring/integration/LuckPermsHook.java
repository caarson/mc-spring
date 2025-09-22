package com.neptune.spring.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;

public class LuckPermsHook {
    private final Plugin plugin;
    
    public LuckPermsHook(Plugin plugin) {
        this.plugin = plugin;
        // Check if LuckPerms is present, then register permissions
    }
    
    public static boolean isAvailable() {
        return Bukkit.getServicesManager().getRegistration(LuckPerms.class) != null;
    }
    
    public boolean hasPermission(Player player, String permission) {
        if (isAvailable()) {
            LuckPerms api = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
            }
        }
        // Fallback to Bukkit permissions
        return player.hasPermission(permission);
    }
}
