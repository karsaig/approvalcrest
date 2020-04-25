package com.github.karsaig.approvalcrest.matcher.typeadapters;

import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class PathTypeAdapter extends TypeAdapter<Path> {

    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            return Path.class.isAssignableFrom(typeToken.getRawType()) ? (TypeAdapter<T>) new PathTypeAdapter() : null;
        }
    };

    @Override
    public void write(JsonWriter out, Path value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toString());
        }
    }

    @Override
    public Path read(JsonReader in) {
        throw new UnsupportedOperationException("Only for serialization!");
    }
}
