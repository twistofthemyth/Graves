package ru.twistofthemyth.graves;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Main extends JavaPlugin {

    static Logger logger = Logger.getLogger("Graves");

    @Override
    public void onEnable() {
        logger.info("Start Graves plugin");
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
    }
}
