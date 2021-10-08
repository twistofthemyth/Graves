package ru.twistofthemyth.graves;

import net.kyori.adventure.text.Component;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.twistofthemyth.graves.Utils.getDay;

public class Grave {

    private final static Logger log = GravesPlugin.getInstance().getLog();
    private final static FileConfiguration config = GravesPlugin.getInstance().getConfig();
    private final static int maxDisplacementV = config.getInt("maxDisplacementV");
    private final static int maxDisplacementH = config.getInt("maxDisplacementH");
    private final static boolean debugMode = config.getBoolean("debugMode");
    private final static MessageManager msg = GravesPlugin.getInstance().msgManager();

    private final Player player;
    private Block signBlock;
    private Block leftChest;
    private Block rightChest;
    private Block leftPlate;
    private Block rightPlate;
    private Block firstHeadstone;
    private Block secondHeadstone;

    private final World w;
    private int x;
    private int y;
    private int z;

    //private Location graveLocation;

    public Grave(Player player) {
        this.player = player;
        w = player.getWorld();
        x = (int) player.getLocation().getX();
        y = (int) player.getLocation().getY();
        z = (int) player.getLocation().getZ();
        setGrave(x, y, z);
    }

    public void place() throws GravePlacementException {
        try {
            findPlace();
        } catch (GravePlacementException exc) {
            player.sendMessage(msg.get("return_items"));
            throw exc;
        }

        List<ItemStack> items = Stream.of(player.getInventory().getContents()).filter(Objects::nonNull).collect(Collectors.toList());

        items.add(new ItemStack(rightChest.getType()));
        items.add(new ItemStack(leftChest.getType()));
        items.add(new ItemStack(leftPlate.getType()));
        items.add(new ItemStack(rightPlate.getType()));
        items.add(new ItemStack(firstHeadstone.getType()));
        items.add(new ItemStack(secondHeadstone.getType()));

        //place chests
        leftChest.setType(Material.CHEST);
        rightChest.setType(Material.CHEST);
        Utils.rotateBlock(leftChest, BlockFace.WEST);
        Utils.rotateBlock(rightChest, BlockFace.WEST);
        Utils.createDoubleChest(leftChest, rightChest);

        Utils.fillDoubleChest(leftChest, rightChest, items.toArray(new ItemStack[54]));

        //place plates
        leftPlate.setType(Material.MOSSY_COBBLESTONE_SLAB);
        rightPlate.setType(Material.MOSSY_COBBLESTONE_SLAB);
        //place headstone
        firstHeadstone.setType(Material.MOSSY_COBBLESTONE);
        secondHeadstone.setType(Material.MOSSY_COBBLESTONE_STAIRS);
        //place sign
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
            log.warning("Unable to set sign");
        }
        player.sendMessage(msg.get("grave_location") + String.format(" X:%s Y:%s Z:%s", x, y, z));
    }

    private void moveGrave(int addX, int addY, int addZ) {
        setGrave(x + addX, y + addY, z + addZ);
    }

    private void setGrave(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        leftChest = w.getBlockAt(x, y - 1, z - 1);
        rightChest = w.getBlockAt(x, y - 1, z - 2);
        leftPlate = w.getBlockAt(x, y, z - 1);
        rightPlate = w.getBlockAt(x, y, z - 2);
        firstHeadstone = w.getBlockAt(x, y, z);
        secondHeadstone = w.getBlockAt(x, y + 1, z);
        signBlock = w.getBlockAt(x, y + 1, z - 1);
    }

    private void logInfo() {
        log.info("\n########GRAVE INFO########");
        log.info(String.format("LEFT CHEST: X%s Y%s Z%s", leftChest.getX(), leftChest.getY(), leftChest.getZ()));
        log.info(String.format("RIGHT CHEST: X%s Y%s Z%s", rightChest.getX(), rightChest.getY(), rightChest.getZ()));
        log.info(String.format("LEFT PLATE: X%s Y%s Z%s", leftPlate.getX(), leftPlate.getY(), leftPlate.getZ()));
        log.info(String.format("RIGHT PLATE: X%s Y%s Z%s", rightPlate.getX(), rightPlate.getY(), rightPlate.getZ()));
        log.info(String.format("SIGN: X%s Y%s Z%s", signBlock.getX(), signBlock.getY(), signBlock.getZ()));
        log.info(String.format("1 HEADSTONE: X%s Y%s Z%s", firstHeadstone.getX(), firstHeadstone.getY(), firstHeadstone.getZ()));
        log.info(String.format("2 HEADSTONE: X%s Y%s Z%s", secondHeadstone.getX(), secondHeadstone.getY(), secondHeadstone.getZ()));
        log.info("######################\n");
    }

    private boolean checkPlace() {
        if (y < 5) {
            setGrave(x, 100, z);
            return false;
        }
        if (isNotProtected()) {
            Integer yMovement = getYMovement();
            if (yMovement != null) {
                moveGrave(0, yMovement, 0);
                return isNotProtected();
            }
        }
        return false;
    }

    private void findPlace() throws GravePlacementException {
        for (int i = 0; i <= maxDisplacementH; i += config.getInt("placePrecision")) {
            //Base check
            if (i == 0) {
                if (checkPlace()) return;
            } else {
                //Positive X check
                moveGrave(i, 0, 0);
                if (checkPlace()) return;
                //Positive Z check
                moveGrave(i, 0, i);
                if (checkPlace()) return;
                //Negative X check
                moveGrave(-2 * i, 0, 0);
                if (checkPlace()) return;
                //Negative Z check
                moveGrave(0, 0, -2 * i);
                if (checkPlace()) return;
            }
        }
        throw new GravePlacementException("Unable to install the grave");
    }

    @Nullable
    private Integer getYMovement() {
        Integer validY = null;
        if (isFloat()) {
            RayTraceResult result = w.rayTraceBlocks(secondHeadstone.getLocation(), new Vector(0, -maxDisplacementV, 0), maxDisplacementV, FluidCollisionMode.NEVER);
            if (result != null) {
                validY = (int) result.getHitPosition().getY();
            }
        } else if (!Utils.isUnderground(leftChest)) {
            for (int i = 1; i < maxDisplacementV; i++) {
                if (leftChest.getLocation().getY() + i > 250) {
                    validY = null;
                    break;
                } else if (!Utils.isUnderground(leftChest.getLocation().add(0, i, 0).getBlock())) {
                    i++;
                } else {
                    validY = i;
                }
            }
        } else {
            validY = 0;
        }
        return validY == null ? null : (validY - y);
    }

    private boolean isFloat() {
        return Utils.isFloat(firstHeadstone);
    }

    private boolean isNotProtected() {
        return !(Utils.isProtected(player, leftChest, rightChest, leftChest, rightChest, firstHeadstone, secondHeadstone, signBlock) ||
                Utils.isBlacklisted(leftChest, rightChest, leftChest, rightChest, firstHeadstone, secondHeadstone, signBlock));
    }
}
