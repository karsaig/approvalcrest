package com.github.karsaig.approvalcrest.matcher.types;

import com.github.karsaig.approvalcrest.matcher.AbstractBeanMatcherTest;
import com.github.karsaig.approvalcrest.testdata.Bean;
import com.github.karsaig.approvalcrest.testdata.BeanWithGeneric;
import com.github.karsaig.approvalcrest.testdata.BeanWithGenericIterable;
import com.google.common.collect.Sets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import static com.github.karsaig.approvalcrest.testdata.Bean.Builder.bean;

class BeanMatcherSupportedTypeTest extends AbstractBeanMatcherTest {

    private static final ZoneId HUN = ZoneId.of("Europe/Budapest");

    public static Object[][] typeSerializationTestCases() {
        return new Object[][]{
                {null, null, null},
                {"NotNull", null, "actual is not null"},
                {null, "NotNull", "\n" +
                        "Expected: \"NotNull\"\n" +
                        "     but: was null"},
                {"NotNull", "NotNull", null},
                {"NotNull", "NotNull2", "\n" +
                        "Expected: \"NotNull2\"\n" +
                        "     but: was \"NotNull\""},
                {"NotNull2", "NotNull", "\n" +
                        "Expected: \"NotNull\"\n" +
                        "     but: was \"NotNull2\""},
                {null, 1, "\n" +
                        "Expected: <1>\n" +
                        "     but: was null"},
                {1, null, "actual is not null"},
                {1, 1, null},
                {true, true, null},
                {false, false, null},
                {true, false, "\n" +
                        "Expected: <false>\n" +
                        "     but: was <true>"},
                {'a', 'a', null},
                {'a', 'b', "\n" +
                        "Expected: \"b\"\n" +
                        "     but: was \"a\""},
                {Byte.MIN_VALUE, Byte.MIN_VALUE, null},
                {Byte.MAX_VALUE, Byte.MAX_VALUE, null},
                {Byte.MIN_VALUE, Byte.MAX_VALUE, "\n" +
                        "Expected: <127b>\n" +
                        "     but: was <-128b>"},
                {Short.MIN_VALUE, Short.MIN_VALUE, null},
                {Short.MAX_VALUE, Short.MAX_VALUE, null},
                {Short.MIN_VALUE, Short.MAX_VALUE, "\n" +
                        "Expected: <32767s>\n" +
                        "     but: was <-32768s>"},
                {Integer.MIN_VALUE, Integer.MIN_VALUE, null},
                {Integer.MAX_VALUE, Integer.MAX_VALUE, null},
                {Integer.MAX_VALUE, Integer.MIN_VALUE, "\n" +
                        "Expected: <-2147483648>\n" +
                        "     but: was <2147483647>"},
                {Long.MIN_VALUE, Long.MIN_VALUE, null},
                {Long.MAX_VALUE, Long.MAX_VALUE, null},
                {Long.MIN_VALUE, Long.MAX_VALUE, "\n" +
                        "Expected: <9223372036854775807L>\n" +
                        "     but: was <-9223372036854775808L>"},
                {Float.MIN_VALUE, Float.MIN_VALUE, null},
                {Float.MAX_VALUE, Float.MAX_VALUE, null},
                {Float.MIN_VALUE, Float.MAX_VALUE, "\n" +
                        "Expected: <3.4028235E38F>\n" +
                        "     but: was <1.4E-45F>"},
                {Double.MIN_VALUE, Double.MIN_VALUE, null},
                {Double.MAX_VALUE, Double.MAX_VALUE, null},
                {Double.MIN_VALUE, Double.MAX_VALUE, "\n" +
                        "Expected: <1.7976931348623157E308>\n" +
                        "     but: was <4.9E-324>"},
                {Double.NaN, Double.NaN, null},
                {Double.NaN, Double.MIN_NORMAL, "\n" +
                        "Expected: <2.2250738585072014E-308>\n" +
                        "     but: was <NaN>"},
                {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, null},
                {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, null},
                {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, "\n" +
                        "Expected: <Infinity>\n" +
                        "     but: was <-Infinity>"},
                {BigInteger.ZERO, BigInteger.ZERO, null},
                {BigInteger.TEN, BigInteger.TEN, null},

                {BigInteger.TEN, BigInteger.ZERO, ""},
                {BigDecimal.ZERO, BigDecimal.ZERO, null},
                {BigDecimal.TEN, BigDecimal.TEN, null},
                {BigDecimal.ZERO, BigDecimal.TEN, ""},
                {EnumTest.ENUM1, EnumTest.ENUM1, null},
                {EnumTest.ENUM2, EnumTest.ENUM2, null},
                {EnumTest.ENUM2, EnumTest.ENUM1, "\n" +
                        "Expected: <ENUM1>\n" +
                        "     but: was <ENUM2>"},
                {Optional.empty(), Optional.empty(), null},
                {Optional.of(13L), Optional.of(13L), null},
                {Optional.of(13L), Optional.empty(), "\n" +
                        "Unexpected: value\n"},
                {Optional.of(15L), Optional.of(12L), "value\n" +
                        "Expected: 12\n" +
                        "     got: 15\n"},
                {com.google.common.base.Optional.absent(), com.google.common.base.Optional.absent(), null},
                {com.google.common.base.Optional.of(13L), com.google.common.base.Optional.of(13L), null},
                {com.google.common.base.Optional.of(13L), com.google.common.base.Optional.absent(), "\n" +
                        "Unexpected: reference\n"},
                {com.google.common.base.Optional.of(13L), com.google.common.base.Optional.of(42L), "reference\n" +
                        "Expected: 42\n" +
                        "     got: 13\n"},
                {Date.from(Instant.ofEpochSecond(13)), Date.from(Instant.ofEpochSecond(13)), null},
                {Date.from(Instant.ofEpochSecond(13)), Date.from(Instant.ofEpochSecond(14)), ""},
                {Date.from(Instant.ofEpochMilli(1L)), Date.from(Instant.ofEpochMilli(1L)), null},
                {Date.from(Instant.ofEpochMilli(1L)), Date.from(Instant.ofEpochMilli(2L)), ""},
                {java.sql.Date.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), java.sql.Date.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), null},
                {java.sql.Date.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), java.sql.Date.from(ZonedDateTime.of(2021, 4, 1, 22, 1, 2, 3, HUN).toInstant()), ""},
                {java.sql.Timestamp.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), java.sql.Timestamp.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), null},
                {java.sql.Timestamp.from(ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, HUN).toInstant()), java.sql.Timestamp.from(ZonedDateTime.of(2021, 4, 1, 22, 1, 2, 3, HUN).toInstant()), ""},
                {Instant.ofEpochMilli(42), Instant.ofEpochMilli(42), null},
                {Instant.MIN.plusSeconds(31622400L), Instant.MIN.plusSeconds(31622400L), null},
                {Instant.MAX.minusSeconds(31622400L), Instant.MAX.minusSeconds(31622400L), null},
                {Instant.EPOCH, Instant.EPOCH, null},
                {Instant.ofEpochMilli(42), Instant.ofEpochMilli(43), ""},
                {LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 1), null},
                {LocalDate.of(2019, 4, 1), LocalDate.of(2019, 4, 2), ""},
                {LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), null},
                {LocalDateTime.of(2020, 4, 21, 18, 38, 15, 13), LocalDateTime.of(2020, 4, 21, 18, 38, 15, 11), ""},
                {LocalTime.of(18, 45, 13, 125), LocalTime.of(18, 45, 13, 125), null},
                {LocalTime.of(18, 45, 13, 125), LocalTime.of(18, 45, 13, 126), ""},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), null},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 12, ZoneOffset.UTC), ""},
                {OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.UTC), OffsetDateTime.of(2020, 4, 21, 20, 54, 15, 13, ZoneOffset.MAX), ""},
                {OffsetTime.of(21, 58, 17, 163, ZoneOffset.UTC), OffsetTime.of(21, 58, 17, 164, ZoneOffset.UTC), ""},
                {OffsetTime.of(21, 58, 17, 163, ZoneOffset.UTC), OffsetTime.of(21, 58, 17, 163, ZoneOffset.UTC), null},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), null},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 4, ZoneId.of("UTC")), ""},
                {ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("UTC")), ZonedDateTime.of(2020, 4, 1, 22, 1, 2, 3, ZoneId.of("Asia/Tokyo")), ""},
                {Instant.class, Instant.class, null},
                {Instant.class, LocalDateTime.class, ""},
                {Paths.get("/something/anything"), Paths.get("/something/anything"), null},
                {Paths.get("/something/anything"), Paths.get("/somethingElse/anything"), ""},
                {new RuntimeException("X:", new IllegalStateException("This is bad!")), new RuntimeException("X:", new IllegalStateException("This is bad!")), null},
                {new RuntimeException("X:", new IllegalStateException("This is bad! Differs")), new RuntimeException("X:", new IllegalStateException("This is bad!")), "cause.0x1.detailMessage\n" +
                        "Expected: This is bad!\n" +
                        "     got: This is bad! Differs\n"},
                {new RuntimeException("X differs:", new IllegalStateException("This is bad!")), new RuntimeException("X:", new IllegalStateException("This is bad!")), "detailMessage\n" +
                        "Expected: X:\n" +
                        "     got: X differs:\n"},
                {bean().string("string").integer(1).build(), bean().string("string").integer(1).build(), null},
                {bean().string("string").integer(1).build(), bean().string("string2").integer(2).build(), "integer\n" +
                        "Expected: 2\n" +
                        "     got: 1\n" +
                        " ; string\n" +
                        "Expected: string2\n" +
                        "     got: string\n"},
                {bean().string("string").integer(1).map(Collections.emptyMap()).hashMap(new HashMap<>()).hashSet(new HashSet<>()).set(Collections.emptySet()).build(), bean().string("string").integer(1).map(Collections.emptyMap()).hashMap(new HashMap<>()).hashSet(new HashSet<>()).set(Collections.emptySet()).build(), null},
                {bean().string("string3").integer(13).map(new HashMap<Bean, Bean>() {{
                    put(bean().build(), bean().build());
                }}).hashMap(new HashMap<>()).hashSet(new HashSet<>()).set(Collections.emptySet()).build(), bean().string("string").integer(1).map(Collections.emptyMap()).hashMap(new HashMap<>()).hashSet(new HashSet<>()).set(Collections.emptySet()).build(), "integer\n" +
                        "Expected: 1\n" +
                        "     got: 13\n" +
                        " ; map[]: Expected 0 values but got 1 ; string\n" +
                        "Expected: string\n" +
                        "     got: string3\n"},

        };
    }

    @ParameterizedTest
    @MethodSource("typeSerializationTestCases")
    void supportedTypeTest(Object input, Object expected, String expectedExceptionMessage) {
        assertDiagnosingMatcher(input, expected, expectedExceptionMessage);
    }

    @ParameterizedTest
    @MethodSource("typeSerializationTestCases")
    void supportedTypeAsGenericPropertyTest(Object input, Object expected, String expectedExceptionMessage) {
        MatcherAssert.assertThat(MATCHER_FACTORY.beanMatcher(BeanWithGeneric.of("dummy", expected))
                .matches(BeanWithGeneric.of("dummy", input)), Matchers.is(expectedExceptionMessage == null));
    }

    @ParameterizedTest
    @MethodSource("typeSerializationTestCases")
    void supportedTypeAsIterablePropertyTest(Object input, Object expected, String expectedExceptionMessage) {
        MatcherAssert.assertThat(MATCHER_FACTORY.beanMatcher(BeanWithGenericIterable.Builder.bean().set(Sets.newHashSet(expected)).build())
                .matches(BeanWithGenericIterable.Builder.bean().set(Sets.newHashSet(input)).build()), Matchers.is(expectedExceptionMessage == null));
    }
}