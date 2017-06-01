package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;

public interface JsonElement {
    boolean isJsonArray();

    @NotNull JsonArray getAsJsonArray();

    boolean isJsonNull();

    @NotNull JsonObject getAsJsonObject();

    boolean isJsonObject();

    boolean isJsonPrimitive();

    /**
     * <pre>
     * You must implement this. Advised to use output of delegate.
     * <b>Warning</b>: not implementing this method might cause non deterministic behaviour.
     * </pre>
     */
    @NotNull String toString();
}
