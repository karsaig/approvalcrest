package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;

public interface JsonFactory {
    @NotNull JsonArray createJsonArray();

    @NotNull JsonObject createJsonObject();
}
