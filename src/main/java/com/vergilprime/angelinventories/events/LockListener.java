package com.vergilprime.angelinventories.events;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.data.CustomInventory;
import com.vergilprime.angelinventories.data.PlayerData;
import com.vergilprime.angelinventories.util.BukkitFuture;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class LockListener implements Listener {

    private AngelInventories plugin;
    private Map<UUID, Integer> lastClick = new HashMap<>();

    public LockListener(AngelInventories plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void addClick(HumanEntity player) {
        int tick = player.getTicksLived();
        lastClick.put(player.getUniqueId(), tick);
        BukkitFuture.run(true, () -> {
            if (lastClick.get(player.getUniqueId()) == tick) {
                lastClick.remove(player.getUniqueId());
            }
        });
    }

    private List<Integer> getAffectedSlotsShiftClick(InventoryView view, int slot) {
        List<Integer> affected = new ArrayList<>();
        ItemStack[] clone = new ItemStack[view.countSlots()];
        for (int i = 0; i < clone.length; i++) {
            clone[i] = view.getItem(i).clone();
        }
        try {
            Method shiftClick = null;
            Object craftContainer = view.getClass().getMethod("getHandle").invoke(view);
            for (Method m : craftContainer.getClass().getMethods()) {
                if (m.getName().equals("shiftClick")
                        && Modifier.isPublic(m.getModifiers())
                        && m.getParameterCount() == 2
                        && m.getParameterTypes()[0].getSimpleName().equals("EntityHuman")
                        && m.getParameterTypes()[1] == int.class) {
                    shiftClick = m;
                    break;
                }
            }
            if (shiftClick == null) {
                throw new RuntimeException("Unable to locate Container::shiftClick(EntityHuman, int) in " + craftContainer.getClass().getName());
            }
            Object entityHuman = view.getPlayer().getClass().getMethod("getHandle").invoke(view.getPlayer());
            ItemStack item = view.getItem(slot).clone();
            for (int i = 0; i < item.getAmount(); i++) {
                shiftClick.invoke(craftContainer, entityHuman, slot);
                ItemStack left = view.getItem(slot);
                if (left == null || left.getAmount() == 0 || left.getType() == Material.AIR || item.equals(left)) {
                    break;
                }
            }
        } catch (Exception e) {
            AngelInventories.getInstance().getLogger().log(Level.WARNING, "Unable to simulate shift-click!");
            AngelInventories.getInstance().getLogger().log(Level.WARNING, "Slot: " + slot + ", View: " + view.getTopInventory().getType() + ", " + view.getBottomInventory().getType() + ", " + view.getPlayer());
            AngelInventories.getInstance().getLogger().log(Level.WARNING, "Error: ", e);
        }
        for (int i = 0; i < clone.length; i++) {
            if (!view.getItem(i).equals(clone[i])) {
                affected.add(i);
            }
        }
        for (int i = 0; i < clone.length; i++) {
            view.setItem(i, clone[i]);
        }
        return affected;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        PlayerData data = plugin.getLoadedPlayers().get(event.getWhoClicked().getUniqueId());
        if (data.getCurrentCustomInv() == null
                || !(event.getView().getBottomInventory() instanceof PlayerInventory)
                || event.getView().getBottomInventory().getHolder() != event.getWhoClicked()) {
            return;
        }
        PlayerInventory playerInv = (PlayerInventory) event.getView().getBottomInventory();
        CustomInventory customInv = plugin.getCustomInventory(data.getCurrentCustomInv());
        boolean bottom = event.getClickedInventory() == playerInv;
        boolean self = event.getView().getType() != InventoryType.CRAFTING;
        if (bottom && customInv.getLockedSlots().contains(event.getSlot())) {
            event.setCancelled(true);
            return;
        }
        switch (event.getAction()) {
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                int slot = event.getHotbarButton();
                if (slot == -1) {
                    slot = 40; // off hand
                }
                if (customInv.getLockedSlots().contains(slot)) {
                    event.setCancelled(true);
                }
                return;

            case MOVE_TO_OTHER_INVENTORY: // shift-click
                for (int i : getAffectedSlotsShiftClick(event.getView(), event.getRawSlot())) {
                    if (event.getView().getInventory(i).equals(playerInv)
                            && customInv.getLockedSlots().contains(event.getView().convertSlot(i))) {
                        event.setCancelled(true);
                    }
                }


            case COLLECT_TO_CURSOR:
                ItemStack cursor = event.getCursor();
                for (int i : customInv.getLockedSlots()) {
                    if (cursor.isSimilar(playerInv.getItem(i))) {
                        event.setCancelled(true);
                        break;
                    }
                }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        addClick(event.getWhoClicked());
        if (!(event.getInventory() instanceof PlayerInventory) && !(event.getInventory() instanceof CraftingInventory)) {
            return;
        }
        if (event.getInventory().getHolder() != event.getWhoClicked()) {
            return;
        }
        PlayerData data = plugin.getLoadedPlayers().get(event.getWhoClicked().getUniqueId());
        if (data.getCurrentCustomInv() == null) {
            return;
        }
        CustomInventory inv = plugin.getCustomInventory(data.getCurrentCustomInv());

        for (int i : inv.getLockedSlots()) {
            if (event.getInventorySlots().contains(i)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Hacky way of checking the stack trace to verify that the dropItem call originates from a Q or CTRL-Q packet by the player
     */
    private boolean isFromDropItemStackPacket() {
        boolean foundNMS = false;
        boolean foundDropItem = false;
        for (StackTraceElement trace : Thread.currentThread().getStackTrace()) {
            if (trace.getClassName().startsWith("net.minecraft.server.")) {
                foundNMS = true;
            } else if (foundNMS) {
                return false;
            }
            if (foundNMS) {
                if (trace.getMethodName().equals("dropItem")) {
                    foundDropItem = true;
                    continue;
                }
                if (foundDropItem) {
                    if (trace.getClassName().endsWith("PlayerConnection")) {
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!isFromDropItemStackPacket()) {
            return;
        }

        PlayerData data = plugin.getLoadedPlayers().get(event.getPlayer().getUniqueId());
        if (data.getCurrentCustomInv() == null) {
            return;
        }
        CustomInventory inv = plugin.getCustomInventory(data.getCurrentCustomInv());
        if (inv.getLockedSlots().contains(event.getPlayer().getInventory().getHeldItemSlot())) {
            event.setCancelled(true);
        }

    }

    private int getArmorSlot(ItemStack item) {
        if (item == null) {
            return -1;
        }
        String name = item.getType().name();
        if (name.endsWith("_BOOTS")) {
            return 36;
        } else if (name.endsWith("_LEGGINGS")) {
            return 37;
        } else if (name.endsWith("_CHESTPLATE")) {
            return 38;
        } else if (name.endsWith("_HELMET")) {
            return 39;
        }
        return -1;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        PlayerData data = plugin.getLoadedPlayers().get(event.getPlayer().getUniqueId());
        if (data.getCurrentCustomInv() == null) {
            return;
        }
        CustomInventory inv = plugin.getCustomInventory(data.getCurrentCustomInv());
        if (inv == null) {
            return;
        }
        if (inv.getLockedSlots().contains(getArmorSlot(event.getItem()))) {
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        PlayerData data = plugin.getLoadedPlayers().get(event.getPlayer().getUniqueId());
        if (data.getCurrentCustomInv() == null) {
            return;
        }
        CustomInventory inv = plugin.getCustomInventory(data.getCurrentCustomInv());
        if (inv == null) {
            return;
        }
        int main = event.getPlayer().getInventory().getHeldItemSlot();
        int off = 40;
        if (inv.getLockedSlots().contains(main) || inv.getLockedSlots().contains(off)) {
            event.setCancelled(true);
        }
    }

}
