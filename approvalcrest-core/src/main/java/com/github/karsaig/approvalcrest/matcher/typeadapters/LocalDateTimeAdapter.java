package com.github.karsaig.approvalcrest.matcher.typeadapters;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocalDateTimeAdapter extends AbstractTemporalAdapter<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("UTC")).withLocale(Locale.ENGLISH);
    public static final Type LOCAL_DATE_TIME_TYPE = (new TypeToken<LocalDateTime>() {
    }).getType();

    public LocalDateTimeAdapter() {
        super(FORMATTER, LocalDateTime::from);
    }
}
