package com.anonymouslyfast.foreignCore.storage;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class DataTypeManager {

    private final HashMap<String, DataType<?>> dataTypeByID = new HashMap<>();
    private final HashMap<Class<?>, DataType<?>> dataTypeByClass = new HashMap<>();

    public DataTypeManager() {
        registerDefautDataTypes();
    }

    public <T> void registerDataType(DataType<T> dataType) {
        dataTypeByID.put(dataType.id(), dataType);
        dataTypeByClass.put(dataType.clazz(), dataType);
    }

    public <T> DataType<T> getDataType(Class<T> clazz) {
        return (DataType<T>) dataTypeByID.get(clazz);
    }

    public <T> DataType<T> getDataType(String id) {
        return (DataType<T>) dataTypeByID.get(id);
    }

    public <T> String serialize(T value) {
        DataType<T> dataType = (DataType<T>) dataTypeByClass.get(value.getClass());
        if (dataType == null) throw new NoSuchElementException("No datatype found for " + value.getClass());
        return dataType.serialize(value);
    }

    public <T> T deserialize(String value) {
        DataType<T> dataType = (DataType<T>) dataTypeByClass.get(value);
        if (dataType == null) throw new NoSuchElementException("No datatype found for " + value.getClass());
        return dataType.deserialize(value);
    }

    private void registerDefautDataTypes() {
        registerDataType(new DataType<Integer>() {
            @Override public String id() { return "integer"; }
            @Override public Class<Integer> clazz() { return Integer.class; }
            @Override public String serialize(Integer value) { return value.toString(); }
            @Override public Integer deserialize(String value) { return Integer.parseInt(value); }
        });

        registerDataType(new DataType<Double>() {
            @Override public String id() { return "double"; }
            @Override public Class<Double> clazz() { return Double.class; }
            @Override public String serialize(Double value) { return value.toString(); }
            @Override public Double deserialize(String value) { return Double.parseDouble(value); }
        });

        registerDataType(new DataType<Long>() {
            @Override public String id() { return "long"; }
            @Override public Class<Long> clazz() { return Long.class; }
            @Override public String serialize(Long value) { return value.toString(); }
            @Override public Long deserialize(String value) { return Long.parseLong(value); }
        });

        registerDataType(new DataType<Boolean>() {
            @Override public String id() { return "boolean"; }
            @Override public Class<Boolean> clazz() { return Boolean.class; }
            @Override public String serialize(Boolean value) { return value.toString(); }
            @Override public Boolean deserialize(String value) { return Boolean.parseBoolean(value); }
        });
    }
}
