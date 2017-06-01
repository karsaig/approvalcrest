package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GsonDelegateJsonObject extends GsonDelegateJsonElement implements JsonObject {
    GsonDelegateJsonObject() {
        this(new com.google.gson.JsonObject());
    }

    GsonDelegateJsonObject(com.google.gson.JsonElement delegate) {
        super(delegate);
    }

    @Override
    public @Nullable JsonElement get(@NotNull String field) {
        com.google.gson.JsonElement element = getDelegateAs(com.google.gson.JsonObject.class).get(field);
        return element == null ? null : new GsonDelegateJsonElement(element);
    }

    @Override
    public void add(@NotNull String property, @NotNull JsonElement child) {
        getDelegateAs(com.google.gson.JsonObject.class).add(property, GsonDelegateJsonElement.class.cast(child).delegate);
    }

    @Override
    public void remove(@NotNull String property) {
        getDelegateAs(com.google.gson.JsonObject.class).remove(property);
    }

    @Override
    public @NotNull String toString() {
        return super.toString();
    }
}
