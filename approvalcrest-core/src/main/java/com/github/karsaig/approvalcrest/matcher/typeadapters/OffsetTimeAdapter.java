package com.github.karsaig.approvalcrest.matcher.typeadapters;

import java.lang.reflect.Type;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class OffsetTimeAdapter implements JsonSerializer<OffsetTime>, JsonDeserializer<OffsetTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_TIME.withLocale(Locale.ENGLISH);
    public static final Type OFFSET_TIME_TYPE = (new TypeToken<OffsetTime>() {
    }).getType();

    @Override
    public JsonElement serialize(OffsetTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(FORMATTER.format(src));
    }

    @Override
    public OffsetTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return FORMATTER.parse(json.getAsString(), OffsetTime::from);
    }
}
