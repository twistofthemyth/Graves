package ru.twistofthemyth.graves;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class MessageManager {

    private FileConfiguration locale;

    public MessageManager(String localeName) {
        try {
            File localeFile = new File(GravesPlugin.getInstance().getDataFolder(), localeName + ".yml");
            GravesPlugin.getInstance().saveResource(localeName + ".yml", true);
            locale = new YamlConfiguration();
            locale.load(localeFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public String get(String name) {
        String message = locale.getString(name);
        return message == null ? "" : ChatColor.AQUA + "[Graves] " + message;
    }
}
