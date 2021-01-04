package com.vergilprime.angelinventories.commands;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.data.PlayerData;
import com.vergilprime.angelinventories.util.Chat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.Stream;

public class ToggleInventory extends Command {

    public ToggleInventory(AngelInventories plugin) {
        super("ToggleInventory", plugin);
    }

    /**
     * /ti [inventory index]
     */
    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Chat.error(sender, "Only players can use this command.");
            return;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        PlayerData playerData = plugin.getLoadedPlayers().get(uuid);

        int max = playerData.getMaxInventories();

        if (max <= 1) {
            Chat.error(sender, "You only have access to {0} inventory.", 1);
            return;
        }

        int index = (playerData.getCurrentPlayerInvIndex() + 1) % max;
        if (args.length > 0) {
            try {
                index = Integer.parseInt(args[0]) - 1;
                if (index < 0 || index >= max) {
                    Chat.error(sender, "Specify an inventory between {0} and {1}.", 1, max);
                    return;
                }
            } catch (NumberFormatException e) {
                Chat.error(sender, "Specify an inventory between {0} and {1}.", 1, max);
                return;
            }
        }

        if (playerData.switchToInventory(index, false)) {
            Chat.error(sender, "Switched inventory to {0}", index + 1);
        } else {
            Chat.error(sender, "You can't change inventories right now.");
        }

        return;
    }

    @Override
    public Stream<String> onTab(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Stream.empty();
        }
        if (args.length == 1) {
            Player player = (Player) sender;
            PlayerData data = plugin.getLoadedPlayers().get(player.getUniqueId());
            int max = data.getMaxInventories();
            String[] nums = new String[max];
            for (int i = 1; i <= max; i++) {
                nums[i] = i + "";
            }
            return Stream.of(nums);
        }
        return Stream.empty();
    }
}
