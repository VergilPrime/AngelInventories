package com.vergilprime.angelinventories.commands;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.util.Tab;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Command implements CommandExecutor, TabCompleter {

    AngelInventories plugin;

    public Command(String name, AngelInventories plugin) {
        this.plugin = plugin;
        PluginCommand cmd = Bukkit.getPluginCommand(name);
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            onCommand(sender, label, args);
        });
        return true;
    }

    public abstract void onCommand(CommandSender sender, String label, String[] args);

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 0) {
            args = new String[]{""};
        }
        Stream<String> out = onTab(sender, alias, args);
        if (out == null) {
            out = Tab.getPlayerList();
        }
        return out.filter(Tab.testPrefix(args))
                .sorted()
                .limit(Tab.maxTab)
                .collect(Collectors.toList());
    }

    public abstract Stream<String> onTab(CommandSender sender, String alias, String[] args);
}
