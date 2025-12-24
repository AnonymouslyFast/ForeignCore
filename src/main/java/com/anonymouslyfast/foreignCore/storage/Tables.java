package com.anonymouslyfast.foreignCore.storage;

public final class Tables {

    public static final String PLAYERS_TABLE = """
        CREATE TABLE IF NOT EXISTS players (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            uuid VARCHAR(36) NOT NULL UNIQUE,
            username TEXT,
            initial_ip TEXT,
            joined_at TIMESTAMP NOT NULL
        );
    """;

    public static final String PLAYER_DATA_TABLE = """
        CREATE TABLE IF NOT EXISTS player_data (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            uuid VARCHAR(36) NOT NULL,
            key TEXT,
            UNIQUE (uuid, key)
        );
    """;

}
