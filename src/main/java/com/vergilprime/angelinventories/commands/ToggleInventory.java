package com.vergilprime.angelinventories.commands;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ToggleInventory implements CommandExecutor {
    AngelInventories plugin;

    public ToggleInventory(AngelInventories plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            UUID uuid = player.getUniqueId();

            PlayerData playerData = plugin.loadedPlayers.get(uuid);

            int max = playerData.GetMaxInventories();

            try {
                int index = playerData.GetCurrentPinvIndex();
                if (args.length == 0) {
                    index++;
                    if (index > max - 1) {
                        index = 0;
                    }
                } else if (args.length == 1) {
                    index = Integer.parseInt(args[0]) - 1;
                }

                if (index > 0) {
                    if (max > 1) {
                        if (index > max) {
                            int result = playerData.ToggleInventory(index, false);
                            switch (result) {
                                case 0:
                                    player.sendMessage("Inventory changed to " + (index + 1));
                                    break;
                                case 1:
                                    player.sendMessage("You can't change inventories right now.");
                                    break;
                            }
                        }
                    } else {
                        sender.sendMessage("You only have access to one inventory.");
                        return false;
                    }
                }


            } catch (NumberFormatException ex) {
                sender.sendMessage("The only argument should be the inventory you want to switch to!");
                return false;
            }
            return true;
        } else {
            plugin.getLogger().severe("Only players can use this command.");
            return false;
        }
    }
}
