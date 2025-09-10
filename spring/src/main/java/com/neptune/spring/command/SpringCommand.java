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
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            // Handle default usage: show help
            sender.sendMessage("Usage: /spring [reload|list]");
            return true;
        }
        
        switch (args[0]) {
            case "reload":
                configManager.reloadConfig();
                sender.sendMessage("Configuration reloaded successfully.");
                break;
            case "list":
                sender.sendMessage(configManager.getListStatus());
                break;
            default:
                sender.sendMessage("Unknown command: /spring " + args[0]);
        }
        
        return true;
    }
}
