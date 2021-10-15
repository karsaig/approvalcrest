package com.github.karsaig.approvalcrest.matcher;


import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;
import com.google.common.collect.Lists;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Stream;

public class JsonMatcherLocaleAndTzIndependenceTest extends AbstractFileMatcherTest {

    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZoneId HUN = ZoneId.of("Europe/Budapest");
    private static final ZoneId UK = ZoneId.of("Europe/London");
    private static final ZoneId SAMOA = ZoneId.of("US/Samoa");
    private static final ZoneId KIRITIMATI = ZoneId.of("Pacific/Kiritimati");

    private static final List<ZoneId> ZONES = Lists.newArrayList(UTC, HUN, UK, SAMOA, KIRITIMATI);
    private static final List<Locale> LOCALES = Lists.newArrayList(Locale.UK, Locale.SIMPLIFIED_CHINESE, Locale.GERMANY, Locale.JAPAN);
    private static final Object[][] TYPES = new Object[][]{
            {Date.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), "\"2020-04-01T20:01:02.000Z\""},
            {Date.from(Instant.ofEpochSecond(13)), "\"1970-01-01T00:00:13.000Z\""},
            {java.sql.Date.from(Instant.ofEpochSecond(15)), "\"1970-01-01T00:00:15.000Z\""},
            {java.sql.Date.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), "\"2020-04-01T20:01:02.000Z\""},
            {java.sql.Timestamp.from(Instant.ofEpochSecond(15)), "\"1970-01-01T00:00:15.000Z\""},
            {java.sql.Timestamp.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), "\"2020-04-01T20:01:02.000Z\""},
            {Instant.ofEpochMilli(42), "\"1970-01-01T00:00:00.042Z\""},
            {LocalDate.of(2019, 4, 1), "\"2019-04-01\""},
            {LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), "\"2020-04-21T18:38:15.000000013\""},
            {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.ofHours(3)), "\"2020-04-21T17:54:15.000000013Z\""},
            {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN), "\"2020-04-01T20:01:02.000000003Z[UTC]\""},
            {OffsetTime.of(21, 58, 17, 163, ZoneOffset.ofHours(3)), "\"21:58:17.000000163+03:00\"", true},
    };

    private static final Object[][] COMPLEX_TYPES = new Object[][]{
            {Date.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"2020-04-01T20:01:02.000Z\"\n" +
                    "  }\n" +
                    "}"},

            {Date.from(Instant.ofEpochSecond(13)), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"1970-01-01T00:00:13.000Z\"\n" +
                    "  }\n" +
                    "}"},

            {java.sql.Date.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"2020-04-01T20:01:02.000Z\"\n" +
                    "  }\n" +
                    "}"},

            {java.sql.Date.from(Instant.ofEpochSecond(15)), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"1970-01-01T00:00:15.000Z\"\n" +
                    "  }\n" +
                    "}"},

            {java.sql.Timestamp.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"2020-04-01T20:01:02.000Z\"\n" +
                    "  }\n" +
                    "}"},

            {java.sql.Timestamp.from(Instant.ofEpochSecond(15)), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"1970-01-01T00:00:15.000Z\"\n" +
                    "  }\n" +
                    "}"},


            {Instant.ofEpochMilli(42), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"1970-01-01T00:00:00.042Z\"\n" +
                    "  }\n" +
                    "}"},

            {LocalDate.of(2019, 4, 1), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"2019-04-01\"\n" +
                    "  }\n" +
                    "}"},


            {LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"2020-04-21T18:38:15.000000013\"\n" +
                    "  }\n" +
                    "}"},


            {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.ofHours(3)), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"2020-04-21T17:54:15.000000013Z\"\n" +
                    "  }\n" +
                    "}"},


            {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"2020-04-01T20:01:02.000000003Z[UTC]\"\n" +
                    "  }\n" +
                    "}"},


            {OffsetTime.of(21, 58, 17, 163, ZoneOffset.ofHours(3)), "{\n" +
                    "  \"dummyString\": \"dummy1\",\n" +
                    "  \"genericValue\": {\n" +
                    "    \"dummyString\": \"dummy2\",\n" +
                    "    \"genericValue\": \"21:58:17.000000163+03:00\"\n" +
                    "  }\n" +
                    "}", true},


    };

    private static Stream<Arguments> getPermutations(Object[][] types) {
        List<Arguments> result = new ArrayList<>(ZONES.size() * LOCALES.size() * types.length);
        for (Object[] actualType : types) {
            for (ZoneId actualZone : ZONES) {
                for (Locale actualLocale : LOCALES) {
                    result.add(Arguments.of(actualType[0].getClass(), actualType[0], actualZone, actualLocale, actualType[1]));
                }
            }
        }

        return result.stream();
    }

    public static Stream<Arguments> localeTimezoneTypePermutations() {
        return getPermutations(TYPES);
    }

    @ParameterizedTest(name = "{index}. {0} tz:{2} l:{3}")
    @MethodSource("localeTimezoneTypePermutations")
    void localeTimezoneIndependenceTest(Class<?> clazz, Object input, ZoneId zoneId, Locale locale, String expected) {
        Locale.setDefault(locale);
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
        assertJsonMatcherWithDummyTestInfo(input, expected, true);
    }

    public static Stream<Arguments> localeTimezoneComplexTypePermutations() {
        return getPermutations(COMPLEX_TYPES);
    }

    @ParameterizedTest(name = "{index}. {0} tz:{2} l:{3}")
    @MethodSource("localeTimezoneComplexTypePermutations")
    void localeTimezoneIndependenceComplexTest(Class<?> clazz, Object input, ZoneId zoneId, Locale locale, String expected) {
        BeanWithGeneric<BeanWithGeneric<Object>> complexInput = BeanWithGeneric.of("dummy1", BeanWithGeneric.of("dummy2", input));
        Locale.setDefault(locale);
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
        assertJsonMatcherWithDummyTestInfo(complexInput, expected, true);
    }
}
