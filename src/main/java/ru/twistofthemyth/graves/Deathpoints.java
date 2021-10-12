package ru.twistofthemyth.graves;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

public class Deathpoints implements Serializable {

    private final static int maxDeathpoints = 20;
    private final Map<String, List<Deathpoint>> data;

    public Deathpoints() {
        data = new HashMap<>();
    }

    @Nullable
    public List<Deathpoint> get(String playerName) {
        return data.get(playerName);
    }

    @Nullable
    public Deathpoint getLast(String playerName) {
        List<Deathpoint> dpList = get(playerName);
        if (dpList == null) {
            return null;
        } else {
            int size = Objects.requireNonNull(get(playerName)).size();
            return Objects.requireNonNull(get(playerName)).get(size - 1);
        }
    }

    public void add(String playerName, int x, int y, int z, ItemStack[] itemsToKeep) {
        Deathpoint dp = new Deathpoint(x, y, z, itemsToKeep);
        List<Deathpoint> dpList;
        if (data.containsKey(playerName)) {
            dpList = data.get(playerName);
            if (dpList.size() > maxDeathpoints) {
                dpList.set(0, dp);
            }
            else {
                data.get(playerName).add(dp);
            }
            dpList.sort(Comparator.comparingLong(Deathpoint::getTime));
        } else {
            data.put(playerName, new ArrayList<>() {{
                add(dp);
            }});
        }
    }

    public static class Deathpoint implements Serializable {
        private final int x;
        private final int y;
        private final int z;
        private final ItemStack[] itemsToKeep;
        private final long time;

        public Deathpoint(int x, int y, int z, ItemStack[] itemsToKeep) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.itemsToKeep = itemsToKeep;
            time = System.currentTimeMillis();
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public ItemStack[] getItemsToKeep() {
            return itemsToKeep;
        }

        public long getTime() {
            return time;
        }

        @Override
        public String toString() {
            return String.format("X:%s Y:%s Z:%s", x, y, z);
        }
    }
}
