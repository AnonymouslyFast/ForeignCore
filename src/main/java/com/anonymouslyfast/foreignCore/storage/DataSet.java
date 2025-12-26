package com.anonymouslyfast.foreignCore.storage;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

public interface DataSet {

    /**
     * Gets a stored value from the entries of the dataset.
     * @param key The key phrase, or word that's associated with the value that you want to get.
     * @param clazz Class of the value, if it's not the right class, will throw an exception.
     * @return Returns the value that's associated with the key, will automatically be parsed to the correct datatype.
     * @throws IllegalStateException When the specified class is not the same as the value's object's class.
     */
    <T> @Nullable T get(String key, Class<T> clazz);

    /**
     * Adds the specified key, and value, to the entries map.
     * @param key The key you wish to be correlated with the value.
     * @param value The object you wish to be saved, and correlated with the key.
     * @throws IllegalArgumentException when there's already a key with that name in the entries map.
     */
    void put(String key, Object value);

    void remove(String key);

    boolean contains(String key);

    /**
     * Updates an already existing key in the entries map, with the new value.
     * @param key The key that is correlated with the value you want to update.
     * @param value The updated value.
     * @throws NoSuchElementException when the entries map does not contain the specified key.
     */
    void update(String key, Object value);

}
