package com.github.karsaig.approvalcrest.matcher.typeadapters;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public abstract class CustomizedTypeAdapterFactory<C> implements TypeAdapterFactory {
    private final Class<C> customizedClass;

    public CustomizedTypeAdapterFactory(Class<C> customizedClass) {
        this.customizedClass = customizedClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return customizedClass.isAssignableFrom(type.getRawType())
                ? (TypeAdapter<T>) customizeClassAdapter(gson, (TypeToken<C>) type)
                : null;
    }

    private TypeAdapter<C> customizeClassAdapter(Gson gson, TypeToken<C> type) {
        TypeAdapter<C> delegate = gson.getDelegateAdapter(this, type);
        TypeAdapter<JsonElement> adapter = gson.getAdapter(JsonElement.class);
        return new TypeAdapter<C>() {
            @Override
            public void write(JsonWriter out, C value) throws IOException {
                JsonElement tree = delegate.toJsonTree(value);
                beforeWrite(value, tree);
                adapter.write(out, tree);
            }

            @Override
            public C read(JsonReader in) throws IOException {
                JsonElement tree = adapter.read(in);
                afterRead(tree);
                return delegate.fromJsonTree(tree);
            }
        };
    }

    /**
     * Override this to muck with {@code toSerialize} before it is written to
     * the outgoing JSON stream.
     */
    protected void beforeWrite(C source, JsonElement toSerialize) {
    }

    /**
     * Override this to muck with {@code deserialized} before it parsed into
     * the application type.
     */
    protected void afterRead(JsonElement deserialized) {
    }
}
