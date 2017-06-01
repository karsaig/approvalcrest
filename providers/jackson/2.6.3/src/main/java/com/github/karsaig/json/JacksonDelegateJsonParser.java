package com.github.karsaig.json;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonDelegateJsonParser implements JsonParser {
    private final ObjectMapper delegate;

    public JacksonDelegateJsonParser(ObjectMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull JsonElement parse(@NotNull String json) {
        try {
            return new JacksonDelegateJsonElement(delegate.readTree(json), delegate);
        } catch (IOException cause) {
            throw new IllegalStateException(cause);
        }
    }
}
