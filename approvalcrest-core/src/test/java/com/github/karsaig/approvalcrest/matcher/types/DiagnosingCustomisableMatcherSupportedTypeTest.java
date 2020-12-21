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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;

class DiagnosingCustomisableMatcherSupportedTypeTest {

    public static Object[][] typeSerializationTestCases() {
        return new Object[][]{
                {Optional.empty(), Optional.empty(), true},
                {Optional.of(13L), Optional.of(13L), true},
                {Optional.of(13L), Optional.empty(), false},
                {Optional.of(15L), Optional.of(12L), false},
                {com.google.common.base.Optional.absent(), com.google.common.base.Optional.absent(), true},
                {com.google.common.base.Optional.of(13L), com.google.common.base.Optional.of(13L), true},
                {com.google.common.base.Optional.of(13L), com.google.common.base.Optional.absent(), false},
                {com.google.common.base.Optional.of(13L), com.google.common.base.Optional.of(42L), false},
                {Date.from(Instant.ofEpochSecond(13)), Date.from(Instant.ofEpochSecond(13)), true},
                {Date.from(Instant.ofEpochSecond(13)), Date.from(Instant.ofEpochSecond(14)), false},
                {Date.from(Instant.ofEpochMilli(1L)), Date.from(Instant.ofEpochMilli(1L)), true},
                {Date.from(Instant.ofEpochMilli(1L)), Date.from(Instant.ofEpochMilli(2L)), false},
                {Instant.ofEpochMilli(42), Instant.ofEpochMilli(42), true},
                {Instant.MIN.plusSeconds(31622400L), Instant.MIN.plusSeconds(31622400L), true},
                {Instant.MAX.minusSeconds(31622400L), Instant.MAX.minusSeconds(31622400L), true},
                {Instant.EPOCH, Instant.EPOCH, true},
                {Instant.ofEpochMilli(42), Instant.ofEpochMilli(43), false},
                {LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 1), true},
                {LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 2), false},
                {LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), true},
                {LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), LocalDateTime.of(2020, 4, 21, 18, 38, 15, 11), false},
                {LocalTime.of(18, 45, 13, 125), LocalTime.of(18, 45, 13, 125), true},
                {LocalTime.of(18, 45, 13, 125), LocalTime.of(18, 45, 13, 126), false},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), true},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 12, ZoneOffset.UTC), false},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.MAX), false},
                {OffsetTime.of(21, 58, 17, 163, ZoneOffset.UTC), OffsetTime.of(21, 58, 17, 164, ZoneOffset.UTC), false},
                {OffsetTime.of(21, 58, 17, 163, ZoneOffset.UTC), OffsetTime.of(21, 58, 17, 163, ZoneOffset.UTC), true},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), true},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 4, ZoneId.of("UTC")), false},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("Asia/Tokyo")), false},
                {Instant.class, Instant.class, true},
                {Instant.class, LocalDateTime.class, false},
                {Paths.get("/something/anything"), Paths.get("/something/anything"), true},
                {Paths.get("/something/anything"), Paths.get("/somethingElse/anything"), false},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")), new RuntimeException("X:", new IllegalStateException("This is bad!")), true},
                {new RuntimeException("X:", new IllegalStateException("This is bad! Differs")), new RuntimeException("X:", new IllegalStateException("This is bad!")), false},
                {new RuntimeException("X differs:", new IllegalStateException("This is bad!")), new RuntimeException("X:", new IllegalStateException("This is bad!")), false},
        };
    }

    @ParameterizedTest
    @MethodSource("typeSerializationTestCases")
    void supportedTypeTest(Object input, Object expected, boolean result) {
        MatcherAssert.assertThat(new DiagnosingCustomisableMatcher<>(expected).matches(input), Matchers.is(result));
    }

    @ParameterizedTest
    @MethodSource("typeSerializationTestCases")
    void supportedTypeAsPropertyTest(Object input, Object expected, boolean result) {
        MatcherAssert.assertThat(new DiagnosingCustomisableMatcher<>(BeanWithGeneric.of("dummy", expected)).matches(BeanWithGeneric.of("dummy", input)), Matchers.is(result));
    }
}