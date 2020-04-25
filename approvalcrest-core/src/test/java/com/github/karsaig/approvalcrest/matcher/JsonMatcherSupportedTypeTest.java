package com.github.karsaig.approvalcrest.matcher;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class JsonMatcherSupportedTypeTest extends AbstractFileMatcherTest {


    public static Object[][] typeSerializationTestCases() {
        return new Object[][]{
                {Optional.empty(), "{}", true},
                {Optional.of(13L), "{\n  \"value\": 13\n}", true},
                {Optional.of(13L), "{}", false},
                {Optional.of(15L), "{\n  \"value\": 13\n}", false},
                {com.google.common.base.Optional.absent(), "{}", true},
                {com.google.common.base.Optional.of(23L), "{\n  \"reference\": 23\n}", true},
                {com.google.common.base.Optional.of(23L), "{}", false},
                {com.google.common.base.Optional.of(23L), "{\n  \"reference\": 42\n}", false},
                {Date.from(Instant.ofEpochSecond(13)), "\"1970-01-01T00:00:13.000Z\"", true},
                {Date.from(Instant.ofEpochSecond(13)), "\"1970-01-01T00:00:14.000Z\"", false},
                {Date.from(Instant.ofEpochMilli(1L)), "\"1970-01-01T00:00:00.001Z\"", true},
                {Date.from(Instant.ofEpochMilli(1L)), "\"1970-01-01T00:00:00.002Z\"", false},
                {Instant.ofEpochMilli(42), "\"1970-01-01T00:00:00.042Z\"", true},
                {Instant.ofEpochMilli(42), "\"1970-01-01T00:00:00.043Z\"", false},
                {LocalDate.of(2019, 4, 1), "\"2019-04-01\"", true},
                {LocalDate.of(2019, 4, 1), "\"2019-04-02\"", false},
                {LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), "\"2020-04-21T18:38:15.000000013\"", true},
                {LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), "\"2020-04-21T18:38:15.000000014\"", false},
                {LocalTime.of(18, 45, 13, 125), "\"18:45:13.000000125\"", true},
                {LocalTime.of(18, 45, 13, 125), "\"18:45:13.000000126\"", false},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), "\"2020-04-21T20:54:15.000000013Z\"", true},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), "\"2020-04-21T20:54:15.000000014Z\"", false},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.MAX), "\"2020-04-21T20:54:15.000000013Z\"", false},
                {OffsetTime.of(21, 58, 17, 163, ZoneOffset.UTC), "\"21:58:17.000000164Z\"", false},
                {OffsetTime.of(21, 58, 17, 163, ZoneOffset.UTC), "\"21:58:17.000000163Z\"", true},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), "\"2020-04-01T22:01:02.000000003Z[UTC]\"", true},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), "\"2020-04-01T22:01:02.000000004Z[UTC]\"", false},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("Asia/Tokyo")), "\"2020-04-01T22:01:02.000000003Z[UTC]\"", false},
                {Instant.class, "\"java.time.Instant\"", true},
                {LocalDateTime.class, "\"java.time.Instant\"", false},
                //{Paths.get("/something/anything"), "", true},
                //{Paths.get("/something/anything"), "", false},
        };
    }

    @ParameterizedTest
    @MethodSource("typeSerializationTestCases")
    void supportedTypeTest(Object input, String expected, boolean result) {
        inMemoryFsWithDummyTestInfo(input, expected, result);
    }


}