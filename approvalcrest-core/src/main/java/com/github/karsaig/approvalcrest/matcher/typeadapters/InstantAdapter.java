package com.github.karsaig.approvalcrest.matcher.typeadapters;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InstantAdapter extends AbstractTemporalAdapter<Instant> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")).withLocale(Locale.ENGLISH);
    public static final Type INSTANT_TYPE = (new TypeToken<Instant>() {
    }).getType();

    public InstantAdapter() {
        super(FORMATTER, Instant::from);
    }
}
