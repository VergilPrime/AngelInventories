package com.vergilprime.angelinventories;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            System.out.println("Only players can open inventories...");
            return false;
        }

        Player player = (Player) sender;
        if (player.hasPermission("AngelInventories.ChestCommand")) {
            player.openInventory(player.getEnderChest());
        } else {
            player.sendMessage("You must vote in order to open your Enderchest anywhere. Open the menu blook with crouch + swap hands (shift + f) and click Vote Links.");
        }
        return true;
    }
}
