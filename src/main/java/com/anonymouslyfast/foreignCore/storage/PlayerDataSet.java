package com.anonymouslyfast.foreignCore.storage;

import com.anonymouslyfast.foreignCore.ForeignCore;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.UUID;

public class PlayerDataSet implements DataSet {

    private final HashMap<String, Object> entries = new HashMap<>();
    private final UUID uuid;

    public PlayerDataSet(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(String key, Class<T> clazz) {
        Object result = entries.get(key);
        if (result == null) return null;

        if (!clazz.isInstance(result)) {
            throw new IllegalStateException(
                    "Data type mismatch for key '" + key +
                            "'. Expected " + clazz.getName() +
                            " but found " + result.getClass().getName()
            );
        }
        return (T) result;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public void put(String key, Object value) {
        if (entries.containsKey(key)) throw new IllegalArgumentException("Duplicate key: " + key);
        entries.put(key, value);
    }

    @Override
    public void remove(String key) {
        entries.remove(key);
    }

    @Override
    public boolean contains(String key) {
        return entries.containsKey(key);
    }

    @Override
    public void update(String key, Object value) {
        if (!contains(key)) throw new NoSuchElementException("Key does not exist");
        entries.put(key, value);
    }

    public HashMap<String, Object> getEntries() {
        return entries;
    }

}
