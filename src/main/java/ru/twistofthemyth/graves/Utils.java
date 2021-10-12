package ru.twistofthemyth.graves;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.logging.Logger;

public class Utils {

    private static final Logger log = GravesPlugin.getInstance().getLog();

    public static long getDay(long time) {
        return time / 24000;
    }

    public static DyeColor parseColor(String color) {
        switch (color) {
            case "WHITE":
                return DyeColor.WHITE;
            case "ORANGE":
                return DyeColor.ORANGE;
            case "MAGENTA":
                return DyeColor.MAGENTA;
            case "LIGHT_BLUE":
                return DyeColor.LIGHT_BLUE;
            case "YELLOW":
                return DyeColor.YELLOW;
            case "LIME":
                return DyeColor.LIME;
            case "PINK":
                return DyeColor.PINK;
            case "LIGHT_GRAY":
                return DyeColor.LIGHT_GRAY;
            case "CYAN":
                return DyeColor.CYAN;
            case "PURPLE":
                return DyeColor.PURPLE;
            case "BLUE":
                return DyeColor.BLUE;
            case "BROWN":
                return DyeColor.BROWN;
            case "GREEN":
                return DyeColor.GREEN;
            case "RED":
                return DyeColor.RED;
            case "BLACK":
                return DyeColor.BLACK;
            default:
                return DyeColor.GRAY;
        }
    }

    public static boolean isProtected(Player player, Block... blocks) {
        int counter = 0;
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        for (Block block : blocks) {
            Location loc = BukkitAdapter.adapt(block.getLocation());
            if ((query.testState(loc, localPlayer, Flags.BUILD))) {
                counter++;
            }
        }
        return !(counter == blocks.length);
    }

    public static boolean isFloat(Block... blocks) {
        boolean isFloat = false;
        for (Block block : blocks) {
            Material material = block.getBlockData().getMaterial();
            if (material.equals(Material.AIR) || material.equals(Material.WATER) || material.equals(Material.LAVA)) {
                isFloat = true;
            }
        }
        return isFloat;
    }

    public static boolean isBlacklisted(Block... blocks) {
        boolean isBlacklisted = false;
        for (Block block : blocks) {
            Material material = block.getBlockData().getMaterial();
            if (material.equals(Material.CHEST) || material.equals(Material.TRAPPED_CHEST) || material.equals(Material.BEDROCK)) {
                isBlacklisted = true;
            }
        }
        return isBlacklisted;
    }

    public static boolean isUnderground(Block... blocks) {
        boolean isUnderground = true;
        for (Block block : blocks) {
            Material material = block.getBlockData().getMaterial();
            if (material.equals(Material.AIR) || material.equals(Material.WATER) || material.equals(Material.LAVA)) {
                isUnderground = false;
            }
        }
        return isUnderground;
    }

    public static void rotateBlock(Block block, BlockFace rotation) {
        boolean rotated = false;
        if (block.getState() instanceof Rotatable blockState) {
            blockState.setRotation(rotation);
            rotated = true;
        } else if (block.getBlockData() instanceof Directional blockData) {
            blockData.setFacing(rotation);
            block.setBlockData(blockData);
        } else {
            log.warning("Unable to rotate " + block + "\n");
        }
        if (rotated) {
            block.getState().update();
        }
    }

    public static void fillChest(Block block, ItemStack[] items) {
        boolean filled = false;
        if (block.getState() instanceof Chest chest) {
            Arrays.stream(items).forEach(item -> {
                if (item != null) {
                    chest.getBlockInventory().addItem(item);
                }
            });
            filled = true;
        } else {
            log.warning("Unable to fill " + block + "\n");
        }
        if (filled) {
            block.getState().update();
        }
    }

    public static void fillDoubleChest(Block leftChest, Block rightChest, ItemStack[] items) {
        if (items.length > 27) {
            ItemStack[] leftItems = Arrays.copyOfRange(items, 0, 26);
            ItemStack[] rightItems = Arrays.copyOfRange(items, 27, items.length - 1);
            fillChest(leftChest, leftItems);
            fillChest(rightChest, rightItems);
        } else {
            fillChest(leftChest, items);
        }
    }

    public static void createDoubleChest(Block leftChest, Block rightChest) {
        boolean created = false;
        if (leftChest.getBlockData() instanceof org.bukkit.block.data.type.Chest lChest) {
            if (rightChest.getBlockData() instanceof org.bukkit.block.data.type.Chest rChest) {
                lChest.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
                rChest.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
                leftChest.setBlockData(lChest, false);
                rightChest.setBlockData(rChest, false);
                created = true;
            }
        }
        if (!created) {
            log.warning("Unable to create double chest from " + leftChest + "\n" + rightChest + "\n");
        }
    }
}
