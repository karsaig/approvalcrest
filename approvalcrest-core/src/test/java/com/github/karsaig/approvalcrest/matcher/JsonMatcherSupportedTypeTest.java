package com.github.karsaig.approvalcrest.matcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

class JsonMatcherSupportedTypeTest {


    public static Object[][] typeSerializationTestCases() {
        return new Object[][]{
                {Optional.empty(), "{}", true},
                {Optional.of(13L), "{\n  \"value\": 13\n}", true},
                {Optional.of(13L), "{\n  \"value\": 14\n}", false},
                {Optional.of(15L), "{\n  \"value\": 13\n}", false},
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
    void supportedTypeTest(Object input, String expected, boolean result) throws IOException {
        Configuration config = Configuration.unix()
                .toBuilder()
                .setAttributeViews("basic", "owner", "posix", "unix")
                .build();
        try (FileSystem fs = Jimfs.newFileSystem(config)) {
            Path testPath = fs.getPath("test", "path");
            Path pathWithDirs = Files.createDirectories(testPath);
            Path jsonDir = pathWithDirs.resolve("4ac405");
            Path testFile = Files.createDirectories(jsonDir).resolve("11b2ef-approved.json");
            Files.write(testFile, ImmutableList.of(expected), StandardCharsets.UTF_8);
            MatcherAssert.assertThat(new JsonMatcher<>(new DummyInformation(pathWithDirs)).matches(input), Matchers.is(result));
        }
    }


    private class DummyInformation implements TestMetaInformation {

        private final Path path;

        public DummyInformation(Path path) {
            this.path = path;
        }

        @Override
        public Path getTestClassPath() {
            return path;
        }

        @Override
        public String testClassName() {
            return "dummyTestClassName";
        }

        @Override
        public String testMethodName() {
            return "dummyTestMethodName";
        }
    }
}