package com.github.karsaig.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonDelegateJsonObject extends JacksonDelegateJsonElement implements JsonObject {
    public JacksonDelegateJsonObject(ObjectMapper mapper) {
        this(mapper.createObjectNode(), mapper);
    }

    JacksonDelegateJsonObject(JsonNode delegate, @NotNull ObjectMapper mapper) {
        super(delegate, mapper);
    }

    @Override
    public @Nullable JsonElement get(@NotNull String field) {
        return new JacksonDelegateJsonElement(getDelegateAs(JsonNode.class).get(field), getMapper());
    }

    @Override
    public void add(@NotNull String property, @NotNull JsonElement child) {
        getDelegateAs(ObjectNode.class).set(property, JacksonDelegateJsonElement.class.cast(child).delegate);
    }

    @Override
    public void remove(@NotNull String property) {
        getDelegateAs(ObjectNode.class).remove(property);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
