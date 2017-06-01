package com.github.karsaig.json;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperHolder {
    private static ObjectMapper createInitializedInstance() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(ALLOW_COMMENTS);
        mapper.enable(ALLOW_UNQUOTED_FIELD_NAMES);
        return mapper;
    }

    private static final ObjectMapper INSTANCE = createInitializedInstance();

    public static ObjectMapper getInstance() {
        return INSTANCE;
    }
}
