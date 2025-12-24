package com.anonymouslyfast.foreignCore.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {

    private final Map<UUID, PlayerDataSet> playerDataSets = new ConcurrentHashMap<>();
    private final Map<String, PluginDataSet> pluginDataSets = new ConcurrentHashMap<>();

    private final DataBaseManger dataBaseManger;
    private final DataTypeManager dataTypeManager;

    public StorageManager(String dbFile_path) {
        this.dataBaseManger = new DataBaseManger(dbFile_path);
        this.dataTypeManager = new DataTypeManager();
        dataBaseManger.registerTable("players", Tables.PLAYERS_TABLE);
        dataBaseManger.registerTable("player_data", Tables.PLAYER_DATA_TABLE);
        dataBaseManger.registerTable("plugin_data", Tables.PLUGIN_DATA_TABLE);
        loadAllPluginData();
    }

    public PlayerDataSet getOrLoadPlayerData(UUID uuid) {
        return playerDataSets.computeIfAbsent(uuid, this::loadPlayerData);
    }

    public boolean cacheContainsPlayer(UUID uuid) {
        return playerDataSets.containsKey(uuid);
    }

    public PluginDataSet getPluginDataSet(String pluginName) {
        return pluginDataSets.get(pluginName);
    }

    private PlayerDataSet loadPlayerData(UUID uuid) {
        PlayerDataSet playerDataSet = new PlayerDataSet(uuid);

        String sql = "SELECT * FROM player_data WHERE uuid = ?";
        try (PreparedStatement statement = dataBaseManger.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String key = resultSet.getString("key");
                    String value = resultSet.getString("value");
                    String type = resultSet.getString("type");

                    Object deserializedValue = dataTypeManager.deserialize(type, value);

                    playerDataSet.put(key, deserializedValue);
                }
            }

        } catch (SQLException e) {
            //TODO: replace with own logger stuff.
            e.printStackTrace();
        }

        playerDataSets.put(uuid, playerDataSet);
        return playerDataSet;
    }

    public void savePlayerData(PlayerDataSet playerDataSet) {

        String sql = """
            INSERT INTO player_data (uuid, `key`, value, type)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(uuid, `key`) DO UPDATE SET
                value = excluded.value,
                type = excluded.type
           """;

        try (PreparedStatement statement = dataBaseManger.getConnection().prepareStatement(sql)) {
            statement.setString(1, playerDataSet.getUUID().toString().toLowerCase(Locale.ROOT));
            for (Map.Entry<String, Object> entry : playerDataSet.getEntries().entrySet()) {
                DataType<?> dataType =  dataTypeManager.getDataType(entry.getValue().getClass());
                if (dataType == null) {
                    //TODO: Log that this is result of no valid datatype, resulting in data loss.
                    continue;
                }
                String serializedValue = dataType.serializeAny(entry.getValue());
                statement.setString(2, entry.getKey());
                statement.setString(3, serializedValue);
                statement.setString(4, dataType.id());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            //TODO: replace with own logger stuff.
            e.printStackTrace();
        }
    }

    public void savePlayerData(UUID uuid) { savePlayerData(getOrLoadPlayerData(uuid)); }

    private void loadAllPluginData() {
        String sql = "SELECT * FROM plugin_data";
        try (PreparedStatement statement = dataBaseManger.getConnection().prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String pluginName = resultSet.getString("plugin_name");
                    String key = resultSet.getString("key");
                    String value = resultSet.getString("value");
                    String type = resultSet.getString("type");

                    Object deserializedValue = dataTypeManager.deserialize(type, value);

                    PluginDataSet pluginDataSet = getPluginDataSet(pluginName);
                    if (pluginDataSet == null) {
                        pluginDataSet = new PluginDataSet(pluginName);
                        pluginDataSets.put(pluginName, pluginDataSet);
                    }
                    pluginDataSet.put(key, deserializedValue);
                }
            }

        } catch (SQLException e) {
            //TODO: replace with own logger stuff.
            e.printStackTrace();
        }
    }

    public void savePluginData(PluginDataSet pluginDataSet) {
        String sql = """
            INSERT INTO plugin_data (plugin_name, `key`, value, type)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(plugin_name, `key`) DO UPDATE SET
                value = excluded.value,
                type = excluded.type
           """;

        try (PreparedStatement statement = dataBaseManger.getConnection().prepareStatement(sql)) {
            statement.setString(1, pluginDataSet.getPluginName());
            for (Map.Entry<String, Object> entry : pluginDataSet.getEntries().entrySet()) {
                DataType<?> dataType =  dataTypeManager.getDataType(entry.getValue().getClass());
                if (dataType == null) {
                    //TODO: Log that this is result of no valid datatype, resulting in data loss.
                    continue;
                }
                String serializedValue = dataType.serializeAny(entry.getValue());
                statement.setString(2, entry.getKey());
                statement.setString(3, serializedValue);
                statement.setString(4, dataType.id());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            //TODO: replace with own logger stuff.
            e.printStackTrace();
        }
    }
}
