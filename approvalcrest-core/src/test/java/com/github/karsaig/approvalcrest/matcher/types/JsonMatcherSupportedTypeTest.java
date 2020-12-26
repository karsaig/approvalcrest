package com.github.karsaig.approvalcrest.matcher.types;

import java.nio.file.Paths;
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

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;

class JsonMatcherSupportedTypeTest extends AbstractFileMatcherTest {


    public static Object[][] typeSerializationTestCases() {
        return new Object[][]{
                {Optional.empty(), "{}", null},
                {Optional.of(13L), "{\n  \"value\": 13\n}", null},
                {Optional.of(13L), "{}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "\n" +
                        "Unexpected: value\n"},
                {Optional.of(15L), "{\n  \"value\": 13\n}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "value\n" +
                        "Expected: 13\n" +
                        "     got: 15\n"},
                {com.google.common.base.Optional.absent(), "{}", null},
                {com.google.common.base.Optional.of(23L), "{\n  \"reference\": 23\n}", null},
                {com.google.common.base.Optional.of(23L), "{}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "\n" +
                        "Unexpected: reference\n"},
                {com.google.common.base.Optional.of(23L), "{\n  \"reference\": 42\n}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "reference\n" +
                        "Expected: 42\n" +
                        "     got: 23\n"},
                {Date.from(Instant.ofEpochSecond(13)), "\"1970-01-01T00:00:13.000Z\"", null},
                {Date.from(Instant.ofEpochSecond(13)), "\"1970-01-01T00:00:14.000Z\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {Date.from(Instant.ofEpochMilli(1L)), "\"1970-01-01T00:00:00.001Z\"", null},
                {Date.from(Instant.ofEpochMilli(1L)), "\"1970-01-01T00:00:00.002Z\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {Instant.ofEpochMilli(42), "\"1970-01-01T00:00:00.042Z\"", null},
                {Instant.ofEpochMilli(42), "\"1970-01-01T00:00:00.043Z\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {LocalDate.of(2019, 4, 1), "\"2019-04-01\"", null},
                {LocalDate.of(2019, 4, 1), "\"2019-04-02\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), "\"2020-04-21T18:38:15.000000013\"", null},
                {LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), "\"2020-04-21T18:38:15.000000014\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {LocalTime.of(18, 45, 13, 125), "\"18:45:13.000000125\"", null},
                {LocalTime.of(18, 45, 13, 125), "\"18:45:13.000000126\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), "\"2020-04-21T20:54:15.000000013Z\"", null},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), "\"2020-04-21T20:54:15.000000014Z\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.MAX), "\"2020-04-21T20:54:15.000000013Z\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {OffsetTime.of(21, 58, 17, 163, ZoneOffset.UTC), "\"21:58:17.000000164Z\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {OffsetTime.of(21, 58, 17, 163, ZoneOffset.UTC), "\"21:58:17.000000163Z\"", null},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), "\"2020-04-01T22:01:02.000000003Z[UTC]\"", null},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), "\"2020-04-01T22:01:02.000000004Z[UTC]\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("Asia/Tokyo")), "\"2020-04-01T22:01:02.000000003Z[UTC]\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {Instant.class, "\"java.time.Instant\"", null},
                {LocalDateTime.class, "\"java.time.Instant\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {Paths.get("/something/anything"), "\"/something/anything\"", null},
                {Paths.get("/something/anything"), "\"/somethingElse\"", "Expected file 4ac405/11b2ef-approved.json\n"},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")), "{\n" +
                        "  \"detailMessage\": \"X:\",\n" +
                        "  \"cause\": {\n" +
                        "    \"0x1\": {\n" +
                        "      \"detailMessage\": \"This is bad!\",\n" +
                        "      \"suppressedExceptions\": [],\n" +
                        "      \"class\": \"java.lang.IllegalStateException\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"suppressedExceptions\": [],\n" +
                        "  \"class\": \"java.lang.RuntimeException\"\n" +
                        "}", null},
                {new RuntimeException("X differs:", new IllegalStateException("This is bad!")), "{\n" +
                        "  \"detailMessage\": \"X:\",\n" +
                        "  \"cause\": {\n" +
                        "    \"0x1\": {\n" +
                        "      \"detailMessage\": \"This is bad!\",\n" +
                        "      \"suppressedExceptions\": [],\n" +
                        "      \"class\": \"java.lang.IllegalStateException\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"suppressedExceptions\": [],\n" +
                        "  \"class\": \"java.lang.RuntimeException\"\n" +
                        "}", "Expected file 4ac405/11b2ef-approved.json\n" +
                        "detailMessage\n" +
                        "Expected: X:\n" +
                        "     got: X differs:\n"},
        };
    }

    @ParameterizedTest
    @MethodSource("typeSerializationTestCases")
    void supportedTypeTest(Object input, String expected, String expectedExceptionMessage) {
        assertJsonMatcherWithDummyTestInfo(input, expected, expectedExceptionMessage);
    }

    @ParameterizedTest
    @MethodSource("typeSerializationTestCases")
    void supportedTypeAsPropertyTest(Object input, String expected, String expectedExceptionMessage) {
        String expectedString = "{\n" +
                "  \"dummyString\": \"dummy\",\n" +
                "  \"genericValue\": " + expected + "\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(BeanWithGeneric.of("dummy", input), expectedString, expectedExceptionMessage == null);
    }
}