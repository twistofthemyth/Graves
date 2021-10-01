package ru.twistofthemyth.graves;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.logging.Logger;

public class DeathListener implements Listener {

    private final Logger log = GravesPlugin.getInstance().log;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setCancelled(true);
        Player player = event.getEntity();
        ItemStack[] items = player.getInventory().getContents();
        boolean keepInventory = event.getKeepInventory();
        try {
            new Grave(event);
        } catch (GravePlacementException exc) {
            log.info("Getting items back to " + player.getName());
            keepInventory = true;
        } catch (Exception exc) {
            log.warning(exc.getMessage() + "\n" + Arrays.toString(exc.getStackTrace()));
        } finally {
            if (keepInventory) {
                GravesPlugin.getInstance().getItemSaver().save(player.getName(), (items));
            }
            event.getEntity().getInventory().clear();
            event.getDrops().clear();
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        ItemSaver itemSaver = GravesPlugin.getInstance().getItemSaver();
        String playerName = event.getPlayer().getName();
        if (itemSaver.isExist(playerName)) {
            ItemStack[] items = itemSaver.load(playerName);
            event.getPlayer().getInventory().setContents(items);
        }
    }
}
