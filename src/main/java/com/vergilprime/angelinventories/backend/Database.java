package com.vergilprime.angelinventories.backend;

import com.vergilprime.angelinventories.AngelInventories;
import com.vergilprime.angelinventories.data.CustomInventory;
import com.vergilprime.angelinventories.data.CustomInventorySetting;
import com.vergilprime.angelinventories.data.PlayerData;
import com.vergilprime.angelinventories.data.PlayerInventoryLight;
import com.vergilprime.angelinventories.util.BukkitFuture;
import com.vergilprime.angelinventories.util.InventorySerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;


public abstract class Database {

    AngelInventories plugin;
    Connection connection;
    String dbname;
    Boolean debugging;

    String QUERY_TEST_CONNECTION = "SELECT 1;";
    String QUERY_LOAD_CUSTOM_INVENTORIES = "SELECT * FROM custom_inventories;";
    String QUERY_SET_CUSTOM_INVENTORY = "REPLACE INTO custom_inventories (name, inventory_armor, inventory_storage, inventory_extra, inventory_offhand, setting) VALUES (?,?,?,?,?,?);";
    String QUERY_LOAD_PLAYER_DATA = "SELECT * FROM player_inventories ON player_inventories.uuid = player_data.uuid WHERE player_inventories.uuid = ?;";

    String QUERY_SAVE_PLAYER_INVENTORIES_PREFIX = "REPLACE INTO player_inventories (uuid, inv_id, inventory_armor, inventory_storage, inventory_extra, inventory_offhand) VALUES ";
    String QUERY_SAVE_PLAYER_INVENTORIES_REPEAT = "(?,?,?,?,?)";
    String QUERY_SAVE_PLAYER_INVENTORIES_DELIMITER = ", ";
    String QUERY_SAVE_PLAYER_INVENTORIES_SUFFIX = ";";

    String QUERY_SAVE_PLAYER_POINTERS = "REPLACE INTO player_data (uuid, current_pinv_index, current_custom_inv) VALUES (?,?,?)";

