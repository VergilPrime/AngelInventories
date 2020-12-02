package com.vergilprime.angelinventories;

import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;

public class CustomInventory {
    private PlayerInventory inventory;
    private CustomInventorySetting setting;
    private ArrayList<Integer> lockedSlots;

    public CustomInventory(PlayerInventory inventory, CustomInventorySetting setting, ArrayList<Integer> lockedSlots) {
        this.inventory = inventory;
        this.setting = setting;
        this.lockedSlots = lockedSlots;
    }

    public CustomInventorySetting getSetting() {
        return setting;
    }

    public PlayerInventory getInventory() {
        return inventory;
    }
}
