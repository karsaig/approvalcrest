package com.github.karsaig.json;

public class GsonDelegateJsonParserProvider implements JsonParserProvider {
    @Override
    public JsonParser jsonParser() {
        return new GsonDelegateJsonParser();
    }
}
