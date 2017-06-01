package com.github.karsaig.json;

import static com.github.karsaig.ServiceLoaders.loadService;

import org.jetbrains.annotations.NotNull;

public class JsonParserFactory {
    public static @NotNull JsonParser jsonParser() {
        return loadService(JsonParserProvider.class).jsonParser();
    }
}
