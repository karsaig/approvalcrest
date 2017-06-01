package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;

public interface JsonParser {
    @NotNull JsonElement parse(@NotNull String json);
}
