package com.anonymouslyfast.foreignCore.storage;

import com.anonymouslyfast.foreignCore.ForeignCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.*;
import java.time.Instant;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StorageManager {

    private final Map<UUID, PlayerDataSet> playerDataSets = new ConcurrentHashMap<>();
    private final Map<String, PluginDataSet> pluginDataSets = new ConcurrentHashMap<>();

    private final DataBaseManger dataBaseManger;
    private final DataTypeManager dataTypeManager;

    private final Logger logger;

    public StorageManager(String dbFile_path) {
        logger = ForeignCore.getInstance().getLogger();
        this.dataTypeManager = new DataTypeManager();
        this.dataBaseManger = new DataBaseManger(dbFile_path);
        dataBaseManger.registerTable("players", Tables.PLAYERS_TABLE);
        dataBaseManger.registerTable("player_data", Tables.PLAYER_DATA_TABLE);
        dataBaseManger.registerTable("plugin_data", Tables.PLUGIN_DATA_TABLE);
        dataBaseManger.loadTables();
        loadAllPluginData();
    }

    /**
     * Only use when there's a possibility that the player has not been registered to databases.
     * @apiNote Only use asynchronously.
     * @return cached, or newly loaded/created player data. Only returns null, if sql has encountered an error.
     */
    public @Nullable PlayerDataSet getOrLoadPlayerData(UUID uuid) {
        return playerDataSets.computeIfAbsent(uuid, this::loadPlayerData);
    }

    public @Nullable PlayerDataSet getPlayerData(UUID uuid) {
        return playerDataSets.get(uuid);
    }

    public @Nullable PluginDataSet getPluginDataSet(String pluginName) {
        return pluginDataSets.get(pluginName);
    }

    public Map<UUID, PlayerDataSet> getPlayerDataSets() {
        return Collections.unmodifiableMap(playerDataSets);
    }

    public Map<String, PluginDataSet> getPluginDataSets() {
        return Collections.unmodifiableMap(pluginDataSets);
    }

    public void addToPlayerDataCache(PlayerDataSet playerDataSet) {
        playerDataSets.put(playerDataSet.getUUID(), playerDataSet);
    }

    /**
     * @apiNote Doesn't auto save to db, if you use this, the data not saved will be lost.
     */
    public void remveFromPlayerCache(UUID uuid) {
        playerDataSets.remove(uuid);
    }

    public void addToPluginDataCache(PluginDataSet pluginDataSet) {
        pluginDataSets.put(pluginDataSet.getPluginName(), pluginDataSet);
    }

    public boolean playerIsRegistered(UUID uuid) {
        String sql = "SELECT 1 FROM players WHERE uuid = ?";

        try (PreparedStatement ps = dataBaseManger.getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next(); // There's no row, so there's no player in the db with the uuid.
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to check player existence: " + uuid, e);
            return false;
        }
    }

    /**
     * Loads the player to cache, will do nothing if the player already is in cache.
     * @apiNote Only use asynchronously.
     */
    public void loadPlayerToCache(Player player) {

        if (!playerIsRegistered(player.getUniqueId())) {
            String sql = """
            INSERT INTO players (uuid, username, initial_ip, joined_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(uuid) DO NOTHING
           """;

            try (PreparedStatement preparedStatement = dataBaseManger.getConnection().prepareStatement(sql)) {
                String ip = "0.0.0.0";
                if (player.getAddress() != null) { ip = player.getAddress().getAddress().toString(); }

                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.setString(2, player.getName());
                preparedStatement.setString(3, ip);
                preparedStatement.setLong(4, Instant.now().toEpochMilli());
                preparedStatement.execute();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to register player " + player.getUniqueId(), e);
            }
        }

        // Loading, and caching playerdata
        getOrLoadPlayerData(player.getUniqueId());
    }


    private @Nullable PlayerDataSet loadPlayerData(UUID uuid) {
        PlayerDataSet playerDataSet = new PlayerDataSet(uuid);

        String sql = "SELECT * FROM player_data WHERE uuid = ?";
        try (PreparedStatement statement = dataBaseManger.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String key = resultSet.getString("key");
                    String value = resultSet.getString("value");
                    String type = resultSet.getString("type");

                    Object deserializedValue = dataTypeManager.deserialize(value, type);

                    playerDataSet.put(key, deserializedValue);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to load player data for " + uuid, e);
            return null;
        }

        logger.info("PLAYER_DATA: Successfully loaded " + playerDataSet.getEntries().size() + " item[s] for " + uuid);
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
                    logger.log(Level.WARNING,
                            "The DataType: " + dataType.id() + " is not a valid type, therefore " + entry.getValue()
                                    + " is not saved for the player " + Bukkit.getPlayer(playerDataSet.getUUID())
                    );
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
            logger.log(Level.WARNING, "Failed to save " + Bukkit.getOfflinePlayer(playerDataSet.getUUID()).getName() + "'s data", e);
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

                    Object deserializedValue = dataTypeManager.deserialize(value, type);

                    PluginDataSet pluginDataSet = getPluginDataSet(pluginName);
                    if (pluginDataSet == null) {
                        pluginDataSet = new PluginDataSet(pluginName);
                        pluginDataSets.put(pluginName, pluginDataSet);
                    }
                    pluginDataSet.put(key, deserializedValue);
                }
            }
            logger.info("Successfully cached " + pluginDataSets.size() + " plugin's data!");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to load plugin data", e);
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
                DataType<?> dataType = dataTypeManager.getDataType(entry.getValue().getClass());
                if (dataType == null) {
                    logger.log(Level.WARNING,
                            "The DataType: " + entry.getValue().getClass() + " is not a valid type, therefore " + entry.getValue()
                                    + " is not saved for the plugin " + pluginDataSet.getPluginName()
                    );
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
            logger.log(Level.WARNING, "Failed to save " + pluginDataSet.getPluginName() + "'s data", e);
        }
    }
}
