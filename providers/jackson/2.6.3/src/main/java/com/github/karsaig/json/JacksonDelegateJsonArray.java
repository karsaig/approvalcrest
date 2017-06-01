package com.github.karsaig.json;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JacksonDelegateJsonArray extends JacksonDelegateJsonElement implements JsonArray {

    public JacksonDelegateJsonArray(ObjectMapper mapper) {
        this(mapper.createArrayNode(), mapper);
    }

    JacksonDelegateJsonArray(JsonNode delegate, ObjectMapper mapper) {
        super(delegate, mapper);
    }

    @Override
    public @NotNull Iterator<JsonElement> iterator() {
        final Iterator<JsonNode> items = getDelegateAs(JsonNode.class).iterator();

        return new Iterator<JsonElement>() {
            @Override public boolean hasNext() {
                return items.hasNext();
            }

            @Override public JsonElement next() {
                return new JacksonDelegateJsonElement(items.next(), getMapper());
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void add(@NotNull JsonElement element) {
        getDelegateAs(ArrayNode.class).add(JacksonDelegateJsonElement.class.cast(element).delegate);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
