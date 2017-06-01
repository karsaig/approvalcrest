package com.github.karsaig.json;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JsonConfiguration {
    void addTypeAdapter(@Nullable Type value, @NotNull Object serializer);

    // TODO: replace Object
    Map<Type, List<Object>> getTypeAdapters();
}
