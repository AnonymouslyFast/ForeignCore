package com.anonymouslyfast.foreignCore.storage;

public interface DataSet {
    Object get(String key);
    void put(String key, Object value);
    void remove(String key);
    boolean contains(String key);
    void update(String key, Object value);
}
