package com.vergilprime.angelinventories.data;

import com.vergilprime.angelinventories.AngelInventories;

import java.util.List;

public class CustomInventory {

    private String name;
    private PlayerInventoryLight inventory;
    private CustomInventorySetting setting;
    private List<Integer> lockedSlots;

    public CustomInventory(String name, PlayerInventoryLight inventory, CustomInventorySetting setting, List<Integer> lockedSlots) {
        this.name = name.toLowerCase();
        this.inventory = inventory;
        this.setting = setting;
        this.lockedSlots = lockedSlots;
    }

    public String getName() {
        return name;
    }

    public CustomInventorySetting getSetting() {
        return setting;
    }

    public void setSetting(CustomInventorySetting setting) {
        this.setting = setting;
    }

    public PlayerInventoryLight getInventory() {
        return inventory;
    }

    public void setInventory(PlayerInventoryLight inventory) {
        this.inventory = inventory;
    }

    public List<Integer> getLockedSlots() {
        return lockedSlots;
    }

    public void setLockedSlots(List<Integer> lockedSlots) {
        this.lockedSlots = lockedSlots;
    }

    public void save() {
        AngelInventories.getInstance().setCustomInventory(name, this);
        AngelInventories.getInstance().getDatabase().saveCustomInventory(this);
    }
}
