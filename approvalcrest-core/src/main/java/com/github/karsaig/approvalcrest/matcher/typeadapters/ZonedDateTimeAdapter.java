package com.github.karsaig.approvalcrest.matcher.typeadapters;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ZonedDateTimeAdapter extends AbstractTemporalAdapter<ZonedDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("UTC")).withLocale(Locale.ENGLISH);
    public static final Type ZONED_DATE_TIME_TYPE = (new TypeToken<ZonedDateTime>() {
    }).getType();

    public ZonedDateTimeAdapter() {
        super(FORMATTER, ZonedDateTime::from);
    }
}
