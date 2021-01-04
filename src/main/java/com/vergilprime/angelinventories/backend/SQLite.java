package com.vergilprime.angelinventories.backend;
/*
 * credits:  https://www.spigotmc.org/threads/how-to-sqlite.56847/
 */

import com.vergilprime.angelinventories.AngelInventories;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;


public class SQLite extends Database {

    public SQLite(AngelInventories plugin) {
        super(plugin);
    }

    @Override
    public Connection getSQLConnection() {
        File dbFile = new File(plugin.getDataFolder(), dbname + ".db");
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Please grab it from https://github.com/xerial/sqlite-jdbc/releases and put it in your server directory /libs folder.");
        }
        return null;
    }

    @Override
    public void load() {
        synchronized (connection) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS `player_inventories` (" +
                                "`uuid` UUID NOT NULL, " +
                                "`inv_id` INTEGER NOT NULL, " +
                                "`inventory_armor` BLOB NOT NULL, " +
                                "`inventory_storage` BLOB NOT NULL, " +
                                "`inventory_offhand` BLOB NOT NULL, " +
                                "PRIMARY KEY (`uuid`, `inv_id`));");
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS `custom_inventories` (" +
                                "`name` VARCHAR(64) NOT NULL, " +
                                "`inventory_armor` BLOB NOT NULL, " +
                                "`inventory_storage` BLOB NOT NULL, " +
                                "`inventory_offhand` BLOB NOT NULL, " +
                                "`setting` VARCHAR(16) NOT NULL, " +
                                "`locked_slots` TEXT, " +
                                "PRIMARY KEY (`name`));");
                statement.execute(
                        "CREATE TABLE IF NOT EXISTS `player_data` (" +
                                "`uuid` UUID NOT NULL, " +
                                "`current_pinv_index` INT NOT NULL, " +
                                "`current_custom_inv` VARCHAR(64), " +
                                "PRIMARY KEY (`uuid`));");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create SQLite tables!", e);
            }
        }
        initialize();
    }
}