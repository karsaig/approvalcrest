package com.github.karsaig.approvalcrest.matcher.typeadapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

public abstract class AbstractTemporalAdapter<T extends TemporalAccessor>
        implements JsonSerializer<T>, JsonDeserializer<T> {

    private final DateTimeFormatter formatter;
    private final TemporalQuery<T> query;

    protected AbstractTemporalAdapter(DateTimeFormatter formatter, TemporalQuery<T> query) {
        this.formatter = formatter;
        this.query = query;
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(src));
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return formatter.parse(json.getAsString(), query);
    }
}
