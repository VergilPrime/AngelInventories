package com.vergilprime.angelinventories.sqlite;

import com.vergilprime.angelinventories.AngelInventories;

import java.util.logging.Level;

public class Error {
    public static void execute(AngelInventories plugin, Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }

    public static void close(AngelInventories plugin, Exception ex) {
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}

// Credit: https://www.spigotmc.org/threads/how-to-sqlite.56847/