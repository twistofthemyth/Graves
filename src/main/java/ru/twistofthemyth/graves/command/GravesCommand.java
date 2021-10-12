package ru.twistofthemyth.graves.command;

import net.kyori.adventure.util.TriState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.twistofthemyth.graves.Deathpoints;
import ru.twistofthemyth.graves.GravesPlugin;
import ru.twistofthemyth.graves.MessageManager;

import java.util.List;

public class GravesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            if (null != strings && TriState.TRUE.equals(player.permissionValue("graves.list.other"))) {
                sendList(player, strings[0]);
            } else {
                sendList(player, player.getName());
            }
        }
        return false;
    }

    private void sendList(Player sender, String playName) {
        GravesPlugin plugin = GravesPlugin.getInstance();
        MessageManager msg = plugin.msgManager();
        List<Deathpoints.Deathpoint> list = plugin.getDeathpoints().get(playName);
        if (list == null) {
            sender.sendMessage(plugin.msgManager().get("no_death_points"));
        } else {
            for (int i = 0; i < list.size(); i++) {
                sender.sendMessage(msg.get("death_point") + " " + (list.size()-i) + " " + ChatColor.GOLD + list.get(i).toString() + "\n");
            }
        }
    }
}
