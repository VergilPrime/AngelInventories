package com.vergilprime.angelinventories.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PlayerInventoryLight {

    private ItemStack[] armor;
    private ItemStack[] storage;
    private ItemStack offhand;

    public PlayerInventoryLight() {
        armor = generateEmpty(4);
        storage = generateEmpty(36);
        offhand = new ItemStack(Material.AIR);
    }

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

    public static ItemStack[] generateEmpty(int size) {
        ItemStack[] stacks = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            stacks[i] = new ItemStack(Material.AIR);
        }
        return stacks;
    }
}
