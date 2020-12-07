package com.vergilprime.angelinventories.commands;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.CustomInventorySetting;
import com.vergilprime.angelinventories.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class AngelInventoriesCommand implements CommandExecutor {
    public final AngelInventories plugin;

    public AngelInventoriesCommand(AngelInventories plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2 || args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "save":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("You must be a player to save your inventory.");
                        return false;
                    }
                    String name = args[1];
                    PlayerInventory inventory = ((Player) sender).getInventory();
                    CustomInventorySetting setting;
                    if (args.length == 3) {
                        try {
                            setting = CustomInventorySetting.valueOf(args[2].toLowerCase());
                        } catch (IllegalArgumentException ex) {
                            sender.sendMessage("Setting must be one of normal, locked, or replace");
                            return false;
                        }
                    } else {
                        setting = CustomInventorySetting.normal;
                    }
                    plugin.sqlite.setCustomInventory(name, inventory, setting);
                    sender.sendMessage("Inventory saved!");
                    return true;
                case "set":
                    Player player;
                    switch (args.length) {
                        case 2:
                            if (!(sender instanceof Player)) {
                                sender.sendMessage("You must specify a player to set their inventory.");
                                return false;
                            }
                            player = (Player) sender;

                            break;
                        case 3:
                            player = Bukkit.getPlayer(args[1]);

                            break;
                        default:
                            return true;
                    }
                    if (player != null) {
                        PlayerData playerData = plugin.loadedPlayers.get(player.getUniqueId());
                        int result = playerData.SetCustomInventory(args[2]);
                        if (result == 0) {
                            sender.sendMessage(player.getDisplayName() + "'s inventory set.");
                        } else {
                            sender.sendMessage("That inventory doesn't exist.");
                        }
                    } else {
                        sender.sendMessage("No player found.");
                    }
                    return true;
                default:
                    return false;
            }
        } else {
            sender.sendMessage("Usage: AngelInventories <Save|Set> [Player] <inventoryName>");
            return false;
        }
    }
}
