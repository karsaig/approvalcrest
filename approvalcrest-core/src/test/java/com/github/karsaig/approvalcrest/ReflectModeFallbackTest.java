package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.DiagnosingCustomisableMatcher;
import com.github.karsaig.approvalcrest.matcher.TestMatcherFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that verify the "fallback" mode (getter-based serialization) works correctly.
 * <p>
 * This test class is run in a separate surefire execution with
 * {@code -DapprovalcrestReflection=fallback}, which disables Unsafe entirely
 * and forces getter-based serialization for locked module types.
 * <p>
 * Covers major library features:
 * <ul>
 *   <li>Mode detection</li>
 *   <li>Simple POJO comparisons (positive and negative)</li>
 *   <li>Nested objects, collections, maps, arrays</li>
 *   <li>Inheritance (child/parent/grandparent)</li>
 *   <li>Enum fields</li>
 *   <li>Locked module types (java.time, java.math)</li>
 *   <li>Throwable comparisons</li>
 *   <li>Ignoring fields (.ignoring())</li>
 *   <li>Custom matching (.with())</li>
 *   <li>Circular references</li>
 *   <li>Non-standard getter naming (record-style)</li>
 * </ul>
 */
public class ReflectModeFallbackTest {

    private static final TestMatcherFactory MATCHER_FACTORY = new TestMatcherFactory();

    // ---- Mode detection ----

    @Test
    public void modeIsFallback() {
        assertEquals("fallback", ReflectUtil.getMode());
        assertTrue(ReflectUtil.isFallbackMode());
        assertFalse(ReflectUtil.isForceMode());
        assertFalse(ReflectUtil.isUnsafeAvailable());
    }

    // ---- Simple POJO (positive) ----

