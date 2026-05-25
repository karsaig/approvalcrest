package com.github.karsaig.approvalcrest.matcher.typeadapters;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocalTimeAdapter extends AbstractTemporalAdapter<LocalTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.of("UTC")).withLocale(Locale.ENGLISH);
    public static final Type LOCAL_TIME_TYPE = (new TypeToken<LocalTime>() {
    }).getType();

    public LocalTimeAdapter() {
        super(FORMATTER, LocalTime::from);
    }
}
