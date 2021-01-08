package com.vergilprime.angelinventories.data;

import java.util.List;

public class CustomInventory {

    private PlayerInventoryLight inventory;
    private CustomInventorySetting setting;
    private List<Integer> lockedSlots;

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

    public List<Integer> getLockedSlots() {
        return lockedSlots;
    }
}
