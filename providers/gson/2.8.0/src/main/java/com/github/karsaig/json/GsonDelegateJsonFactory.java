package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;

public class GsonDelegateJsonFactory implements JsonFactory {
    @Override
    public @NotNull JsonArray createJsonArray() {
        return new GsonDelegateJsonArray();
    }

    @Override
    public @NotNull JsonObject createJsonObject() {
        return new GsonDelegateJsonObject();
    }
}
