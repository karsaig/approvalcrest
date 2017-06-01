package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;

public class GsonDelegateJson implements Json {
    private final Gson gson;

    public GsonDelegateJson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public @NotNull String toJson(@Nullable Object src) {
        return gson.toJson(unwrapIfPossible(src));
    }

    @Override
    public @NotNull JsonElement toJsonTree(@Nullable Object actual) {
        return new GsonDelegateJsonElement(gson.toJsonTree(unwrapIfPossible(actual)));
    }

    // ========== Helper methods below. ==========

    private @Nullable Object unwrapIfPossible(@Nullable Object src) {
        return GsonDelegateJsonElement.class.isInstance(src) ? GsonDelegateJsonElement.class.cast(src).delegate : src;
    }
}
