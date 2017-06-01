package com.github.karsaig.json;

import com.github.karsaig.ServiceLoaders;

public class JsonFactoryProvider {
    public static JsonFactory jsonFactory() {
        return ServiceLoaders.loadService(JsonFactory.class);
    }
}
