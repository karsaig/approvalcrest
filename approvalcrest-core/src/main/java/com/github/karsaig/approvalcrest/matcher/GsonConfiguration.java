package com.github.karsaig.approvalcrest.matcher;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.TypeAdapterFactory;

/**
 * Configuration file for {@link GsonProvider}.
 *
 * @author Andras_Gyuro
 */
public class GsonConfiguration {

    private List<TypeAdapterFactory> typeAdapterFactories = new ArrayList<>();
    private Map<Type, List<Object>> typeAdapters = new HashMap<>();
    private Map<Class<?>, List<Object>> typeHierarchyAdapter = new HashMap<>();

    public void addTypeAdapterFactory(TypeAdapterFactory factory) {
        typeAdapterFactories.add(factory);
    }

    public void addTypeAdapter(Type key, Object value) {
        if (typeAdapters.get(key) == null) {
            typeAdapters.put(key, new ArrayList<>());
            typeAdapters.get(key).add(value);
        } else {
            typeAdapters.get(key).add(value);
        }
    }

    public void addTypeHierarchyAdapter(Class<?> key, Object value) {
        if (typeHierarchyAdapter.get(key) == null) {
            typeHierarchyAdapter.put(key, new ArrayList<>());
            typeHierarchyAdapter.get(key).add(value);
        } else {
            typeHierarchyAdapter.get(key).add(value);
        }
    }

    public List<TypeAdapterFactory> getTypeAdapterFactories() {
        return typeAdapterFactories;
    }

    public Map<Type, List<Object>> getTypeAdapters() {
        return typeAdapters;
    }

    public Map<Class<?>, List<Object>> getTypeHierarchyAdapter() {
        return typeHierarchyAdapter;
    }

}
