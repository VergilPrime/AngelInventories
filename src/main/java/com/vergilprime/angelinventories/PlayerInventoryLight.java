package com.vergilprime.angelinventories;

import org.bukkit.inventory.ItemStack;

public class PlayerInventoryLight {
    ItemStack[] armor;
    ItemStack[] storage;
    ItemStack offhand;

    public void setArmorContents(ItemStack[] armor) {
        this.armor = armor;
    }

    public void setStorageContents(ItemStack[] storage) {
        this.storage = storage;
    }

    public void setItemInOffHand(ItemStack offhand) {
        this.offhand = offhand;
    }

    public ItemStack[] getArmorContents() {
        return armor;
    }

    public ItemStack[] getStorageContents() {
        return storage;
    }

    public ItemStack getItemInOffHand() {
        return offhand;
    }
}
