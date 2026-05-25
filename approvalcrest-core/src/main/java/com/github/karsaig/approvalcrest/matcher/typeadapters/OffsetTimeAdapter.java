package com.github.karsaig.approvalcrest.matcher.typeadapters;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OffsetTimeAdapter extends AbstractTemporalAdapter<OffsetTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_TIME.withLocale(Locale.ENGLISH);
    public static final Type OFFSET_TIME_TYPE = (new TypeToken<OffsetTime>() {
    }).getType();

    public OffsetTimeAdapter() {
        super(FORMATTER, OffsetTime::from);
    }
}
