package com.vergilprime.angelinventories.commands;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.data.PlayerData;
import com.vergilprime.angelinventories.util.Chat;
import com.vergilprime.angelinventories.util.Tab;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

public class RestoreInventory extends Command {

    public RestoreInventory(AngelInventories plugin) {
        super("RestoreInventory", plugin);
    }

    /**
     * /RestoreInventory <playerName>
     */
    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        Player target = null;
        if (sender instanceof Player) {
            target = (Player) sender;
        }
        if (args.length > 0) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                Chat.error(sender, "Unknown player {0}", args[0]);
                return;
            }
        }
        if (target == null) {
            Chat.error(sender, "Please use {0}.", "/" + label + " <PlayerName>");
            return;
        }
        PlayerData data = plugin.getLoadedPlayers().get(target.getUniqueId());

        if (data.getCurrentCustomInv() != null) {
            data.restore();
            Chat.main(sender, "Restored {0}'s inventory back from custom.", target.getName());
        } else {
            Chat.error(sender, "{0} does not have an active custom inventory.");
        }
    }

    @Override
    public Stream<String> onTab(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Tab.getPlayerList();
        }
        return Stream.empty();
    }
}
