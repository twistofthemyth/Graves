package ru.twistofthemyth.graves;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.logging.Logger;

public class DeathListener implements Listener {

    private static Logger log = Main.logger;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setCancelled(true);
        try {
            GraveConstructor gc = new GraveConstructor(event.getEntity());
            gc.createGrave();
        } catch (Exception exc) {
            Main.logger.warning(exc.getMessage());
        } finally {
            event.getEntity().getInventory().clear();
            event.getDrops().clear();
            event.setCancelled(false);
        }
    }
}
