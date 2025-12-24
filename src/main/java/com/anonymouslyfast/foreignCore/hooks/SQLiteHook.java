package com.anonymouslyfast.foreignCore.hooks;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteHook {

    private Connection connection;
    private final File dbFile;

    public SQLiteHook(String dbPath) {
        dbFile = new File(dbPath);
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdir();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
        } catch (SQLException e) {
            //TODO: change to logger stuff.
            e.printStackTrace();
        }
    }

    public Connection getConnection() { return connection; }
    public File getDbFile() { return dbFile; }

}
