package com.vergilprime.angelinventories;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private static FileConfiguration getConfig() {
        return AngelInventories.getInstance().getConfig();
    }

    public static int getMaxInventories() {
        return getConfig().getInt("MaxInventories");
    }

    public static String getDatabase() {
        return getConfig().getString("Database");
    }

    public static boolean getDebugMode() {
        return getConfig().getBoolean("Debugging");
    }

    public static void saveDefaults() {
        FileConfiguration config = getConfig();
        config.addDefault("Debugging", false);
        config.addDefault("Database", "storage");
        config.addDefault("MaxInventories", 5);
        config.options().copyDefaults(true);
        AngelInventories.getInstance().saveConfig();
    }
}
