package com.github.karsaig.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonDelegateJsonParserProvider implements JsonParserProvider {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperHolder.getInstance();

    @Override
    public JsonParser jsonParser() {
        return new JacksonDelegateJsonParser(OBJECT_MAPPER);
    }
}
