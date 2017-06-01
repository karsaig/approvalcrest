package com.github.karsaig.json;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonDelegateJsonElement implements JsonElement {
    private final ObjectMapper mapper;
    final JsonNode delegate;

    public JacksonDelegateJsonElement(JsonNode delegate, @NotNull ObjectMapper mapper) {
        this.delegate = checkNotNull(delegate, "Delegate cannot be null in %s", delegate.getClass().getCanonicalName());
        this.mapper = mapper;
    }

    @Override
    public boolean isJsonArray() {
        return delegate.isArray();
    }

    @Override
    public boolean isJsonNull() {
        return delegate.isNull();
    }

    @Override
    public boolean isJsonObject() {
        return delegate.isObject();
    }

    @Override
    public boolean isJsonPrimitive() {
        return delegate.isTextual() || delegate.isBoolean() || delegate.isNumber();
    }

    @Override
    public @NotNull JsonArray getAsJsonArray() {
        if (!isJsonArray()) {
            throw new IllegalStateException("Not a JsonArray.");
        }
        return new JacksonDelegateJsonArray(delegate, mapper);
    }

    @Override
    public @NotNull JsonObject getAsJsonObject() {
        if (!isJsonObject()) {
            throw new IllegalStateException("Not a JsonObject");
        }
        return new JacksonDelegateJsonObject(delegate, mapper);
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(delegate);
        } catch (JsonProcessingException cause) {
            throw new IllegalStateException(cause);
        }
    }

    // ========== Helper methods below. ==========

    <T extends JsonNode> T getDelegateAs(Class<T> type) {
        return type.cast(delegate);
    }

    ObjectMapper getMapper() {
        return mapper;
    }
}
