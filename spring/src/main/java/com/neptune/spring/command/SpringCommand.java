package com.neptune.spring.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import com.neptune.spring.config.ConfigManager;

public class SpringCommand implements CommandExecutor {
    private ConfigManager configManager;
    
    public SpringCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length == 0) {
            // Show help when no arguments provided
            sender.sendMessage("§6=== Spring Bounce Pad Plugin ===");
            sender.sendMessage("§e/spring reload §7- Reload the configuration");
            sender.sendMessage("§e/spring list §7- Show current plugin status");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                configManager.reloadConfig();
                sender.sendMessage("§aConfiguration reloaded successfully.");
                break;
            case "list":
                sender.sendMessage(configManager.getListStatus());
                break;
            default:
                sender.sendMessage("§cUnknown command: /spring " + args[0]);
                sender.sendMessage("§cUsage: /spring [reload|list]");
        }
        
        return true;
    }
}
