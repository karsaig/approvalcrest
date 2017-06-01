package com.github.karsaig.json;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

public interface JsonArray extends JsonElement {
    @NotNull Iterator<JsonElement> iterator();

    void add(@NotNull JsonElement element);
}
