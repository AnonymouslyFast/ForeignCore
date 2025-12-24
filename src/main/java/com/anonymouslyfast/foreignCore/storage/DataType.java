package com.anonymouslyfast.foreignCore.storage;

public interface DataType<T> {
    String id();
    Class<T> clazz();
    String serialize(T value);
    T deserialize(String value);

    @SuppressWarnings("unchecked")
    default String serializeAny(Object value) {
        if (!clazz().isInstance(value)) {
            throw new IllegalArgumentException(
                    "Invalid value type. Expected " + clazz().getName()
                            + " but got " + value.getClass().getName()
            );
        }
        return serialize((T) value);
    }
}