    public Database(AngelInventories plugin) {
        this.plugin = plugin;
        dbname = plugin.getConfig().getString("database");
        debugging = plugin.getConfig().getBoolean("debugging");
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize() {
        connection = getSQLConnection();
        try (PreparedStatement ps = connection.prepareStatement(QUERY_TEST_CONNECTION)) {
            ps.executeQuery();
            plugin.getLogger().info("Connected to database with backed " + getClass().getName() + " successfully.");
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    public BukkitFuture<Void> loadCustomInventories() {
        return BukkitFuture.async(() -> {
            synchronized (connection) {
                try (PreparedStatement ps = connection.prepareStatement(QUERY_LOAD_CUSTOM_INVENTORIES)) {
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        String name = rs.getString("name");
                        byte[] inv_armor = rs.getBytes("inventory_armor");
                        byte[] inv_storage = rs.getBytes("inventory_storage");
                        byte[] inv_offhand = rs.getBytes("inventory_offhand");
                        String lockedSlotsString = rs.getString("locked_slots");
                        String settingString = rs.getString("setting");

                        PlayerInventoryLight inventory = new PlayerInventoryLight();

                        inventory.setArmorContents(InventorySerializer.bytesToItems(inv_armor));
                        inventory.setStorageContents(InventorySerializer.bytesToItems(inv_storage));
                        inventory.setItemInOffHand(InventorySerializer.bytesToItems(inv_offhand)[0]);

                        String[] stringSlots = lockedSlotsString.split(",");
                        ArrayList<Integer> lockedSlots = new ArrayList<>();
                        for (int i = 0; i < stringSlots.length; i++) {
                            lockedSlots.set(i, Integer.parseInt(stringSlots[i]));
                        }

                        CustomInventorySetting setting = CustomInventorySetting.valueOf(settingString);

                        CustomInventory customInventory = new CustomInventory(inventory, setting, lockedSlots);

                        plugin.getCustomInventories().put(name, customInventory);
                    }
                } catch (SQLException | IOException | ClassNotFoundException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Unable to load custom inventories:", ex);
                }
            }
            return null;
        });
    }

    public BukkitFuture<Void> setCustomInventory(String name, PlayerInventory inventory, CustomInventorySetting setting) {
        return BukkitFuture.async(() -> {
            synchronized (connection) {
                CustomInventory customInventory = new CustomInventory(inventory, setting, new ArrayList<>());
                plugin.getCustomInventories().put(name, customInventory);


                try (PreparedStatement ps = connection.prepareStatement(QUERY_SET_CUSTOM_INVENTORY)) {

                    ps.setString(1, name);

                    byte[] inv_armor = InventorySerializer.itemsToBytes(inventory.getArmorContents());
                    ps.setBytes(2, inv_armor);

                    byte[] inv_storage = InventorySerializer.itemsToBytes(inventory.getStorageContents());
                    ps.setBytes(3, inv_storage);

                    byte[] inv_extras = InventorySerializer.itemsToBytes(inventory.getExtraContents());
                    ps.setBytes(4, inv_extras);

                    byte[] inv_offhand = InventorySerializer.itemsToBytes(new ItemStack[]{inventory.getItemInOffHand()});
                    ps.setBytes(5, inv_offhand);

                    String settingString = setting.name();
                    ps.setString(6, settingString);

                    ps.execute();
                } catch (SQLException | IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to save custom inventory '" + name + "'", e);
                }
            }
            return null;
        });
    }

    public BukkitFuture<Void> loadPlayerData(UUID uuid) {
        return BukkitFuture.async(() -> {
            synchronized (connection) {
                ArrayList<PlayerInventoryLight> playerInventories = new ArrayList<>();
                try (PreparedStatement ps = connection.prepareStatement(QUERY_LOAD_PLAYER_DATA)) {
                    ps.setObject(1, uuid);
                    ResultSet rs = ps.executeQuery();
                    Integer current_pinv_index = null;
                    String current_custom_inv = null;

                    HashMap<Integer, PlayerInventoryLight> invMap = new HashMap<>();
                    Integer lastIndex = 0;
                    while (rs.next()) {
                        current_pinv_index = current_pinv_index == null ? rs.getInt("current_pinv_index") : current_pinv_index;
                        current_custom_inv = current_custom_inv == null ? rs.getString("current_custom_inv") : current_custom_inv;
                        PlayerInventoryLight inventory = new PlayerInventoryLight();
                        inventory.setArmorContents(InventorySerializer.bytesToItems(rs.getBytes("inventory_armor")));
                        inventory.setStorageContents(InventorySerializer.bytesToItems(rs.getBytes("inventory_storage")));
                        inventory.setItemInOffHand(InventorySerializer.bytesToItems(rs.getBytes("inventory_offhand"))[0]);
                        Integer index = rs.getInt("inv_id");
                        if (index > lastIndex) {
                            lastIndex = index;
                        }
                        invMap.put(rs.getInt("inv_id"), inventory);
                    }
                    for (int i = 0; i < lastIndex; i++) {
                        playerInventories.add(invMap.get(i));
                    }
                    current_pinv_index = current_pinv_index == null ? 0 : current_pinv_index;
                    plugin.getLoadedPlayers().put(uuid, new PlayerData(plugin, uuid, current_pinv_index, current_custom_inv, playerInventories));
                } catch (SQLException | IOException | ClassNotFoundException exception) {
                    exception.printStackTrace();
                }
            }
            return null;
        });
    }

    public BukkitFuture<Boolean> savePlayerInventories(PlayerData playerData) {
        return BukkitFuture.async(() -> {
            synchronized (connection) {
                String sqlQuery = QUERY_SAVE_PLAYER_INVENTORIES_PREFIX;
                String[] repeat = new String[playerData.getInventories().size()];
                Arrays.fill(repeat, QUERY_SAVE_PLAYER_INVENTORIES_REPEAT);
                sqlQuery += String.join(QUERY_SAVE_PLAYER_INVENTORIES_DELIMITER, repeat) + QUERY_SAVE_PLAYER_INVENTORIES_SUFFIX;
                try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {

                    int j = 1;
                    for (int i = 0; i < playerData.getInventories().size(); i++) {

                        if (debugging) {
                            plugin.getLogger().info("Inventory index " + i);
                            plugin.getLogger().info("Inventory is null: " + (playerData.getInventories().get(i) == null));
                        }

                        ps.setString(j, playerData.toString());
                        j++;

                        ps.setInt(j, i);
                        j++;

                        byte[] inv_armor = InventorySerializer.itemsToBytes(playerData.getInventories().get(i).getArmorContents());
                        ps.setBytes(j, inv_armor);
                        j++;

                        byte[] inv_storage = InventorySerializer.itemsToBytes(playerData.getInventories().get(i).getStorageContents());
                        ps.setBytes(j, inv_storage);
                        j++;

                        byte[] inv_offhand = InventorySerializer.itemsToBytes(new ItemStack[]{playerData.getInventories().get(i).getItemInOffHand()});
                        ps.setBytes(j, inv_offhand);
                        j++;
                    }

                    ps.executeUpdate();
                    return true;

                } catch (SQLException | IOException exception) {
                    exception.printStackTrace();
                    return false;
                }
            }
        });
    }

    public BukkitFuture<Boolean> savePlayerPointers(PlayerData data) {
        return BukkitFuture.async(() -> {
            synchronized (connection) {
                try (PreparedStatement ps = connection.prepareStatement(QUERY_SAVE_PLAYER_POINTERS)) {

                    ps.setObject(1, data.getUUID());
                    ps.setInt(2, data.getCurrentPlayerInvIndex());
                    ps.setString(3, data.getCurrentCustomInv());

                    ps.executeUpdate();
                    ps.close();

                    return true;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        });
    }

}