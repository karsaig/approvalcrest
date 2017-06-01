package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonDelegateJsonFactory implements JsonFactory {
    private static final ObjectMapper DELEGATE = ObjectMapperHolder.getInstance();

    @Override
    public @NotNull JsonArray createJsonArray() {
        return new JacksonDelegateJsonArray(DELEGATE.createArrayNode(), DELEGATE);
    }

    @Override
    public @NotNull JsonObject createJsonObject() {
        return new JacksonDelegateJsonObject(DELEGATE.createObjectNode(), DELEGATE);
    }
}
