package com.vergilprime.angelinventories.util;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.data.CustomInventory;
import com.vergilprime.angelinventories.data.PlayerInventoryLight;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LockedSlotEditor implements Listener {

    private static List<LockedSlotEditor> openEditors = new ArrayList<>();
    private static ItemStack LOCK_ITEM = new ItemStack(Material.BARRIER);

    static {
        ItemMeta im = LOCK_ITEM.getItemMeta();
        im.setDisplayName(ChatColor.RED + "SLOT LOCKED");
        LOCK_ITEM.setItemMeta(im);
    }

    private String name;
    private Player player;
    private PlayerInventoryLight origInv;

    public LockedSlotEditor(Player player, String name) {
        this.player = player;
        this.name = name;
        open();
    }

    private void open() {
        CustomInventory inv = AngelInventories.getInstance().getCustomInventory(name);
        if (inv == null) {
            Chat.error(player, "Unknown custom inventory {0}", name);
            return;
        }
        player.closeInventory();
        Bukkit.getPluginManager().registerEvents(this, AngelInventories.getInstance());
        origInv = new PlayerInventoryLight(player.getInventory());
        player.getInventory().clear();
        for (int slot : inv.getLockedSlots()) {
            player.getInventory().setItem(slot, LOCK_ITEM.clone());
        }
        // player.openInventory(player.getOpenInventory()); we are not allowed to open the players own crafting view
        Chat.main(player, "Open your inventory to edit locked slots for {0}", name);
    }

    private void close() {
        HandlerList.unregisterAll(this);
        player.closeInventory();
        CustomInventory inv = AngelInventories.getInstance().getCustomInventory(name);
        if (inv == null) {
            Chat.error(player, "Unable to save locked slots\nSeems the custom inventory {0} has been deleted", name);
            return;
        }
        List<Integer> lockedSlots = new ArrayList<>();
        for (int slot = 0; slot <= 40; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                lockedSlots.add(slot);
            }
        }
        inv.setLockedSlots(lockedSlots);
        inv.save();
        origInv.apply(player.getInventory());
        Chat.main(player, "Saved locked slots for {0}", inv.getName());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) {
            return;
        }
        event.setCancelled(true);
        if (!event.getClickedInventory().equals(player.getInventory())) {
            return;
        }
        if (LOCK_ITEM.equals(event.getCurrentItem())) {
            event.setCurrentItem(null);
        } else {
            event.setCurrentItem(LOCK_ITEM.clone());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer().equals(player)) {
            close();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) {
            close();
        }
    }

    // --- safety checks ---
    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        // this event is not called for when the player opens their own internal crafting view
        // if this event is called, then the player opened another inventory like a chest or similar
        if (event.getPlayer().equals(player)) {
            close();
        }
    }

    @EventHandler
    public void onPickup(InventoryPickupItemEvent event) {
        if (player.equals(event.getInventory().getHolder())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().equals(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getPlayer().equals(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        if (event.getPlayer().equals(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().equals(player)) {
            event.setCancelled(true);
        }
    }
    // ---------------------

    public static void closeAll() {
        for (LockedSlotEditor editor : openEditors) {
            editor.close();
        }
    }


}
