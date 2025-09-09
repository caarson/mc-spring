package com.neptune.spring.integration;

import org.bukkit.plugin.Plugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.Context;
import net.luckperms.api.user.User;

public class LuckPermsHook {
    private final Plugin plugin;
    
    public LuckPermsHook(Plugin plugin) {
        this.plugin = plugin;
        // Check if LuckPerms is present, then register permissions
    }
    
    public boolean hasPermission(Player player, String permission) {
        if (LuckPerms.getInstance() != null) {
            User user = LuckPerms.getApi().getUser(player);
            return user.hasPermission(Context.of(), permission);
        } else {
            // Fallback to Bukkit permissions
            return player.hasPermission(permission);
        }
    }
}
