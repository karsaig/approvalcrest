package com.github.karsaig.approvalcrest.matcher.typeadapters;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocalDateAdapter extends AbstractTemporalAdapter<LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("UTC")).withLocale(Locale.ENGLISH);
    public static final Type LOCAL_DATE_TYPE = (new TypeToken<LocalDate>() {
    }).getType();

    public LocalDateAdapter() {
        super(FORMATTER, LocalDate::from);
    }
}
