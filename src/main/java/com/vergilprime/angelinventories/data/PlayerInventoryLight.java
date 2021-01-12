package com.vergilprime.angelinventories.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerInventoryLight {

    private ItemStack[] armor;
    private ItemStack[] storage;
    private ItemStack offhand;

    public PlayerInventoryLight() {
        armor = generateEmpty(4);
        storage = generateEmpty(36);
        offhand = new ItemStack(Material.AIR);
    }

    public PlayerInventoryLight(PlayerInventory inventory) {
        loadFrom(inventory);
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

    public void loadFrom(PlayerInventory player) {
        setArmorContents(player.getArmorContents());
        setItemInOffHand(player.getItemInOffHand());
        setStorageContents(player.getStorageContents());
    }

    public void apply(PlayerInventory player) {
        player.setArmorContents(getArmorContents());
        player.setItemInOffHand(getItemInOffHand());
        player.setStorageContents(getStorageContents());
    }

    public static ItemStack[] generateEmpty(int size) {
        ItemStack[] stacks = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            stacks[i] = new ItemStack(Material.AIR);
        }
        return stacks;
    }

}
