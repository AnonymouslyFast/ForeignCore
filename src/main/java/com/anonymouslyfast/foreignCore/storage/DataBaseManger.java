package com.anonymouslyfast.foreignCore.storage;

import com.anonymouslyfast.foreignCore.hooks.SQLiteHook;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class DataBaseManger {

    private final HashMap<String, String> tables = new HashMap<>();
    private final SQLiteHook hook;

    public DataBaseManger(String dbFilePath) {
        hook = new SQLiteHook(dbFilePath);
    }

    public void loadTables() {
        tables.forEach((tableName, tableQuery) -> {
            try {
                Statement statement = hook.getConnection().createStatement();
                statement.executeUpdate(tableQuery);
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void registerTable(String tableName, String tableQuery) {
        tables.put(tableName, tableQuery);
    }

    public void unregisterTable(String tableName) {
        tables.remove(tableName);
    }

    public Connection getConnection() {
        return hook.getConnection();
    }

}
