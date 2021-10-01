package ru.twistofthemyth.graves;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.kyori.adventure.text.Component;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.logging.Logger;

import static ru.twistofthemyth.graves.Utils.getDay;

public class Grave {
    private Logger log = GravesPlugin.getInstance().log;

    private static MessageManager msg = GravesPlugin.getInstance().msgManager();
    private static FileConfiguration config = GravesPlugin.getInstance().getConfig();
    private static final int maxDisplacementV = config.getInt("maxDisplacementV");
    private static final int maxDisplacementH = config.getInt("maxDisplacementH");
    private static final boolean debugMode = config.getBoolean("debugMode");

    private final Player player;
    private Block plateBlock;
    private Block chestBlock;
    private Block signBlock;


    public Grave(PlayerDeathEvent event) throws GravePlacementException {
        this.player = event.getEntity();

        if (event.getKeepInventory()) {
            player.sendMessage(msg.get("return_items"));
            throw new GravePlacementException("Player has KeepInventory");
        }
        Location graveLocation;
        try {
            graveLocation = shiftGravePlacement(player.getLocation());
        } catch (GravePlacementException exc) {
            player.sendMessage(msg.get("return_items"));
            throw exc;
        }
        Location chestLocation = getChestLocation(graveLocation);
        Location signLocation = getSignLocation(graveLocation);

        if (debugMode) {
            log.info(String.format("Death chest placed in X:%s Y:%s Z:%s", (long) chestLocation.getX(), (long) chestLocation.getY(), (long) chestLocation.getZ()));
        }

        //Plate
        plateBlock = graveLocation.getBlock();
        plateBlock.setType(Material.POLISHED_ANDESITE_STAIRS);
        //Chest
        chestBlock = chestLocation.getBlock();
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            Arrays.stream(player.getInventory().getContents()).forEach(itemStack -> {
                if (itemStack != null) chest.getBlockInventory().addItem(itemStack);
            });
        } else {
            log.warning("Unable to set death chest");
        }
        //Sign
        signBlock = signLocation.getBlock();
        signBlock.setType(Material.WARPED_WALL_SIGN);
        if (signBlock.getState() instanceof Sign sign) {
            sign.setGlowingText(config.getBoolean("glowingSign"));
            sign.setColor(Utils.parseColor(config.getString("signColor")));
            sign.line(0, player.name());
            sign.line(2, Component.text(msg.get("death_time")));
            sign.line(3, Component.text(getDay(player.getWorld().getFullTime()) + "D"));
            sign.update();
        } else {
            log.warning("Unable to set sign");
        }
        if (signBlock.getBlockData() instanceof WallSign dir) {
            dir.setFacing(BlockFace.NORTH);
        } else {
            log.warning("Sign is not attachable");
        }
        player.sendMessage(msg.get("grave_location") + String.format(" [X:%s Y:%s Z:%s]", (long) graveLocation.getX(), (long) graveLocation.getY(), (long) graveLocation.getZ()));
    }

    @Nullable
    private Location shiftGravePlacement(final Location graveLocation) throws GravePlacementException {
        World world = graveLocation.getWorld();
        double x = graveLocation.getX();
        double y = graveLocation.getY();
        double z = graveLocation.getZ();

        for (int i = 0; i <= maxDisplacementH; i += 50) {
            Location loc;
            //Base check
            if (i == 0) {
                loc = graveLocation.add(i, 0, 0);
                if (checkRegionProtection(loc)) {
                    Double height = getValidHeight(loc);
                    if (height != null) {
                        loc = loc.set(x, height, z);
                        if (checkRegionProtection(loc)) {
                            return loc;
                        }
                    }
                }
            } else {
                //Positive X check
                loc = graveLocation.add(i, 0, 0);
                if (checkRegionProtection(loc)) {
                    Double height = getValidHeight(loc);
                    if (height != null) {
                        loc = loc.set(x + i, height, z);
                        if (checkRegionProtection(loc)) {
                            return loc;
                        }
                    }
                }

                //Negative X check
                loc = graveLocation.add(-2 * i, 0, 0);
                if (checkRegionProtection(loc)) {
                    Double height = getValidHeight(loc);
                    if (height != null) {
                        loc = loc.set(x - i, height, z);
                        if (checkRegionProtection(loc)) {
                            return loc;
                        }
                    }
                }
                loc = graveLocation.add(i, 0, 0);
                //Positive Z check
                loc = graveLocation.add(0, 0, i);
                if (checkRegionProtection(loc)) {
                    Double height = getValidHeight(loc);
                    if (height != null) {
                        loc = loc.set(x, height, z + i);
                        if (checkRegionProtection(loc)) {
                            return loc;
                        }
                    }
                }
                //Negative Z check
                loc = graveLocation.add(0, 0, -2 * i);
                if (checkRegionProtection(loc)) {
                    Double height = getValidHeight(loc);
                    if (height != null) {
                        loc = loc.set(x, height, z - i);
                        if (checkRegionProtection(loc)) {
                            return loc;
                        }
                    }
                }
                loc = graveLocation.add(0, 0, i);
            }
        }
        throw new GravePlacementException("Unable to install the grave");
    }

    @Nullable
    private Double getValidHeight(final Location graveLocation) {
        double x = graveLocation.getX();
        double y = graveLocation.getY();
        double z = graveLocation.getZ();
        Location loc = graveLocation;
        if (isFloat(loc)) {
            RayTraceResult result = graveLocation.getWorld().rayTraceBlocks(graveLocation, new Vector(0, -maxDisplacementV, 0), maxDisplacementV, FluidCollisionMode.NEVER);
            if (result != null) {
                loc.set(x, result.getHitPosition().getY(), z);
                log.info(result.getHitBlock().toString());
            } else {
                return null;
            }
        } else if (isUnderground(loc)) {
            for (int i = 0; i < maxDisplacementV; i++) {
                if (y + i > 250) {
                    return null;
                }
                loc.set(x, y + i, z);
                if (!isUnderground(loc)) {
                    break;
                }
            }
        }
        return loc.getY();
    }

    private Location getChestLocation(final Location graveLocation) {
        return new Location(graveLocation.getWorld(), graveLocation.getX(), graveLocation.getY() - 1, graveLocation.getZ());
    }

    private Location getSignLocation(final Location graveLocation) {
        return new Location(graveLocation.getWorld(), graveLocation.getX(), graveLocation.getY(), graveLocation.getZ() - 1);
    }

    private boolean isFloat(final Location graveLocation) {
        if (getChestLocation(graveLocation).getBlock().getBlockData().getMaterial().equals(Material.AIR) ||
                getChestLocation(graveLocation).getBlock().getBlockData().getMaterial().equals(Material.WATER) ||
                getChestLocation(graveLocation).getBlock().getBlockData().getMaterial().equals(Material.LAVA)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isUnderground(final Location graveLocation) {
        if (graveLocation.getBlock().getBlockData().getMaterial().equals(Material.AIR)) {
            return false;
        }
        return true;
    }

    private boolean checkRegionProtection(final Location graveLocation) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        com.sk89q.worldedit.util.Location wgGraveLocation = BukkitAdapter.adapt(graveLocation);
        com.sk89q.worldedit.util.Location wgChestLocation = BukkitAdapter.adapt(getChestLocation(graveLocation));
        com.sk89q.worldedit.util.Location wgSignLocation = BukkitAdapter.adapt(getSignLocation(graveLocation));

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (!(query.testState(wgGraveLocation, localPlayer, Flags.BUILD)) ||
                !(query.testState(wgChestLocation, localPlayer, Flags.BUILD)) ||
                !(query.testState(wgSignLocation, localPlayer, Flags.BUILD))) {
            return false;
        } else {
            return true;
        }
    }
}
