package com.vergilprime.angelinventories;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerData {
    private final AngelInventories plugin;
    private final UUID uuid;
    private final Player player;
    private final ArrayList<PlayerInventoryLight> inventories;
    private int playerInvIndex;
    private String customInvName;

    public PlayerData(AngelInventories plugin, UUID uuid, Integer current_pinv_index, String current_custom_inv, ArrayList<PlayerInventoryLight> playerInventories) {
        this.plugin = plugin;
        this.uuid = uuid;
        player = Bukkit.getPlayer(uuid);
        playerInvIndex = current_pinv_index;
        customInvName = current_custom_inv;
        inventories = playerInventories;
    }

    public int ToggleInventory(boolean override) {
        return ToggleInventory(true, override);
    }

    // Forward determines direction of cycling through inventories
    // Override determines whether it works despite the player having a locked custom inventory on.
    public int ToggleInventory(boolean forward, boolean override) {
        int newslot = playerInvIndex;
        if (forward) {
            newslot++;
            // if newslot is beyond the player's last available inventory
            if (newslot > GetMaxInventories() - 1) {
                // switch to the first inventory
                newslot = 0;
            }
        } else {
            newslot--;
            // if newslot is negative
            if (newslot < 0) {
                // switch to the last inventory
                newslot = GetMaxInventories() - 1;
            }
        }
        return ToggleInventory(newslot, override);
    }

    // Index determines which inventory to switch to
    // Override determines whether it works despite the player having a locked custom inventory on.
    public int ToggleInventory(int index, boolean override) {
        if (index >= inventories.size()) {
            //TODO: Doesn't like to cast this
            PlayerInventoryLight newInventory = new PlayerInventoryLight();
            inventories.add(newInventory);
        }
        // If current CustomInv is locked and override is false
        if (!override && customInvName != null) {
            CustomInventory customInventory = plugin.customInventories.get(customInvName);
            if (customInventory.getSetting() == CustomInventorySetting.locked) {
                return 1;
            }
        }
        PlayerInventoryLight newInventory = inventories.get(index);
        if (newInventory != null) {
            SaveInventories();
            playerInvIndex = index;
            SetInventory(player.getInventory(), newInventory);
            SavePointers();
            return 0;
        } else {
            return 1;
        }
    }

    public int SetCustomInventory(String name) {
        // Look for custom inventory at that name
        CustomInventory customInventory = plugin.customInventories.get(name);

        // Replace is determined by the CustomInventory's setting object
        boolean replace;

        // If a custom inventory exists with that name
        if (customInventory != null) {
            // Set Replace based on the CustomInventorySetting's value
            if (customInventory.getSetting() == CustomInventorySetting.replace) {
                replace = true;
            } else {
                replace = false;
            }

            // If not replacing the inventory we should save now so we can restore later
            if (!replace) {
                SaveInventories();
            }

            // New inventory is takeon out of customInventory object
            PlayerInventoryLight newInventory = customInventory.getInventory();

            // Replace player's inventory with new inventory
            SetInventory(player.getInventory(), newInventory);

            // If not replacing inventory we must set customInvName so that we don't save over player's inventory later
            if (!replace) {
                customInvName = name;
                SavePointers();
                // if replacing inventory, save the new inventory and don't set customInvName.
            } else {
                SaveInventories();

            }

            return 0;
        } else {
            return 1;
        }
    }

    public Integer GetMaxInventories() {
        int max = plugin.config.getInt("MaxInventories");

        while (max > 1 && !player.hasPermission("AngelInventories.Inventories." + max)) {
            max--;
        }

        return max;
    }

    public static void SetInventory(PlayerInventory inventory1, PlayerInventory inventory2) {
        inventory1.setArmorContents(inventory2.getArmorContents());
        inventory1.setItemInOffHand(inventory2.getItemInOffHand());
        inventory1.setExtraContents(inventory2.getExtraContents());
        inventory1.setStorageContents(inventory2.getStorageContents());
    }

    public static void SetInventory(PlayerInventoryLight inventory1, PlayerInventory inventory2) {
        inventory1.setArmorContents(inventory2.getArmorContents());
        inventory1.setItemInOffHand(inventory2.getItemInOffHand());
        inventory1.setExtraContents(inventory2.getExtraContents());
        inventory1.setStorageContents(inventory2.getStorageContents());
    }

    public static void SetInventory(PlayerInventory inventory1, PlayerInventoryLight inventory2) {
        inventory1.setArmorContents(inventory2.getArmorContents());
        inventory1.setItemInOffHand(inventory2.getItemInOffHand());
        inventory1.setExtraContents(inventory2.getExtraContents());
        inventory1.setStorageContents(inventory2.getStorageContents());
    }

    public static void SetInventory(PlayerInventoryLight inventory1, PlayerInventoryLight inventory2) {
        inventory1.setArmorContents(inventory2.getArmorContents());
        inventory1.setItemInOffHand(inventory2.getItemInOffHand());
        inventory1.setExtraContents(inventory2.getExtraContents());
        inventory1.setStorageContents(inventory2.getStorageContents());
    }

    public void SaveInventories() {
        if (customInvName == null) {
            SetInventory(inventories.get(playerInvIndex), player.getInventory());
        }
        plugin.sqlite.SavePlayerPointers(uuid);
    }

    public void SavePointers() {
        int result = plugin.sqlite.SavePlayerPointers(uuid);
        switch (result) {
            case 0:
                break;
            case 1:
                plugin.getLogger().severe("Player data could not be saved because it was not loaded!");
                break;
            case 2:
                plugin.getLogger().severe("Player data could not be saved because of an SQL Exception.");
                break;
        }
    }

    public ArrayList<PlayerInventoryLight> GetInventories() {
        return inventories;
    }

    public Integer GetCurrentPinvIndex() {
        return playerInvIndex;
    }

    public String GetCurrentCustomInv() {
        return customInvName;
    }

    public void Save() {
        SaveInventories();
        SavePointers();
    }
}
