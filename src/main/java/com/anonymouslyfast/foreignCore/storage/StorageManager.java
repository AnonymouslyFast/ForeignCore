package com.anonymouslyfast.foreignCore.storage;

import com.anonymouslyfast.foreignCore.hooks.SQLiteHook;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class StorageManager {

    private final HashMap<UUID, PlayerDataSet> playerDataSets = new HashMap<>();
    private final GlobalDataSet globalDataSet = new GlobalDataSet();

    private final DataBaseManger dataBaseManger;
    private final DataTypeManager dataTypeManager;

    public StorageManager(String dbFile_path) {
        this.dataBaseManger = new DataBaseManger(dbFile_path);
        this.dataTypeManager = new DataTypeManager();
        dataBaseManger.registerTable("players", Tables.PLAYERS_TABLE);
        dataBaseManger.registerTable("player_data", Tables.PLAYER_DATA_TABLE);
    }

    public GlobalDataSet getGlobalDataSet() {
        return globalDataSet;
    }

    public HashMap<UUID, PlayerDataSet> getPlayerDataSets() {
        return playerDataSets;
    }

    public PlayerDataSet getPlayerDataSet(UUID uuid) {
        return playerDataSets.get(uuid);
    }

    public void loadPlayerData(UUID uuid) {
        
    }
}
