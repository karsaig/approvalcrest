package com.github.karsaig.json;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jetbrains.annotations.NotNull;

public class GsonDelegateJsonElement implements JsonElement {
    final com.google.gson.JsonElement delegate;

    GsonDelegateJsonElement(com.google.gson.JsonElement delegate) {
        this.delegate = checkNotNull(delegate, "Delegate cannot be null in %s", delegate.getClass().getCanonicalName());
    }

    @Override
    public boolean isJsonArray() {
        return delegate.isJsonArray();
    }

    @Override
    public boolean isJsonNull() {
        return delegate.isJsonNull();
    }

    @Override public boolean isJsonObject() {
        return delegate.isJsonObject();
    }

    @Override public boolean isJsonPrimitive() {
        return delegate.isJsonPrimitive();
    }

    @Override
    public @NotNull JsonArray getAsJsonArray() {
        return new GsonDelegateJsonArray(delegate.getAsJsonArray());
    }

    @Override
    public @NotNull JsonObject getAsJsonObject() {
        return new GsonDelegateJsonObject(delegate.getAsJsonObject());
    }

    @Override
    public @NotNull String toString() {
        return delegate.toString();
    }

    <T extends com.google.gson.JsonElement> T getDelegateAs(Class<T> type) {
        return type.cast(delegate);
    }
}
