package ru.twistofthemyth.graves;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class GravesPlugin extends JavaPlugin {

    private static GravesPlugin instance;
    Logger log = Logger.getLogger("Graves");
    FileConfiguration config;
    private ItemSaver itemSaver;

    public static GravesPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        log.info("Starting \"Graves\" plugin");
        saveDefaultConfig();
        config = getConfig();
        instance = this;
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        itemSaver = new ItemSaver();
    }

    public void onDisable() {
        itemSaver.saveData();
    }

    public ItemSaver getItemSaver() {
        return itemSaver;
    }
}
