package com.vergilprime.angelinventories;

import com.vergilprime.angelinventories.backend.Database;
import com.vergilprime.angelinventories.backend.SQLite;
import com.vergilprime.angelinventories.commands.AngelInventoriesCommand;
import com.vergilprime.angelinventories.commands.ToggleInventory;
import com.vergilprime.angelinventories.data.CustomInventory;
import com.vergilprime.angelinventories.data.PlayerData;
import com.vergilprime.angelinventories.events.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AngelInventories extends JavaPlugin {

    private Database database;
    private Map<UUID, PlayerData> loadedPlayers = new HashMap<>();
    private Map<String, CustomInventory> customInventories = new HashMap<>();
    private FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        config.addDefault("debugging", false);
        config.addDefault("database", "storage");
        config.addDefault("MaxInventories", 5);
        config.options().copyDefaults(true);
        saveConfig();

        new ToggleInventory(this);
        new AngelInventoriesCommand(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        database = new SQLite(this);
        database.load();

        database.loadCustomInventories();

        // Since nobody should be online at startup this probably will do nothing
        // Leaving it in case someone reloads.
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            database.loadPlayerData(uuid);
        }
    }

    @Override
    public void onDisable() {
        loadedPlayers.forEach((uuid, playerData) -> {
            playerData.saveAll();
        });
    }

    public Database getDatabase() {
        return database;
    }

    public Map<UUID, PlayerData> getLoadedPlayers() {
        return loadedPlayers;
    }

    public Map<String, CustomInventory> getCustomInventories() {
        return customInventories;
    }
}
