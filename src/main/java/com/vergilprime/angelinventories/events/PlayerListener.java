package com.vergilprime.angelinventories.events;

import com.vergilprime.angelinventories.AngelInventories;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {
    private final AngelInventories plugin;

    public PlayerListener(AngelInventories plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        plugin.sqlite.loadPlayerData(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        OfflinePlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        plugin.loadedPlayers.remove(uuid);
        //TODO: Save player's inventory here
    }
}
