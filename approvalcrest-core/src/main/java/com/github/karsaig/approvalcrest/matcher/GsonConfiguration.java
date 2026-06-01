package com.github.karsaig.approvalcrest.matcher;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Set<Class<?>> typesToSkipInFallbackFactories = new HashSet<>();

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

    /**
     * Registers a type that the fallback serialization factories (UnsafeFieldTypeAdapterFactory
     * and GetterBasedTypeAdapterFactory) should skip.
     * <p>
     * Use this when you register a custom type adapter or type hierarchy adapter for a type
     * that resides in a named/locked module (e.g., Vavr's {@code io.vavr.control.Option}).
     * Without this, the fallback factories may claim the type before your custom adapter
     * gets a chance to handle it.
     * <p>
     * Types are matched using {@code isAssignableFrom}, so registering a base class or
     * interface will also skip all subtypes.
     *
     * @param type the class to skip in fallback factories
     */
    public void addTypeToSkipInFallbackFactories(Class<?> type) {
        typesToSkipInFallbackFactories.add(type);
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

    public Set<Class<?>> getTypesToSkipInFallbackFactories() {
        return Collections.unmodifiableSet(typesToSkipInFallbackFactories);
    }

}
