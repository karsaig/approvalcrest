package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;

public class GsonDelegateJsonParser implements JsonParser {
    private final com.google.gson.JsonParser parser;

    public GsonDelegateJsonParser() {
        this(new com.google.gson.JsonParser());
    }

    private GsonDelegateJsonParser(com.google.gson.JsonParser parser) {
        this.parser = parser;
    }

    @Override
    public @NotNull JsonElement parse(@NotNull String json) {
        return new GsonDelegateJsonElement(parser.parse(json));
    }
}
