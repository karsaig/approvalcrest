package com.github.karsaig.approvalcrest.matcher.typeadapters;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ClassAdapter extends TypeAdapter<Class> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return Class.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new ClassAdapter() : null;
        }
    };

    @Override
    public void write(JsonWriter out, Class value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.getName());
        }
    }

    @Override
    public Class read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException("Only for serialization!");
    }
}
