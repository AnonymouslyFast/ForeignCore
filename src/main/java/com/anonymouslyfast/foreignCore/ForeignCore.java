package com.anonymouslyfast.foreignCore;

import com.anonymouslyfast.foreignCore.listeners.PlayerJoinListener;
import com.anonymouslyfast.foreignCore.storage.DataTypeManager;
import com.anonymouslyfast.foreignCore.storage.PluginDataSet;
import com.anonymouslyfast.foreignCore.storage.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


public final class ForeignCore extends JavaPlugin {

    private final String DB_FILE_PATH = getDataFolder() + "/database.db";
    private final long AUTO_SAVE_DELAY = 60; // In minutes
    private final long AUTO_SAVE_INITIAL_DELAY = 1;

    private static ForeignCore instance = null;
    private StorageManager storageManager;

    private PluginDataSet pluginDataSet;

    @Override
    public void onEnable() {
        instance = this;
        storageManager = new StorageManager(DB_FILE_PATH);

        // Loading / Setting PluginDataSet
        pluginDataSet = storageManager.getPluginDataSet(getPluginMeta().getName().toLowerCase());
        if (pluginDataSet == null) {
            pluginDataSet = new PluginDataSet(getPluginMeta().getName().toLowerCase());
            storageManager.addToPluginDataCache(pluginDataSet);
        }

        // Test of plugin data usage
        //        if (!pluginDataSet.contains("testValue")) {
//            pluginDataSet.put("testValue", 80085);
//            getLogger().info("Set the test value!");
//        }
//
//        getLogger().info("TEST VALUE: " + pluginDataSet.get("testValue", Integer.class));

        registerListeners();


        // Auto save and clean up cache
        Bukkit.getAsyncScheduler().runAtFixedRate(ForeignCore.getInstance(), task -> {
            saveAllStorage();
            // Cleaning up cache
            for (UUID uuid : storageManager.getPlayerDataSets().keySet()) {
                Bukkit.getScheduler().runTask(this, () -> {
                    if (!Bukkit.getOfflinePlayer(uuid).isOnline()) {
                        storageManager.remveFromPlayerCache(uuid);
                    }
                });
            }
            getLogger().info("DATABASE AUTOSAVE: Cache has been saved, and cleaned up!");
        }, AUTO_SAVE_INITIAL_DELAY, AUTO_SAVE_DELAY, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable() {
        saveAllStorage();
    }

    private void saveAllStorage() {
        //Saving Global storage
        storageManager.getPluginDataSets().values().forEach(pluginDataSet -> {
            storageManager.savePluginData(pluginDataSet);
        });

        //Saving cached player storage
        storageManager.getPlayerDataSets().values().forEach(playerDataSet -> {
            storageManager.savePlayerData(playerDataSet);
        });
    }

    public static ForeignCore getInstance() {
        if (instance == null) throw new IllegalStateException("Plugin Instance is null");
        return instance;
    }
    public StorageManager getStorageManager() { return storageManager; }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }
}
