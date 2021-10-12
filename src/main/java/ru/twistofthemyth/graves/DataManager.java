package ru.twistofthemyth.graves;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

public class DataManager {
    private static final Logger log = GravesPlugin.getInstance().getLog();

    public static void saveDeathpoints(Deathpoints dp) {
        try (BukkitObjectOutputStream outputStream = new BukkitObjectOutputStream(new FileOutputStream(GravesPlugin.getInstance().getDataFolder() + "/deathpoins.dat"))) {
            outputStream.writeObject(dp);
        } catch (IOException e) {
            log.warning(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public static Deathpoints loadDeathpoints() {
        Deathpoints dp;
        if (!Files.exists(Path.of(GravesPlugin.getInstance().getDataFolder() + "/deathpoins.dat"))) {
            dp = new Deathpoints();
        } else {
            try (BukkitObjectInputStream inputStream = new BukkitObjectInputStream(new FileInputStream(GravesPlugin.getInstance().getDataFolder() + "/deathpoins.dat"))) {
                dp = (Deathpoints) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                log.warning(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                dp = new Deathpoints();
            }
        }
        return dp;
    }
}
