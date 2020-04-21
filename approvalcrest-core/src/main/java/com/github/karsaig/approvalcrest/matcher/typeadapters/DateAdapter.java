package com.github.karsaig.approvalcrest.matcher.typeadapters;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class DateAdapter extends TypeAdapter<Date> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withLocale(Locale.ENGLISH).withZone(ZoneId.of("UTC"));

    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            return Date.class.isAssignableFrom(typeToken.getRawType()) ? (TypeAdapter<T>) new DateAdapter() : null;
        }
    };

    @Override
    public void write(JsonWriter jsonWriter, Date date) throws IOException {
        if (date == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(FORMATTER.format(date.toInstant()));
        }
    }

    @Override
    public Date read(JsonReader jsonReader) throws IOException {
        throw new UnsupportedOperationException("Only for serialization!");
    }
}
