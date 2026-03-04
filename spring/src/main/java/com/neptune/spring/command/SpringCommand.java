package com.neptune.spring.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import com.neptune.spring.config.ConfigManager;
import java.util.*;

public class SpringCommand implements CommandExecutor, TabCompleter {
    private ConfigManager configManager;
    private Map<UUID, CreationSession> sessions = new HashMap<>();
    
    public SpringCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender, 1);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        page = 1;
                    }
                }
                showHelp(sender, page);
                break;
                
            case "reload":
                if (!sender.hasPermission("spring.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }
                configManager.reloadConfig();
                sender.sendMessage("§aConfiguration reloaded successfully.");
                break;
                
            case "list":
                sender.sendMessage(configManager.getListStatus());
                break;
                
            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be used by players.");
                    return true;
                }
                if (!sender.hasPermission("spring.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /spring create <material|level>");
                    return true;
                }
                handleCreate(sender, args);
                break;
                
            case "edit":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be used by players.");
                    return true;
                }
                if (!sender.hasPermission("spring.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /spring edit <material|level> <name>");
                    return true;
                }
                handleEdit(sender, args);
                break;
                
            case "wizard":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be used by players.");
                    return true;
                }
                if (!sender.hasPermission("spring.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }
                startWizard((Player) sender);
                break;
                
            default:
                sender.sendMessage("§cUnknown command: /spring " + args[0]);
                sender.sendMessage("§7Type §e/spring help §7for a list of commands.");
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender, int page) {
        sender.sendMessage("§6§m                                        ");
        sender.sendMessage("§6§l    Spring Bounce Plugin - Help");
        sender.sendMessage("§6§m                                        ");
        
        if (page == 1) {
            sender.sendMessage("");
            sender.sendMessage("§e/spring help [page] §7- Show this help menu");
            sender.sendMessage("§e/spring list §7- Show all configured materials & levels");
            sender.sendMessage("§e/spring reload §7- Reload configuration §c(admin)");
            sender.sendMessage("");
            sender.sendMessage("§7§oPage 1/2 - Use /spring help 2 for more");
        } else if (page == 2) {
            sender.sendMessage("");
            sender.sendMessage("§6Creation & Editing:");
            sender.sendMessage("§e/spring wizard §7- Interactive setup walkthrough §c(admin)");
            sender.sendMessage("§e/spring create material <BLOCK> <level> §7- Add bounce block §c(admin)");
            sender.sendMessage("§e/spring create level <name> <vertical> <horizontal> §7- Add level §c(admin)");
            sender.sendMessage("§e/spring edit material <BLOCK> §7- Modify material settings §c(admin)");
            sender.sendMessage("§e/spring edit level <name> §7- Modify level settings §c(admin)");
            sender.sendMessage("");
            sender.sendMessage("§7§oPage 2/2 - Use /spring help 1 for basics");
        }
        sender.sendMessage("§6§m                                        ");
    }
    
    private void handleCreate(CommandSender sender, String[] args) {
        String type = args[1].toLowerCase();
        
        if (type.equals("material")) {
            if (args.length < 4) {
                sender.sendMessage("§cUsage: /spring create material <BLOCK_TYPE> <level_name>");
                sender.sendMessage("§7Example: /spring create material DIAMOND_BLOCK springy");
                return;
            }
            
            String blockType = args[2].toUpperCase();
            String levelName = args[3].toLowerCase();
            
            // Validate material
            Material mat = Material.matchMaterial(blockType);
            if (mat == null) {
                sender.sendMessage("§cInvalid block type: " + blockType);
                sender.sendMessage("§7Use a valid Minecraft material name (e.g., DIAMOND_BLOCK, GOLD_BLOCK)");
                return;
            }
            
            // Validate level exists
            if (!configManager.getLevels().containsKey(levelName)) {
                sender.sendMessage("§cLevel '" + levelName + "' does not exist.");
                sender.sendMessage("§7Create it first with: /spring create level " + levelName + " <vertical> <horizontal>");
                return;
            }
            
            sender.sendMessage("§a✓ Material created successfully!");
            sender.sendMessage("§7Add this to your config.json materials array:");
            sender.sendMessage("§e{ \"material\": \"" + mat.name() + "\", \"level\": \"" + levelName + "\" }");
            sender.sendMessage("");
            sender.sendMessage("§7Also add a bounce chain:");
            sender.sendMessage("§e\"" + mat.name() + "\": [\"" + levelName + "\"]");
            sender.sendMessage("");
            sender.sendMessage("§7Then run §e/spring reload §7to apply changes.");
            
        } else if (type.equals("level")) {
            if (args.length < 5) {
                sender.sendMessage("§cUsage: /spring create level <name> <vertical> <horizontal>");
                sender.sendMessage("§7Example: /spring create level mega 3.0 1.5");
                return;
            }
            
            String levelName = args[2].toLowerCase();
            double vertical, horizontal;
            
            try {
                vertical = Double.parseDouble(args[3]);
                horizontal = Double.parseDouble(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid numbers. Use decimals like 1.4 or 2.2");
                return;
            }
            
            sender.sendMessage("§a✓ Level template created!");
            sender.sendMessage("§7Add this to your config.json levels section:");
            sender.sendMessage("§e\"" + levelName + "\": {");
            sender.sendMessage("§e  \"verticalVelocity\": " + vertical + ",");
            sender.sendMessage("§e  \"horizontalMultiplier\": " + horizontal + ",");
            sender.sendMessage("§e  \"anglePreservation\": true,");
            sender.sendMessage("§e  \"sound\": \"minecraft:block.slime_block.place\",");
            sender.sendMessage("§e  \"soundVolume\": 1.0,");
            sender.sendMessage("§e  \"soundPitch\": 1.2,");
            sender.sendMessage("§e  \"particles\": { \"type\": \"minecraft:poof\", \"count\": 10, \"offset\": [0.3, 0.1, 0.3] }");
            sender.sendMessage("§e}");
            sender.sendMessage("");
            sender.sendMessage("§7Then run §e/spring reload §7to apply changes.");
            
        } else {
            sender.sendMessage("§cUsage: /spring create <material|level>");
        }
    }
    
    private void handleEdit(CommandSender sender, String[] args) {
        String type = args[1].toLowerCase();
        
        if (type.equals("material")) {
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /spring edit material <BLOCK_TYPE>");
                return;
            }
            
            String blockType = args[2].toUpperCase();
            Material mat = Material.matchMaterial(blockType);
            if (mat == null) {
                sender.sendMessage("§cInvalid block type: " + blockType);
                return;
            }
            
            // Find in config
            List<Map<String, Object>> materials = configManager.getMaterialsList();
            Map<String, Object> found = null;
            for (Map<String, Object> m : materials) {
                String key = ConfigManager.normalizeMaterialKey(String.valueOf(m.get("material")));
                if (key.equals(mat.name())) {
                    found = m;
                    break;
                }
            }
            
            if (found == null) {
                sender.sendMessage("§cMaterial " + mat.name() + " is not configured.");
                sender.sendMessage("§7Create it with: /spring create material " + mat.name() + " <level>");
                return;
            }
            
            sender.sendMessage("§6Current configuration for " + mat.name() + ":");
            sender.sendMessage("§eInitial Level: §f" + found.get("level"));
            
            Map<String, List<String>> chains = configManager.getBounceChains();
            if (chains.containsKey(mat.name())) {
                sender.sendMessage("§eBounce Chain: §f" + String.join(" → ", chains.get(mat.name())));
            }
            
            sender.sendMessage("");
            sender.sendMessage("§7Edit config.json manually, then run §e/spring reload");
            
        } else if (type.equals("level")) {
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /spring edit level <name>");
                return;
            }
            
            String levelName = args[2].toLowerCase();
            Map<String, Map<String, Object>> levels = configManager.getLevels();
            
            if (!levels.containsKey(levelName)) {
                sender.sendMessage("§cLevel '" + levelName + "' does not exist.");
                sender.sendMessage("§7Create it with: /spring create level " + levelName + " <vertical> <horizontal>");
                return;
            }
            
            Map<String, Object> level = levels.get(levelName);
            sender.sendMessage("§6Current configuration for level '" + levelName + "':");
            sender.sendMessage("§eVertical Velocity: §f" + level.get("verticalVelocity"));
            sender.sendMessage("§eHorizontal Multiplier: §f" + level.get("horizontalMultiplier"));
            sender.sendMessage("§eAngle Preservation: §f" + level.get("anglePreservation"));
            sender.sendMessage("§eSound: §f" + level.get("sound"));
            sender.sendMessage("");
            sender.sendMessage("§7Edit config.json manually, then run §e/spring reload");
            
        } else {
            sender.sendMessage("§cUsage: /spring edit <material|level> <name>");
        }
    }
    
    private void startWizard(Player player) {
        player.sendMessage("§6§m                                        ");
        player.sendMessage("§6§l    Spring Setup Wizard");
        player.sendMessage("§6§m                                        ");
        player.sendMessage("");
        player.sendMessage("§7This wizard will help you create a new bounce block.");
        player.sendMessage("");
        player.sendMessage("§eStep 1: §fChoose a block material");
        player.sendMessage("§7Examples: DIAMOND_BLOCK, GOLD_BLOCK, EMERALD_BLOCK, TUFF");
        player.sendMessage("");
        player.sendMessage("§7Type the material name in chat, or type §ccancel §7to exit.");
        player.sendMessage("§6§m                                        ");
        
        sessions.put(player.getUniqueId(), new CreationSession());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("help", "list", "reload", "create", "edit", "wizard"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("edit")) {
                completions.addAll(Arrays.asList("material", "level"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create") && args[1].equalsIgnoreCase("material")) {
                // Suggest common block types
                completions.addAll(Arrays.asList("DIAMOND_BLOCK", "GOLD_BLOCK", "EMERALD_BLOCK", "IRON_BLOCK", "TUFF"));
            } else if (args[0].equalsIgnoreCase("edit") && args[1].equalsIgnoreCase("level")) {
                completions.addAll(configManager.getLevels().keySet());
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("create") && args[1].equalsIgnoreCase("material")) {
                completions.addAll(configManager.getLevels().keySet());
            }
        }
        
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .sorted()
            .toList();
    }
    
    private static class CreationSession {
        String material;
        String levelName;
        Double vertical;
        Double horizontal;
        int step = 1;
    }
}
