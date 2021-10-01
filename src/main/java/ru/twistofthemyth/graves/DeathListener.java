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

    private Logger log = GravesPlugin.getInstance().log;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setCancelled(true);
        Player player = event.getEntity();
        ItemStack[] items = player.getInventory().getContents();
        boolean keepInventory = event.getKeepInventory();
        try {
            new Grave(event);
        } catch (GravePlacementException exc) {
            log.info("Возвращение вещей игроку " + player.getName());
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
        ItemSaver is = GravesPlugin.getInstance().getItemSaver();
        String playerName = event.getPlayer().getName();
        if (is.isExist(playerName)) {
            ItemStack[] items = is.load(playerName);
            event.getPlayer().getInventory().setContents(items);
        }
    }
}
