package com.vergilprime.angelinventories.commands;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ResetInventory implements CommandExecutor {
    AngelInventories plugin;

    public ResetInventory(AngelInventories plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;
        if (args.length == 1) {
            player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage("That player was not found.");
                return false;
            }
        } else if (args.length == 0 && sender instanceof Player) {
            player = (Player) sender;
        } else {
            return false;
        }

        UUID uuid = player.getUniqueId();
        PlayerData playerData = plugin.loadedPlayers.get(uuid);
        
        if (playerData.GetCurrentCustomInv() != null) {
            playerData.ToggleInventory(true);
            sender.sendMessage(player.getDisplayName() + "'s custom inventory swapped to their player inventory.");
        } else {
            sender.sendMessage("That player doesn't currently have a Custom Inventory set.");
        }
        return true;
    }
}
