package com.vergilprime.angelinventories.data;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PlayerData {

    private final AngelInventories plugin;
    private final UUID uuid;
    private final List<PlayerInventoryLight> inventories;
    private int playerInvIndex;
    private String customInvName;

    public PlayerData(AngelInventories plugin, UUID uuid, Integer current_pinv_index, String current_custom_inv, List<PlayerInventoryLight> playerInventories) {
        this.plugin = plugin;
        this.uuid = uuid;
        playerInvIndex = current_pinv_index;
        customInvName = current_custom_inv;
        inventories = playerInventories;
    }

    /**
     * @return Returns true if successful, false if failed or locked (override == false)
     */
    public boolean restore() {
        return switchToInventory(playerInvIndex, true);
    }

    /**
     * @param index    determines which inventory to switch to
     * @param override determines whether it works despite the player having a locked custom inventory on.
     * @return Returns true if successful, false if failed or locked (override == false)
     */
    public boolean switchToInventory(int index, boolean override) {
        // If current CustomInv is locked and override is false
        if (!override && customInvName != null) {
            CustomInventory customInventory = plugin.getCustomInventory(customInvName);
            if (customInventory.getSetting() == CustomInventorySetting.locked) {
                return false;
            }
        }
        while (index >= inventories.size()) {
            PlayerInventoryLight newInventory = new PlayerInventoryLight();
            inventories.add(newInventory);
        }
        PlayerInventoryLight newInventory = inventories.get(index);
        if (newInventory != null) {
            if (customInvName == null) {
                saveInventories();
            }
            customInvName = null;
            playerInvIndex = index;
            newInventory.apply(Bukkit.getPlayer(uuid).getInventory());
            savePointers();
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param name Custom inventory name
     * @return Returns true if success, false on fail
     */
    public boolean setCustomInventory(String name) {
        // Look for custom inventory at that name
        CustomInventory customInventory = plugin.getCustomInventory(name);

        if (customInventory == null) {
            return false;
        }

        // If true, replace and overwrite player's inventory
        // else, save players current inventory before loading the custom one
        boolean replace = customInventory.getSetting() == CustomInventorySetting.replace;

        if (replace) {
            customInventory.getInventory().apply(Bukkit.getPlayer(uuid).getInventory());
            saveInventories();
        } else {
            saveInventories();
            customInventory.getInventory().apply(Bukkit.getPlayer(uuid).getInventory());
            customInvName = name;
            savePointers();
        }
        return true;

    }

    public int getMaxInventories() {
        int max = Config.getMaxInventories();
        Player player = Bukkit.getPlayer(uuid);
        while (max > 1 && !player.hasPermission("AngelInventories.Inventories." + max)) {
            max--;
        }

        return Math.max(max, 1);
    }

    public void saveInventories() {
        if (customInvName == null) {
            inventories.get(playerInvIndex).loadFrom(Bukkit.getPlayer(uuid).getInventory());
        }
        plugin.getDatabase().savePlayerInventories(this);
    }

    public void savePointers() {
        plugin.getDatabase().savePlayerPointers(this);
    }

    public void saveAll() {
        saveInventories();
        savePointers();
    }

    public List<PlayerInventoryLight> getInventories() {
        return inventories;
    }

    public Integer getCurrentPlayerInvIndex() {
        return playerInvIndex;
    }

    public String getCurrentCustomInv() {
        return customInvName;
    }

    public UUID getUUID() {
        return uuid;
    }
}
