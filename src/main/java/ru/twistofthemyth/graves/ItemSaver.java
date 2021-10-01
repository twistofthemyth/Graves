package ru.twistofthemyth.graves;

import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ItemSaver {
    private String dataPath = GravesPlugin.getInstance().getDataFolder() + "/keepInventory.dat";
    private Map<String, ItemStack[]> data;

    public ItemSaver() {
        if (!Files.exists(Path.of(dataPath))) {
            data = new HashMap<>();
        } else {
            data = loadData();
        }
    }

    public void save(String name, ItemStack[] items) {
        data.put(name, items);
    }

    public boolean isExist(String name) {
        return data.containsKey(name);
    }

    public void saveData() {
        try {
            Files.delete(Path.of(dataPath));
        } catch (IOException e) {
            GravesPlugin.getInstance().log.warning(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(dataPath))) {
            outputStream.writeObject(data);
        } catch (IOException e) {
            GravesPlugin.getInstance().log.warning(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    public Map<String, ItemStack[]> loadData() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(dataPath))) {
            return (HashMap<String, ItemStack[]>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            GravesPlugin.getInstance().log.warning(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

    public ItemStack[] load(String name) {
        ItemStack[] items = data.get(name);
        data.remove(name);
        return items;
    }
}
