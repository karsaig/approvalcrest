package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JsonObject extends JsonElement {
    @Nullable JsonElement get(@NotNull String field);

    void add(@NotNull String property, @NotNull JsonElement child);

    void remove(@NotNull String property);
}
