package ru.twistofthemyth.graves;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

public class DataManager {
    private final String dataPath = GravesPlugin.getInstance().getDataFolder() + "/playerData.dat";
    private static final Logger log = GravesPlugin.getInstance().getLog();

    public static void saveDeathpoints(Deathpoints dp) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(GravesPlugin.getInstance().getDataFolder() + "/deathpoins.dat"))) {
            outputStream.writeObject(Deathpoints.class);
        } catch (IOException e) {
            log.warning(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public static Deathpoints loadDeathpoints() {
        Deathpoints dp;
        if (!Files.exists(Path.of(GravesPlugin.getInstance().getDataFolder() + "/deathpoins.dat"))) {
            dp = new Deathpoints();
        } else {
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(GravesPlugin.getInstance().getDataFolder() + "/deathpoins.dat"))) {
                dp = (Deathpoints) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                log.warning(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                dp = new Deathpoints();
            }
        }
        return dp;
    }
}
