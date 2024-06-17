package me.spacety.totemzombie;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private static FileConfiguration config;


    @Override
    public void onEnable() {
        loadCfg();

        getCommand("totemzombie").setExecutor(new ZombieTotemExecutor());

    }

    @Override
    public void onDisable() {
        
    }

    public static String getString(String key) {
        return config.getString(key);
    }

    public static void reloadCfg() {
        Main plugin = Main.getPlugin(Main.class);
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void loadCfg() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        saveDefaultConfig();
        config = getConfig();
    }
}
