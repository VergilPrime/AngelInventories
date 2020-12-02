package com.vergilprime.angelinventories;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerData {
    private final AngelInventories plugin;
    private final UUID uuid;
    private final Player player;
    private final ArrayList<PlayerInventory> inventories;
    private int pinv_index;
    private String custom_inv;

    public PlayerData(AngelInventories plugin, UUID uuid, Integer current_pinv_index, String current_custom_inv, ArrayList<PlayerInventory> playerInventories) {
        this.plugin = plugin;
        this.uuid = uuid;
        player = Bukkit.getPlayer(uuid);
        pinv_index = current_pinv_index;
        custom_inv = current_custom_inv;
        inventories = playerInventories;
    }

    public int ToggleInventory(boolean override) {
        return ToggleInventory(true, override);
    }

    public int ToggleInventory(boolean forward, boolean override) {
        int newslot = pinv_index;
        if (forward) {
            newslot++;
            if (newslot > GetMaxInventories() - 1) {
                newslot = 0;
            }
        } else {
            newslot--;
            if (newslot < 0) {
                newslot = GetMaxInventories() - 1;
            }
        }
        return ToggleInventory(newslot, override);
    }

    public int ToggleInventory(int index, boolean override) {
        if (index >= inventories.size()) {
            inventories.add((PlayerInventory) Bukkit.createInventory(null, InventoryType.PLAYER));
        }
        // If current CustomInv is locked and override is false
        if (!override && !custom_inv.isEmpty()) {
            CustomInventory customInventory = plugin.customInventories.get(custom_inv);
            if (customInventory.getSetting() == CustomInventorySetting.locked) {
                return 1;
            }
        }
        int currentIndex = pinv_index;
        SetInventory(inventories.get(pinv_index), player.getInventory());
        SetInventory(player.getInventory(), inventories.get(index));
        pinv_index = index;
        Save();
        return 0;
    }

    public int SetCustomInventory(String name) {
        CustomInventory customInventory = plugin.customInventories.get(name);
        switch (customInventory.getSetting()) {
            case normal:
            case locked:
                int currentIndex = pinv_index;
                //TODO: Save the player's current inventory at the current index;
                SetInventory(inventories.get(pinv_index), player.getInventory());
                SetInventory(player.getInventory(), customInventory.getInventory());
                custom_inv = name;
                Save();
                break;
        }

        PlayerInventory newInventory = customInventory.getInventory();

        SetInventory(player.getInventory(), newInventory);
        return 0;
    }

    public Integer GetMaxInventories() {
        //TODO: config this
        int max = plugin.config.getInt("MaxInventories");

        while (max > 1 && !player.hasPermission("AngelInventories.Inventories." + max)) {
            max--;
        }

        return max;
    }

    public void SetInventory(PlayerInventory inventory1, PlayerInventory inventory2) {
        inventory1.setArmorContents(inventory2.getArmorContents());
        inventory1.setItemInOffHand(inventory2.getItemInOffHand());
        inventory1.setExtraContents(inventory2.getExtraContents());
        inventory1.setStorageContents(inventory2.getStorageContents());
    }

    public void Save() {
        SetInventory(inventories.get(pinv_index), player.getInventory());
        plugin.sqlite.savePlayerData(uuid);
    }

    public ArrayList<PlayerInventory> GetInventories() {
        return inventories;
    }

    public Integer GetCurrentPinvIndex() {
        return pinv_index;
    }

    public String GetCurrentCustomInv() {
        return custom_inv;
    }
}