    @Test
    public void pojoComparisonSucceedsWhenEqual() {
        SimpleBean expected = new SimpleBean("hello", 42);
        SimpleBean actual = new SimpleBean("hello", 42);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void nullFieldsHandledCorrectly() {
        SimpleBean expected = new SimpleBean(null, 0);
        SimpleBean actual = new SimpleBean(null, 0);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    // ---- Simple POJO (negative) ----

    @Test
    public void pojoComparisonFailsWhenDifferent() {
        SimpleBean expected = new SimpleBean("hello", 42);
        SimpleBean actual = new SimpleBean("world", 99);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void pojoMismatchReportsCorrectDiff() {
        SimpleBean expected = new SimpleBean("hello", 42);
        SimpleBean actual = new SimpleBean("world", 42);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        AssertionError error = assertThrows(AssertionError.class,
                () -> MatcherAssert.assertThat(actual, matcher));
        assertTrue(error.getMessage().contains("name"));
        assertTrue(error.getMessage().contains("hello"));
        assertTrue(error.getMessage().contains("world"));
    }

    // ---- Nested objects ----

    @Test
    public void nestedPojoComparisonWorks() {
        NestedBean expected = new NestedBean("outer", new SimpleBean("inner", 1));
        NestedBean actual = new NestedBean("outer", new SimpleBean("inner", 1));

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void nestedPojoMismatchDetected() {
        NestedBean expected = new NestedBean("outer", new SimpleBean("inner", 1));
        NestedBean actual = new NestedBean("outer", new SimpleBean("different", 2));

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void deeplyNestedComparisonWorks() {
        NestedBean inner = new NestedBean("level2", new SimpleBean("level3", 100));
        DeepBean expected = new DeepBean("level1", inner);
        DeepBean actual = new DeepBean("level1", new NestedBean("level2", new SimpleBean("level3", 100)));

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    // ---- Collections ----

    @Test
    public void beanWithListComparisonWorks() {
        BeanWithCollections expected = new BeanWithCollections(
                Arrays.asList("a", "b", "c"), null, null);
        BeanWithCollections actual = new BeanWithCollections(
                Arrays.asList("a", "b", "c"), null, null);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void beanWithListMismatchDetected() {
        BeanWithCollections expected = new BeanWithCollections(
                Arrays.asList("a", "b", "c"), null, null);
        BeanWithCollections actual = new BeanWithCollections(
                Arrays.asList("a", "x", "c"), null, null);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void beanWithSetComparisonWorks() {
        Set<String> items = new HashSet<>(Arrays.asList("x", "y", "z"));
        BeanWithCollections expected = new BeanWithCollections(null, items, null);
        BeanWithCollections actual = new BeanWithCollections(null, new HashSet<>(items), null);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void beanWithArrayComparisonWorks() {
        BeanWithCollections expected = new BeanWithCollections(null, null, new int[]{1, 2, 3});
        BeanWithCollections actual = new BeanWithCollections(null, null, new int[]{1, 2, 3});

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void beanWithArrayMismatchDetected() {
        BeanWithCollections expected = new BeanWithCollections(null, null, new int[]{1, 2, 3});
        BeanWithCollections actual = new BeanWithCollections(null, null, new int[]{1, 2, 99});

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void beanWithNestedObjectListComparisonWorks() {
        List<SimpleBean> items = Arrays.asList(new SimpleBean("a", 1), new SimpleBean("b", 2));
        BeanWithObjectList expected = new BeanWithObjectList("container", items);
        BeanWithObjectList actual = new BeanWithObjectList("container",
                Arrays.asList(new SimpleBean("a", 1), new SimpleBean("b", 2)));

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    // ---- Maps ----

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
    public void beanWithMapFieldWorks() {
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("alice", 100);
        scores.put("bob", 85);
        BeanWithMap expected = new BeanWithMap("scores", scores);

        Map<String, Integer> scoresActual = new LinkedHashMap<>();
        scoresActual.put("alice", 100);
        scoresActual.put("bob", 85);
        BeanWithMap actual = new BeanWithMap("scores", scoresActual);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void beanWithMapMismatchDetected() {
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("alice", 100);
        BeanWithMap expected = new BeanWithMap("scores", scores);

        Map<String, Integer> scoresActual = new LinkedHashMap<>();
        scoresActual.put("alice", 50);
        BeanWithMap actual = new BeanWithMap("scores", scoresActual);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    // ---- Inheritance ----

    @Test
    public void inheritedFieldsComparedCorrectly() {
        ChildBean expected = new ChildBean("parent", "child", 42);
        ChildBean actual = new ChildBean("parent", "child", 42);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void inheritedFieldMismatchDetected() {
        ChildBean expected = new ChildBean("parent", "child", 42);
        ChildBean actual = new ChildBean("different-parent", "child", 42);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    @Test
    public void grandchildInheritanceWorks() {
        GrandchildBean expected = new GrandchildBean("gp", "p", "c", true);
        GrandchildBean actual = new GrandchildBean("gp", "p", "c", true);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void grandchildInheritanceMismatchDetected() {
        GrandchildBean expected = new GrandchildBean("gp", "p", "c", true);
        GrandchildBean actual = new GrandchildBean("gp", "p", "c", false);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    // ---- Enums ----

    @Test
    public void beanWithEnumFieldWorks() {
        BeanWithEnum expected = new BeanWithEnum("item", Status.ACTIVE);
        BeanWithEnum actual = new BeanWithEnum("item", Status.ACTIVE);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void beanWithEnumMismatchDetected() {
        BeanWithEnum expected = new BeanWithEnum("item", Status.ACTIVE);
        BeanWithEnum actual = new BeanWithEnum("item", Status.INACTIVE);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    // ---- Locked module types ----

    @Test
    public void beanWithLockedModuleTypeDoesNotCrash() {
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

    // ---- Throwable ----

    @Test
    public void throwableComparisonDoesNotCrash() {
        Exception expected = new RuntimeException("test error");
        Exception actual = new RuntimeException("test error");

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertNotNull(matcher);
        matcher.matches(actual);
    }

    @Test
    public void throwableMismatchDetected() {
        Exception expected = new RuntimeException("error one");
        Exception actual = new RuntimeException("error two");

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    // ---- Ignoring fields ----

    @Test
    public void ignoringFieldWorks() {
        SimpleBean expected = new SimpleBean("hello", 42);
        SimpleBean actual = new SimpleBean("hello", 99);

        MatcherAssert.assertThat(actual, MATCHER_FACTORY.beanMatcher(expected)
                .ignoring("value"));
    }

    @Test
    public void ignoringMultipleFieldsWorks() {
        SimpleBean expected = new SimpleBean("hello", 42);
        SimpleBean actual = new SimpleBean("world", 99);

        MatcherAssert.assertThat(actual, MATCHER_FACTORY.beanMatcher(expected)
                .ignoring("name").ignoring("value"));
    }

    @Test
    public void ignoringNestedFieldWorks() {
        NestedBean expected = new NestedBean("outer", new SimpleBean("inner", 1));
        NestedBean actual = new NestedBean("outer", new SimpleBean("different", 1));

        MatcherAssert.assertThat(actual, MATCHER_FACTORY.beanMatcher(expected)
                .ignoring("child.name"));
    }

    // ---- Custom matching ----

    @Test
    public void customMatcherWorks() {
        SimpleBean expected = new SimpleBean("hello world", 42);
        SimpleBean actual = new SimpleBean("hello earth", 42);

        MatcherAssert.assertThat(actual, MATCHER_FACTORY.beanMatcher(expected)
                .with("name", Matchers.startsWith("hello")));
    }

    @Test
    public void customMatcherFailsWhenNoMatch() {
        SimpleBean expected = new SimpleBean("hello world", 42);
        SimpleBean actual = new SimpleBean("goodbye", 42);

        DiagnosingCustomisableMatcher<SimpleBean> matcher = MATCHER_FACTORY.beanMatcher(expected)
                .with("name", Matchers.startsWith("hello"));
        assertFalse(matcher.matches(actual));
    }

    // ---- Circular references ----

    @Test
    public void circularReferenceDoesNotCrash() {
        CircularBean a = new CircularBean("a");
        CircularBean b = new CircularBean("b");
        a.setRef(b);
        b.setRef(a);

        CircularBean expectedA = new CircularBean("a");
        CircularBean expectedB = new CircularBean("b");
        expectedA.setRef(expectedB);
        expectedB.setRef(expectedA);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expectedA);
        // Should not throw StackOverflowError
        matcher.matches(a);
    }

    // ---- Non-standard getters (record-style) ----
    // Note: user POJOs on the classpath use field-based access in all modes.
    // This test verifies that field-based access handles non-standard naming.

    @Test
    public void beanWithNonStandardGetterNamingWorks() {
        RecordStyleBean expected = new RecordStyleBean("hello", 42, true);
        RecordStyleBean actual = new RecordStyleBean("hello", 42, true);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        MatcherAssert.assertThat(actual, matcher);
    }

    @Test
    public void beanWithNonStandardGetterMismatchDetected() {
        RecordStyleBean expected = new RecordStyleBean("hello", 42, true);
        RecordStyleBean actual = new RecordStyleBean("world", 42, false);

        DiagnosingCustomisableMatcher<Object> matcher = MATCHER_FACTORY.beanMatcher(expected);
        assertFalse(matcher.matches(actual));
    }

    // ---- Test data classes ----

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
    public static class DeepBean {
        private final String name;
        private final NestedBean nested;

        public DeepBean(String name, NestedBean nested) {
            this.name = name;
            this.nested = nested;
        }

        public String getName() { return name; }
        public NestedBean getNested() { return nested; }
    }

    @SuppressWarnings("unused")
    public static class BeanWithCollections {
        private final List<String> items;
        private final Set<String> tags;
        private final int[] numbers;

        public BeanWithCollections(List<String> items, Set<String> tags, int[] numbers) {
            this.items = items;
            this.tags = tags;
            this.numbers = numbers;
        }

        public List<String> getItems() { return items; }
        public Set<String> getTags() { return tags; }
        public int[] getNumbers() { return numbers; }
    }

    @SuppressWarnings("unused")
    public static class BeanWithObjectList {
        private final String label;
        private final List<SimpleBean> children;

        public BeanWithObjectList(String label, List<SimpleBean> children) {
            this.label = label;
            this.children = children;
        }

        public String getLabel() { return label; }
        public List<SimpleBean> getChildren() { return children; }
    }

    @SuppressWarnings("unused")
    public static class BeanWithMap {
        private final String label;
        private final Map<String, Integer> data;

        public BeanWithMap(String label, Map<String, Integer> data) {
            this.label = label;
            this.data = data;
        }

        public String getLabel() { return label; }
        public Map<String, Integer> getData() { return data; }
    }

    @SuppressWarnings("unused")
    public static class ParentBean {
        private final String parentField;

        public ParentBean(String parentField) {
            this.parentField = parentField;
        }

        public String getParentField() { return parentField; }
    }

    @SuppressWarnings("unused")
    public static class ChildBean extends ParentBean {
        private final String childField;
        private final int childValue;

        public ChildBean(String parentField, String childField, int childValue) {
            super(parentField);
            this.childField = childField;
            this.childValue = childValue;
        }

        public String getChildField() { return childField; }
        public int getChildValue() { return childValue; }
    }

    @SuppressWarnings("unused")
    public static class GrandparentBean {
        private final String gpField;

        public GrandparentBean(String gpField) {
            this.gpField = gpField;
        }

        public String getGpField() { return gpField; }
    }

    @SuppressWarnings("unused")
    public static class MiddleBean extends GrandparentBean {
        private final String middleField;

        public MiddleBean(String gpField, String middleField) {
            super(gpField);
            this.middleField = middleField;
        }

        public String getMiddleField() { return middleField; }
    }

    @SuppressWarnings("unused")
    public static class GrandchildBean extends MiddleBean {
        private final String gcField;
        private final boolean active;

        public GrandchildBean(String gpField, String middleField, String gcField, boolean active) {
            super(gpField, middleField);
            this.gcField = gcField;
            this.active = active;
        }

        public String getGcField() { return gcField; }
        public boolean isActive() { return active; }
    }

    public enum Status {
        ACTIVE, INACTIVE, PENDING
    }

    @SuppressWarnings("unused")
    public static class BeanWithEnum {
        private final String name;
        private final Status status;

        public BeanWithEnum(String name, Status status) {
            this.name = name;
            this.status = status;
        }

        public String getName() { return name; }
        public Status getStatus() { return status; }
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

    @SuppressWarnings("unused")
    public static class CircularBean {
        private final String name;
        private CircularBean ref;

        public CircularBean(String name) {
            this.name = name;
        }

        public String getName() { return name; }
        public CircularBean getRef() { return ref; }
        public void setRef(CircularBean ref) { this.ref = ref; }
    }

    /**
     * Bean using record-style accessor naming (name(), value(), active())
     * instead of JavaBean getX()/isX() convention.
     * Since this class is on the classpath (unnamed module), it uses field-based
     * access regardless of mode — verifies that field access works for such classes.
     */
    @SuppressWarnings("unused")
    public static class RecordStyleBean {
        private final String name;
        private final int value;
        private final boolean active;

        public RecordStyleBean(String name, int value, boolean active) {
            this.name = name;
            this.value = value;
            this.active = active;
        }

        // Non-standard accessors (no get/is prefix)
        public String name() { return name; }
        public int value() { return value; }
        public boolean active() { return active; }
    }
}
