package com.anonymouslyfast.foreignCore;

import com.anonymouslyfast.foreignCore.storage.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class ForeignCore extends JavaPlugin {

    private final String DB_FILE_PATH = getDataFolder() + "/database.db";

    private static ForeignCore instance = null;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        instance = this;
        storageManager = new StorageManager(DB_FILE_PATH);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ForeignCore getInstance() {
        if (instance == null) throw new IllegalStateException("Plugin Instance is null");
        return instance;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}
