package com.github.karsaig.approvalcrest.matcher.typeadapters;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OffsetDateTimeAdapter extends AbstractTemporalAdapter<OffsetDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")).withLocale(Locale.ENGLISH);
    public static final Type OFFSET_DATE_TIME_TYPE = (new TypeToken<OffsetDateTime>() {
    }).getType();

    public OffsetDateTimeAdapter() {
        super(FORMATTER, OffsetDateTime::from);
    }
}
