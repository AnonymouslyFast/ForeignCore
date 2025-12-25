package com.anonymouslyfast.foreignCore.listeners;

import com.anonymouslyfast.foreignCore.ForeignCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // New player | Saving to database
        if (ForeignCore.getInstance().getStorageManager().getOrLoadPlayerData(player.getUniqueId()) == null) {
            Bukkit.getScheduler().runTaskAsynchronously(ForeignCore.getInstance(), () -> {

            });
        }
    }
}
