package com.github.karsaig.json;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

public class GsonDelegateJsonArray extends GsonDelegateJsonElement implements JsonArray {
    public GsonDelegateJsonArray() {
        this(new com.google.gson.JsonArray());
    }

    GsonDelegateJsonArray(com.google.gson.JsonArray delegate) {
        super(delegate);
    }

    @Override
    public @NotNull Iterator<JsonElement> iterator() {
        final Iterator<com.google.gson.JsonElement> delegateAs = getDelegateAs(com.google.gson.JsonArray.class).iterator();

        return new Iterator<JsonElement>() {
            @Override public boolean hasNext() {
                return delegateAs.hasNext();
            }

            @Override public JsonElement next() {
                return new GsonDelegateJsonElement(delegateAs.next());
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void add(@NotNull JsonElement element) {
        getDelegateAs(com.google.gson.JsonArray.class).add(GsonDelegateJsonElement.class.cast(element).delegate);
    }

    @Override
    public @NotNull String toString() {
        return super.toString();
    }
}
