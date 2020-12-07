package com.vergilprime.angelinventories.sqlite;

import com.vergilprime.angelinventories.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;


public abstract class Database {
    AngelInventories plugin;
    Connection connection;
    String dbname;
    Boolean debugging;

    public Database(AngelInventories plugin) {
        this.plugin = plugin;
        dbname = plugin.config.getString("database");
        debugging = plugin.config.getBoolean("debugging");
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize() {
        connection = getSQLConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT 1;");
            ResultSet rs = ps.executeQuery();
            close(ps, rs);

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    public void loadCustomInventories() {
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            connection = getSQLConnection();
            ps = connection.prepareStatement("SELECT * FROM custom_inventories;");

            rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String invstring_armor = rs.getString("inventory_armor");
                String invstring_storage = rs.getString("inventory_storage");
                String invstring_offhand = rs.getString("inventory_offhand");
                String lockedSlotsString = rs.getString("locked_slots");
                String settingString = rs.getString("setting");

                PlayerInventoryLight inventory = new PlayerInventoryLight();

                inventory.setArmorContents(InventorySerializer.itemStackArrayFromBase64(invstring_armor));
                inventory.setStorageContents(InventorySerializer.itemStackArrayFromBase64(invstring_storage));
                inventory.setItemInOffHand(InventorySerializer.itemStackArrayFromBase64(invstring_offhand)[0]);

                String[] stringSlots = lockedSlotsString.split(",");
                ArrayList<Integer> lockedSlots = new ArrayList<>();
                for (int i = 0; i < stringSlots.length; i++) {
                    lockedSlots.set(i, Integer.parseInt(stringSlots[i]));
                }

                CustomInventorySetting setting = CustomInventorySetting.valueOf(settingString);

                CustomInventory customInventory = new CustomInventory(inventory, setting, lockedSlots);

                plugin.customInventories.put(name, customInventory);
            }
        } catch (SQLException | IOException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public void setCustomInventory(String name, PlayerInventory inventory, CustomInventorySetting setting) {

        CustomInventory customInventory = new CustomInventory(inventory, setting, new ArrayList<>());
        plugin.customInventories.put(name, customInventory);


        PreparedStatement ps = null;
        try {
            connection = getSQLConnection();
            ps = connection.prepareStatement(
                    "REPLACE INTO 'custom_inventories' (name, inventory_armor, inventory_storage, inventory_extra, inventory_offhand, setting)" +
                            "VALUES (?,?,?,?,?,?)");

            ps.setString(1, name);

            String invstring_armor = InventorySerializer.itemStackArrayToBase64(inventory.getArmorContents());
            ps.setString(2, invstring_armor);

            String invstring_storage = InventorySerializer.itemStackArrayToBase64(inventory.getStorageContents());
            ps.setString(3, invstring_storage);

            String invstring_extras = InventorySerializer.itemStackArrayToBase64(inventory.getExtraContents());
            ps.setString(4, invstring_extras);

            String invstring_offhand = InventorySerializer.itemStackArrayToBase64(new ItemStack[]{inventory.getItemInOffHand()});
            ps.setString(5, invstring_offhand);

            String settingString = setting.name();
            ps.setString(6, settingString);

            ps.execute();
            ps.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite tables failed to create, check SQLite.java 59-63", e);
        }
    }

    public void loadPlayerData(UUID uuid) {
        PreparedStatement ps;
        ResultSet rs;
        ArrayList<PlayerInventoryLight> playerInventories = new ArrayList<>();
        try {
            connection = getSQLConnection();
            //plugin.getLogger().info("Proof of Life");
            ps = connection.prepareStatement(
                    "SELECT * FROM player_inventories\n" +
                            "LEFT JOIN player_data\n" +
                            "ON player_inventories.uuid = player_data.uuid\n" +
                            "WHERE player_inventories.uuid = '" + uuid + "';");
            rs = ps.executeQuery();
            Integer current_pinv_index = null;
            String current_custom_inv = null;

            HashMap<Integer, PlayerInventoryLight> invMap = new HashMap<>();
            Integer lastIndex = 0;
            while (rs.next()) {
                current_pinv_index = current_pinv_index == null ? rs.getInt("current_pinv_index") : current_pinv_index;
                current_custom_inv = current_custom_inv == null ? rs.getString("current_custom_inv") : current_custom_inv;
                PlayerInventoryLight inventory = new PlayerInventoryLight();
                inventory.setArmorContents(InventorySerializer.itemStackArrayFromBase64(rs.getString("inventory_armor")));
                inventory.setStorageContents(InventorySerializer.itemStackArrayFromBase64(rs.getString("inventory_storage")));
                inventory.setItemInOffHand(InventorySerializer.itemStackArrayFromBase64(rs.getString("inventory_offhand"))[0]);
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
            plugin.loadedPlayers.put(uuid, new PlayerData(plugin, uuid, current_pinv_index, current_custom_inv, playerInventories));
        } catch (SQLException | IOException throwables) {
            throwables.printStackTrace();
        }
    }

    public int savePlayerInventories(UUID uuid) {
        PlayerData playerData = plugin.loadedPlayers.get(uuid);
        if (playerData != null) {
            PreparedStatement ps;
            connection = getSQLConnection();

            String sqlQuery = "REPLACE INTO player_inventories (uuid, inv_id, inventory_armor, inventory_storage, inventory_extra, inventory_offhand) VALUES ";
            int i;
            for (i = 0; i < playerData.GetInventories().size(); i++) {
                if (i != playerData.GetInventories().size() - 1) {
                    sqlQuery = sqlQuery + "(?,?,?,?,?), ";
                } else {
                    sqlQuery = sqlQuery + "(?,?,?,?,?);";
                }
            }
            try {
                ps = connection.prepareStatement(sqlQuery);

                int j = 1;
                for (i = 0; i < playerData.GetInventories().size(); i++) {

                    if (debugging) {
                        plugin.getLogger().info("Inventory index " + i);
                        plugin.getLogger().info("Inventory is null: " + (playerData.GetInventories().get(i) == null));
                    }

                    ps.setString(j, uuid.toString());
                    j++;

                    ps.setInt(j, i);
                    j++;

                    String invstring_armor = InventorySerializer.itemStackArrayToBase64(playerData.GetInventories().get(i).getArmorContents());
                    ps.setString(j, invstring_armor);
                    j++;

                    String invstring_storage = InventorySerializer.itemStackArrayToBase64(playerData.GetInventories().get(i).getStorageContents());
                    ps.setString(j, invstring_storage);
                    j++;

                    String invstring_offhand = InventorySerializer.itemStackArrayToBase64(new ItemStack[]{playerData.GetInventories().get(i).getItemInOffHand()});
                    ps.setString(j, invstring_offhand);
                    j++;
                }

                ps.executeUpdate();
                ps.close();

                return 0;

            } catch (SQLException throwables) {
                throwables.printStackTrace();
                return 2;
            }
        } else {
            return 1;
        }
    }

    public int SavePlayerPointers(UUID uuid) {
        PlayerData playerData = plugin.loadedPlayers.get(uuid);
        if (playerData != null) {
            try {
                PreparedStatement ps;

                ps = connection.prepareStatement("REPLACE INTO 'player_data' (`uuid`, `current_pinv_index`, `current_custom_inv`) " +
                        "VALUES (?,?,?)");

                ps.setObject(1, uuid);
                ps.setInt(2, playerData.GetCurrentPinvIndex());
                ps.setString(3, playerData.GetCurrentCustomInv());

                ps.executeUpdate();
                ps.close();

                return 0;
            } catch (SQLException ex) {
                return 2;
            }
        } else {
            return 1;
        }
    }

    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
}

// Credit: https://www.spigotmc.org/threads/how-to-sqlite.56847/