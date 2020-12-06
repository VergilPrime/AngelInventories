package com.vergilprime.angelinventories;

import com.vergilprime.angelinventories.events.PlayerListener;
import com.vergilprime.angelinventories.sqlite.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class AngelInventories extends JavaPlugin {
    public SQLite sqlite;
    public HashMap<UUID, PlayerData> loadedPlayers = new HashMap<>();
    public HashMap<String, CustomInventory> customInventories;
    public FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        config.addDefault("debugging", false);
        config.addDefault("database", "storage");
        config.addDefault("MaxInventories", 5);
        config.options().copyDefaults(true);
        saveConfig();

        getCommand("ToggleInventory").setExecutor(new com.vergilprime.angelinventories.commands.ToggleInventory(this));
        getCommand("AngelInventories").setExecutor(new com.vergilprime.angelinventories.commands.AngelInventoriesCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        sqlite = new SQLite(this);
        sqlite.load();
        
        sqlite.loadCustomInventories();

        // Since nobody should be online at startup this probably will do nothing
        // Leaving it in case someone reloads.
        UUID uuid;
        for (Player player : Bukkit.getOnlinePlayers()) {
            uuid = player.getUniqueId();
            sqlite.loadPlayerData(uuid);
        }
    }

    @Override
    public void onDisable() {
        loadedPlayers.forEach((uuid, playerData) -> {
            playerData.Save();
        });
        //TODO: Save all loaded players
    }
}
