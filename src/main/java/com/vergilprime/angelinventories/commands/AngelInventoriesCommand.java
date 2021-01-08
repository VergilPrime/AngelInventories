package com.vergilprime.angelinventories.commands;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.data.CustomInventory;
import com.vergilprime.angelinventories.data.CustomInventorySetting;
import com.vergilprime.angelinventories.data.PlayerData;
import com.vergilprime.angelinventories.data.PlayerInventoryLight;
import com.vergilprime.angelinventories.util.Chat;
import com.vergilprime.angelinventories.util.Tab;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.stream.Stream;

public class AngelInventoriesCommand extends Command {

    public AngelInventoriesCommand(AngelInventories plugin) {
        super("AngelInventories", plugin);
    }

    /**
     * /ai set <inventory> [player]
     * /ai save <name> [setting]
     */

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            Chat.main(sender, "Available sub commands:");
            Chat.main(sender, " * {0}", "set <inventory-name> [player]");
            Chat.main(sender, " * {0}", "save <inventory-name> [setting]");
            Chat.main(sender, " * {0}", "setting <inventory-name> <setting>");
            return;


        } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 2) {
                Chat.error(sender, "Please use {0}", "set <inventory-name> [player]");
                return;
            }
            Player target = null;
            if (sender instanceof Player) {
                target = (Player) sender;
            }
            if (args.length > 2) {
                target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    Chat.error(sender, "Unknown player {0}", args[0]);
                    return;
                }
            }
            if (target == null) {
                Chat.error(sender, "Please use {0}", "set <inventory-name> <player>");
                return;
            }
            PlayerData data = plugin.getLoadedPlayers().get(target.getUniqueId());
            String name = args[1];
            if (data.setCustomInventory(name)) {
                Chat.main(sender, "Set {0}'s inventory to {1}", target.getName(), name);
            } else {
                Chat.error(sender, "Unknown custom inventory {0}", name);
            }


        } else if (args[0].equalsIgnoreCase("save")) {
            if (args.length < 2) {
                Chat.error(sender, "Please use {0}", "save <inventory-name> [setting]");
                return;
            }
            if (!(sender instanceof Player)) {
                Chat.error(sender, "You need to be a player to save your inventory.");
                return;
            }
            String name = args[1];
            PlayerInventory inventory = ((Player) sender).getInventory();
            CustomInventorySetting setting = CustomInventorySetting.normal;
            if (args.length > 2) {
                try {
                    setting = CustomInventorySetting.valueOf(args[2].toLowerCase());
                } catch (IllegalArgumentException ex) {
                    Chat.error(sender, "Unknown setting type {0}, use one of\n{1}", args[2], Arrays.toString(CustomInventorySetting.values()));
                    return;
                }
            }
            plugin.getDatabase().setCustomInventory(name, new PlayerInventoryLight(inventory), setting);
            Chat.main(sender, "Saved inventory {0}", name);


        } else if (args[0].equalsIgnoreCase("setting")) {
            if (args.length < 3) {
                Chat.error(sender, "Please use {0}", "save <inventory-name> <setting>");
                return;
            }
            String name = args[1];
            CustomInventory inv = AngelInventories.getInstance().getCustomInventory(name);
            if (inv == null) {
                Chat.error(sender, "Unknown custom inventory {0}", name);
                return;
            }
            CustomInventorySetting setting;
            try {
                setting = CustomInventorySetting.valueOf(args[2].toLowerCase());
            } catch (IllegalArgumentException ex) {
                Chat.error(sender, "Unknown setting type {0}, use one of\n{1}", args[2], Arrays.toString(CustomInventorySetting.values()));
                return;
            }
            plugin.getDatabase().setCustomInventory(name, inv.getInventory(), setting);
            Chat.main(sender, "Updated setting for inventory {0} to {1}", name, setting);


        } else {
            Chat.error(sender, "Unknown sub command {0}", args[0]);
        }
    }


    @Override
    public Stream<String> onTab(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Stream.of("set", "save", "setting");
        }
        if (args.length == 2) {
            return plugin.getCustomInventories().stream();
        }
        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "set":
                    return Tab.getPlayerList();
                case "save":
                case "setting":
                    return Stream.of(CustomInventorySetting.values()).map(Enum::name);
                default:
                    return Stream.empty();
            }
        }
        return Stream.empty();
    }
}
