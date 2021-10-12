package ru.twistofthemyth.graves;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class GravesPlugin extends JavaPlugin {

    private final Logger log = Logger.getLogger("Graves");
    private static GravesPlugin instance;
    private Deathpoints dp;
    private MessageManager msgManager;
    private FileConfiguration config;

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
        dp = DataManager.loadDeathpoints();
        msgManager = new MessageManager(config.getString("locale"));
    }

    public void onDisable() {
        DataManager.saveDeathpoints(dp);
    }

    public @NotNull Deathpoints getDeathpoints() {
        return dp;
    }

    public @NotNull MessageManager msgManager() {
        return msgManager;
    }

    public @NotNull Logger getLog() {
        return log;
    }
}
