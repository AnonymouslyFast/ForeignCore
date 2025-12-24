package com.anonymouslyfast.foreignCore.storage;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class GlobalDataSet implements DataSet {
    private final HashMap<String, Object> entries = new HashMap<>();

    @Override
    public Object get(String key) {
        return entries.get(key);
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
}
