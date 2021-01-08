package com.vergilprime.angelinventories;

import com.vergilprime.angelinventories.backend.Database;
import com.vergilprime.angelinventories.backend.SQLite;
import com.vergilprime.angelinventories.commands.AngelInventoriesCommand;
import com.vergilprime.angelinventories.commands.RestoreInventory;
import com.vergilprime.angelinventories.commands.ToggleInventory;
import com.vergilprime.angelinventories.data.CustomInventory;
import com.vergilprime.angelinventories.data.PlayerData;
import com.vergilprime.angelinventories.events.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class AngelInventories extends JavaPlugin {

    private Database database;
    private Map<UUID, PlayerData> loadedPlayers = new HashMap<>();
    private Map<String, CustomInventory> customInventories = new HashMap<>();
    private FileConfiguration config = getConfig();

    private static AngelInventories instance;
    private static boolean disabling;

    @Override
    public void onEnable() {
        AngelInventories.instance = this;
        Config.saveDefaults();

        new ToggleInventory(this);
        new AngelInventoriesCommand(this);
        new RestoreInventory(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        database = new SQLite(this);
        database.load();

        database.loadCustomInventories(true);

        // Normally player data is loaded on player join
        // This is in case of the plugin starting up after a reload
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            database.loadPlayerData(uuid, true);
        }
        getLogger().info("Loaded player data for " + Bukkit.getOnlinePlayers().size() + " online players.");
    }

    @Override
    public void onDisable() {
        disabling = true;
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

    public CustomInventory getCustomInventory(String name) {
        return customInventories.get(name.toLowerCase());
    }

    public void setCustomInventory(String name, CustomInventory inv) {
        customInventories.put(name, inv);
    }

    public Set<String> getCustomInventories() {
        return Collections.unmodifiableSet(customInventories.keySet());
    }

    public static AngelInventories getInstance() {
        return instance;
    }

    public static boolean isDisabling() {
        return disabling;
    }
}
