package com.vergilprime.angelinventories.sqlite;

import com.vergilprime.angelinventories.AngelInventories;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;


public class SQLite extends Database {
    private final String dbname;
    private final boolean debugging;

    public SQLite(AngelInventories plugin) {
        super(plugin);
        dbname = plugin.config.getString("database");
        debugging = plugin.config.getBoolean("debugging");
    }

    // SQL creation stuff, You can leave the blow stuff untouched.
    @Override
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
            }
        }
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
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
        connection = getSQLConnection();
        try {
            Statement statement = connection.createStatement();
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS `player_inventories` (" +
                            "`uuid` UUID NOT NULL, " +
                            "`inv_id` INTEGER NOT NULL, " +
                            "`inventory_armor` TEXT NOT NULL, " +
                            "`inventory_storage` TEXT NOT NULL, " +
                            "`inventory_extra` TEXT NOT NULL, " +
                            "`inventory_offhand` TEXT NOT NULL, " +
                            "PRIMARY KEY (`uuid`, `inv_id`));");
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS `custom_inventories` (" +
                            "`name` TEXT NOT NULL, " +
                            "`inventory_armor` TEXT NOT NULL, " +
                            "`inventory_storage` TEXT NOT NULL, " +
                            "`inventory_extra` TEXT NOT NULL, " +
                            "`inventory_offhand` TEXT NOT NULL, " +
                            "`setting` TEXT NOT NULL, " +
                            "`locked_slots` TEXT, " +
                            "PRIMARY KEY (`name`));");
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS `player_data` (" +
                            "`uuid` UUID NOT NULL, " +
                            "`current_pinv_index` INT NOT NULL, " +
                            "`current_custom_inv` TEXT, " +
                            "PRIMARY KEY (`uuid`));");
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "SQLite tables failed to create, check SQLite.java 59-63", e);
        }
        initialize();
    }
}

// Credit: https://www.spigotmc.org/threads/how-to-sqlite.56847/