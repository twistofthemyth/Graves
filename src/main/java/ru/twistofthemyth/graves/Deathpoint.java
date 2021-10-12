package ru.twistofthemyth.graves;

import org.bukkit.inventory.ItemStack;

public class Deathpoint {
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
        return GravesPlugin.getInstance().msgManager().get("death_point") + String.format("X:%s Y%s Z%s", x, y, z);
    }
}
