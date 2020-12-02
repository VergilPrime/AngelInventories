package com.vergilprime.angelinventories;

import com.vergilprime.angelinventories.sqlite.SQLite;
import org.bukkit.configuration.file.FileConfiguration;
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
        config.addDefault("tablename", "AngelInventories");
        config.addDefault("username", "username");
        config.addDefault("password", "password");
        config.options().copyDefaults(true);
        saveConfig();
        getCommand("ToggleInventory").setExecutor(new com.vergilprime.angelinventories.commands.ToggleInventory(this));
        getCommand("AngelInventories").setExecutor(new com.vergilprime.angelinventories.commands.AngelInventoriesCommand(this));
        sqlite = new SQLite(this);
        sqlite.load();
    }

    @Override
    public void onDisable() {
        //TODO: Save all open chests        
    }
}
