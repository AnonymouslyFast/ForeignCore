package com.anonymouslyfast.foreignCore.listeners;

import com.anonymouslyfast.foreignCore.ForeignCore;
import com.anonymouslyfast.foreignCore.storage.PlayerDataSet;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        // Player's data is already cached.
        if (ForeignCore.getInstance().getStorageManager().getPlayerData(event.getPlayer().getUniqueId()) != null) {
            return;
        }

        // Loading the player into cache
        Bukkit.getAsyncScheduler().runNow(ForeignCore.getInstance(), (scheduledTask ->  {
            ForeignCore.getInstance().getStorageManager().loadPlayerToCache(event.getPlayer());
            PlayerDataSet playerDataSet = ForeignCore.getInstance().getStorageManager().getPlayerData(event.getPlayer().getUniqueId());
            ForeignCore.getInstance().getStorageManager().addToPlayerDataCache(playerDataSet);

            // Test usage of the per player storage
//            PlayerDataSet cachedDataSet = ForeignCore.getInstance().getStorageManager().getPlayerData(event.getPlayer().getUniqueId());
//            if (cachedDataSet == null) {
//                ForeignCore.getInstance().getLogger().warning("Failed to load the player's data!");
//            }
//            if (!cachedDataSet.contains("testValue")) {
//                cachedDataSet.put("testValue", 77777);
//                ForeignCore.getInstance().getLogger().info("Loaded test value!");
//            }
//            Integer storedValue = cachedDataSet.get("testValue", Integer.class);
//            if (storedValue == null) {
//                ForeignCore.getInstance().getLogger().warning("Failed to load test value!");
//                return;
//            }
//            ForeignCore.getInstance().getLogger().info(storedValue.toString());
        }));
    }
}
