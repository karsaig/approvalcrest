package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.TestMatcherFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that verify the "fallback" mode (getter-based serialization) works correctly.
 * <p>
 * This test class is run in a separate surefire execution with
 * {@code -DapprovalcrestReflection=fallback}, which disables Unsafe entirely
 * and forces getter-based serialization for locked module types.
 * <p>
 * These tests verify:
 * <ul>
 *   <li>The mode is correctly detected</li>
 *   <li>POJO comparisons still work (our own classes are not in locked modules)</li>
 *   <li>Types from locked modules (java.time, java.math, etc.) don't crash</li>
 *   <li>Throwable comparisons don't crash</li>
 *   <li>Equal objects match, different objects don't match</li>
 * </ul>
 */
public class ReflectModeFallbackTest {

    private static final TestMatcherFactory MATCHER_FACTORY = new TestMatcherFactory();

    @Test
    public void modeIsFallback() {
        assertEquals("fallback", ReflectUtil.getMode());
        assertTrue(ReflectUtil.isFallbackMode());
        assertFalse(ReflectUtil.isForceMode());
        assertFalse(ReflectUtil.isUnsafeAvailable());
    }

    @Test
    public void pojoComparisonSucceedsWhenEqual() {
        SimpleBean expected = new SimpleBean("hello", 42);
        SimpleBean actual = new SimpleBean("hello", 42);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void pojoComparisonFailsWhenDifferent() {
        SimpleBean expected = new SimpleBean("hello", 42);
        SimpleBean actual = new SimpleBean("world", 99);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void nestedPojoComparisonWorks() {
        NestedBean expected = new NestedBean("outer", new SimpleBean("inner", 1));
        NestedBean actual = new NestedBean("outer", new SimpleBean("inner", 1));

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void mapComparisonWorks() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", 123);

        Map<String, Object> actual = new HashMap<>();
        actual.put("key1", "value1");
        actual.put("key2", 123);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void beanWithLockedModuleTypeDoesNotCrash() {
        // java.time types are in locked modules — in fallback mode, they must not crash
        BeanWithLockedTypes expected = new BeanWithLockedTypes(
                LocalDate.of(2024, 1, 15),
                LocalDateTime.of(2024, 1, 15, 10, 30),
                Instant.parse("2024-01-15T10:30:00Z"),
                new BigDecimal("3.14")
        );
        BeanWithLockedTypes actual = new BeanWithLockedTypes(
                LocalDate.of(2024, 1, 15),
                LocalDateTime.of(2024, 1, 15, 10, 30),
                Instant.parse("2024-01-15T10:30:00Z"),
                new BigDecimal("3.14")
        );

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void beanWithDifferentLockedModuleTypeDetectsDifference() {
        BeanWithLockedTypes expected = new BeanWithLockedTypes(
                LocalDate.of(2024, 1, 15), null, null, new BigDecimal("3.14")
        );
        BeanWithLockedTypes actual = new BeanWithLockedTypes(
                LocalDate.of(2024, 2, 20), null, null, new BigDecimal("2.71")
        );

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void throwableComparisonDoesNotCrash() {
        Exception expected = new RuntimeException("test error");
        Exception actual = new RuntimeException("test error");

        // In fallback mode, Throwable internals may not serialize identically,
        // but the operation must not crash
        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertNotNull(matcher);
        // Just verify no exception is thrown during matching
        matcher.matches(actual);
    }

    @Test
    public void nullFieldsHandledCorrectly() {
        SimpleBean expected = new SimpleBean(null, 0);
        SimpleBean actual = new SimpleBean(null, 0);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    // Test data classes

    @SuppressWarnings("unused")
    public static class SimpleBean {
        private final String name;
        private final int value;

        public SimpleBean(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    @SuppressWarnings("unused")
    public static class NestedBean {
        private final String label;
        private final SimpleBean child;

        public NestedBean(String label, SimpleBean child) {
            this.label = label;
            this.child = child;
        }

        public String getLabel() { return label; }
        public SimpleBean getChild() { return child; }
    }

    @SuppressWarnings("unused")
    public static class BeanWithLockedTypes {
        private final LocalDate date;
        private final LocalDateTime dateTime;
        private final Instant instant;
        private final BigDecimal decimal;

        public BeanWithLockedTypes(LocalDate date, LocalDateTime dateTime, Instant instant, BigDecimal decimal) {
            this.date = date;
            this.dateTime = dateTime;
            this.instant = instant;
            this.decimal = decimal;
        }

        public LocalDate getDate() { return date; }
        public LocalDateTime getDateTime() { return dateTime; }
        public Instant getInstant() { return instant; }
        public BigDecimal getDecimal() { return decimal; }
    }
}
