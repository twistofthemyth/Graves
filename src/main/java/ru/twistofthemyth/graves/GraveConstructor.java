package ru.twistofthemyth.graves;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.logging.Logger;

public class GraveConstructor {

    private final static Logger log = Main.logger;
    private final Player player;
    private int maxIterations = 100;

    public GraveConstructor(Player player) {
        this.player = player;
    }

    public boolean createGrave() {
        Location graveLocation = shiftGravePlacement(player.getLocation());
        Location chestLocation = getChestLocation(graveLocation);
        Location signLocation = getSignLocation(graveLocation);
        log.info(String.format("Место сундука X:%s Y:%s Z:%s", chestLocation.getX(), chestLocation.getY(), chestLocation.getZ()));
        log.info(String.format("Место знака X:%s Y:%s Z:%s", signLocation.getX(), signLocation.getY(), signLocation.getZ()));
        log.info(String.format("Место плиты X:%s Y:%s Z:%s", graveLocation.getX(), graveLocation.getY(), graveLocation.getZ()));

        //Grave
        Block grave = graveLocation.getBlock();
        grave.setType(Material.POLISHED_ANDESITE_STAIRS);

        //Chest
        Block chestBlock = chestLocation.getBlock();
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getState() instanceof Chest chest) {
            Arrays.stream(player.getInventory().getContents()).forEach(itemStack -> {
                if (itemStack != null) chest.getBlockInventory().addItem(itemStack);
            });
        } else {
            log.warning("Невозможно установить сундук");
        }

        //Sign
        Block signBlock = signLocation.getBlock();
        signBlock.setType(Material.WARPED_WALL_SIGN);
        if (signBlock.getState() instanceof Sign sign) {
            sign.setGlowingText(true);
            sign.line(0, player.name());
            sign.update();
        } else {
            log.warning("Невозможно установить знак");
        }
        if (signBlock.getBlockData() instanceof WallSign dir) {
            dir.setFacing(BlockFace.NORTH);
        } else {
            log.warning("Sign is not attachable");
        }

        player.sendRawMessage(String.format("Твоя могила расположена: X:%s Y:%s Z:%s",
                graveLocation.getX(), graveLocation.getY(), graveLocation.getZ()));
        return true;
    }

    @Nullable
    private Location shiftGravePlacement(final Location graveLocation) {
        double x = graveLocation.getX();
        double y = graveLocation.getY();
        double z = graveLocation.getZ();

        Location loc = graveLocation;
        for (int i = 0; i <= 250; i += 25) {
            if (checkLocation(loc.set(x + i, y, z))) {
                Double height = getPlacementY(loc);
                if (height != null) {
                    loc.set(x + i, height, z);
                    if (checkLocation(loc)) {
                        log.info(String.format("Проверка на приват X:%s Y:%s Z:%s пройдена", loc.getX(), loc.getY(), loc.getZ()));
                        return loc;
                    }
                }
            }

            if (checkLocation(loc.set(x - i, y, z))) {
                Double height = getPlacementY(loc);
                if (height != null) {
                    loc.set(x - i, height, z);
                    if (checkLocation(loc)) {
                        log.info(String.format("Проверка на приват X:%s Y:%s Z:%s пройдена", loc.getX(), loc.getY(), loc.getZ()));
                        return loc;
                    }
                }
            }

            if (checkLocation(loc.set(x, y, z + i))) {
                Double height = getPlacementY(loc);
                if (height != null) {
                    loc.set(x, height, z + i);
                    if (checkLocation(loc)) {
                        loc = loc;
                        log.info(String.format("Проверка на приват X:%s Y:%s Z:%s пройдена", loc.getX(), loc.getY(), loc.getZ()));
                        return loc;
                    }
                }
            }

            if (checkLocation(loc.set(x, y, z - i))) {
                Double height = getPlacementY(loc);
                if (height != null) {
                    loc.set(x, height, z - i);
                    if (checkLocation(loc)) {
                        loc = loc;
                        log.info(String.format("Проверка на приват X:%s Y:%s Z:%s пройдена", loc.getX(), loc.getY(), loc.getZ()));
                        return loc;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private Double getPlacementY(final Location graveLocation) {
        double x = graveLocation.getX();
        double y = graveLocation.getY();
        double z = graveLocation.getZ();
        Location loc = graveLocation;
        if (graveIsFloat(loc)) {
            for (int i = 0; i > -maxIterations; i--) {
                loc.set(x, y + i, z);
                log.info("Проверка летающей гробницы на: " + loc.getY());
                if (!graveIsFloat(loc)) {
                    log.info("Гробница не летает на: " + loc.getY());
                    break;
                }
            }
        } else if (graveIsUnderground(loc)) {
            for (int i = 0; i < maxIterations; i++) {
                loc.set(x, y + i, z);
                log.info("Проверка погребенной гробницы на: " + loc.getY());
                if (!graveIsUnderground(loc)) {
                    log.info("Гробница не погребена на: " + loc.getY());
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

    private boolean graveIsFloat(final Location graveLocation) {
        if (getChestLocation(graveLocation).getBlock().getBlockData().getMaterial().equals(Material.AIR) ||
                getChestLocation(graveLocation).getBlock().getBlockData().getMaterial().equals(Material.WATER) ||
                getChestLocation(graveLocation).getBlock().getBlockData().getMaterial().equals(Material.LAVA)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean graveIsUnderground(final Location graveLocation) {
        if (graveLocation.getBlock().getBlockData().getMaterial().equals(Material.AIR)) {
            return false;
        }
        return true;
    }

    private boolean checkLocation(final Location graveLocation) {
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
