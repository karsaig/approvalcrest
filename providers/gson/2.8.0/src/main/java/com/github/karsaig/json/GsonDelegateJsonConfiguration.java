package com.github.karsaig.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GsonDelegateJsonConfiguration implements JsonConfiguration {
    private Map<Type, List<Object>> typeAdapters = new HashMap<Type, List<Object>>();

    @Override
    public void addTypeAdapter(@Nullable Type key, @NotNull Object value) {
        if (typeAdapters.get(key) == null) {
            typeAdapters.put(key, new ArrayList<Object>());
            typeAdapters.get(key).add(value);
        } else {
            typeAdapters.get(key).add(value);
        }
    }

    public Map<Type, List<Object>> getTypeAdapters() {
        return typeAdapters;
    }
}
