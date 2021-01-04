package com.vergilprime.angelinventories.data;

import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class CustomInventory {

    private PlayerInventoryLight inventory = new PlayerInventoryLight();
    private CustomInventorySetting setting;
    private List<Integer> lockedSlots;

    public CustomInventory(PlayerInventory inventory, CustomInventorySetting setting, List<Integer> lockedSlots) {
        PlayerData.setInventory(this.inventory, inventory);
        this.setting = setting;
        this.lockedSlots = lockedSlots;
    }

    public CustomInventory(PlayerInventoryLight inventory, CustomInventorySetting setting, List<Integer> lockedSlots) {
        this.inventory = inventory;
        this.setting = setting;
        this.lockedSlots = lockedSlots;
    }

    public CustomInventorySetting getSetting() {
        return setting;
    }

    public PlayerInventoryLight getInventory() {
        return inventory;
    }
}
