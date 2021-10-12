package ru.twistofthemyth.graves;

import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

public class DeathListener implements Listener {

    private final Logger log = GravesPlugin.getInstance().getLog();
    private final Deathpoints dp = GravesPlugin.getInstance().getDeathpoints();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setCancelled(true);
        Player player = event.getEntity();
        ItemStack[] items = player.getInventory().getContents();
        boolean keepInventory = TriState.TRUE.equals(player.permissionValue("graves.keepinventory"));
        try {
            if (!keepInventory) {
                log.info("placing grave");
                new Grave(player).place();
            } else {
                player.sendMessage(GravesPlugin.getInstance().msgManager().get("return_items"));
            }
        } catch (GravePlacementException exc) {
            log.info("Getting items back to " + player.getName());
            keepInventory = true;
        } catch (Exception exc) {
            log.warning(exc.getMessage() + "\n" + Arrays.toString(exc.getStackTrace()));
        } finally {
            event.getEntity().getInventory().clear();
            event.getDrops().clear();
            event.setCancelled(false);
            dp.add(player.getName(),
                    player.getLocation().getBlockX(),
                    player.getLocation().getBlockY(),
                    player.getLocation().getBlockZ(),
                    (keepInventory ? items : null));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        String playerName = event.getPlayer().getName();
        if (null != dp.getLast(playerName) && null != Objects.requireNonNull(dp.getLast(playerName)).getItemsToKeep()) {
            ItemStack[] items = Objects.requireNonNull(dp.getLast(event.getPlayer().getName())).getItemsToKeep();
            event.getPlayer().getInventory().setContents(items);
        }
    }
}
