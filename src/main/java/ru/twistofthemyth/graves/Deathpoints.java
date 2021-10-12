package ru.twistofthemyth.graves;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class Deathpoints {

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
            data.get(playerName).add(dp);
        } else {
            dpList = data.put(playerName, new ArrayList<>() {{
                add(dp);
            }});
        }
        assert dpList != null;
        dpList.sort(Comparator.comparingLong(Deathpoint::getTime));
    }
}
