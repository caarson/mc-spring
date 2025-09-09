package com.neptune.spring.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SpringCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return false; // Empty stub for now
    }
}
