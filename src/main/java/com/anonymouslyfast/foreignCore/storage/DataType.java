package com.anonymouslyfast.foreignCore.storage;

public interface DataType<T> {
    String id();
    Class<T> clazz();
    String serialize(T value);
    T deserialize(String value);
}
