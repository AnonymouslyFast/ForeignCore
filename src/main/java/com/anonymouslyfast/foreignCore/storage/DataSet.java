package com.anonymouslyfast.foreignCore.storage;

import javax.annotation.Nullable;

public interface DataSet {
    <T> @Nullable T get(String key, Class<T> clazz);
    void put(String key, Object value);
    void remove(String key);
    boolean contains(String key);
    void update(String key, Object value);

}
