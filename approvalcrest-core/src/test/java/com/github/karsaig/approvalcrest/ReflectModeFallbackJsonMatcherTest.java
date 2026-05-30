package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that verify the file-based {@link JsonMatcher} (sameJsonAsApproved) works correctly
 * in "fallback" mode (getter-based serialization for locked module types).
 * <p>
 * This test class is run in a separate surefire execution with
 * {@code -DapprovalcrestReflection=fallback}.
 * <p>
 * Covers major library features:
 * <ul>
 *   <li>Mode detection</li>
 *   <li>Simple POJOs (positive and negative)</li>
 *   <li>Nested objects</li>
 *   <li>Collections (List, arrays)</li>
 *   <li>Maps</li>
 *   <li>Inheritance</li>
 *   <li>Enums</li>
 *   <li>Types with custom adapters (LocalDate, Instant, Path)</li>
 *   <li>Throwable (getter-based in fallback)</li>
 *   <li>Ignoring fields</li>
 *   <li>Custom matching</li>
 *   <li>Not-approved file creation</li>
 *   <li>Mismatch detection with diff</li>
 *   <li>Non-standard getter naming</li>
 * </ul>
 */
public class ReflectModeFallbackJsonMatcherTest extends AbstractFileMatcherTest {

    @Test
    public void modeIsFallbackInJsonMatcherTest() {
        assertTrue(ReflectUtil.isFallbackMode());
        assertFalse(ReflectUtil.isForceMode());
        assertFalse(ReflectUtil.isUnsafeAvailable());
    }

    // ---- Simple POJO (positive) ----

    @Test
    public void pojoMatchesApprovedFile() {
        SimplePojo input = new SimplePojo("hello", 42);
        String approvedContent = "{\n  \"name\": \"hello\",\n  \"value\": 42\n}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void nullFieldsSerializeCorrectly() {
        SimplePojo input = new SimplePojo(null, 0);
        String approvedContent = "{\n  \"name\": null,\n  \"value\": 0\n}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    // ---- Simple POJO (negative) ----

    @Test
    public void pojoMismatchDetected() {
        SimplePojo input = new SimplePojo("world", 99);
        String approvedContent = "{\n  \"name\": \"hello\",\n  \"value\": 42\n}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                "Expected file 4ac405/11b2ef-approved.json\n" +
                        "name\n" +
                        "Expected: hello\n" +
                        "     got: world\n" +
                        " ; value\n" +
                        "Expected: 42\n" +
                        "     got: 99\n");
    }

    // ---- Nested objects ----

    @Test
    public void nestedPojoMatchesApprovedFile() {
        NestedPojo input = new NestedPojo("outer", new SimplePojo("inner", 7));
        String approvedContent = "{\n" +
                "  \"label\": \"outer\",\n" +
                "  \"child\": {\n" +
                "    \"name\": \"inner\",\n" +
                "    \"value\": 7\n" +
                "  }\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void nestedPojoMismatchDetected() {
        NestedPojo input = new NestedPojo("outer", new SimplePojo("wrong", 7));
        String approvedContent = "{\n" +
                "  \"label\": \"outer\",\n" +
                "  \"child\": {\n" +
                "    \"name\": \"inner\",\n" +
                "    \"value\": 7\n" +
                "  }\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                "Expected file 4ac405/11b2ef-approved.json\n" +
                        "child.name\n" +
                        "Expected: inner\n" +
                        "     got: wrong\n");
    }

    // ---- Collections ----

    @Test
    public void beanWithListMatchesApprovedFile() {
        BeanWithList input = new BeanWithList("container", Arrays.asList("a", "b", "c"));
        String approvedContent = "{\n" +
                "  \"label\": \"container\",\n" +
                "  \"items\": [\n" +
                "    \"a\",\n" +
                "    \"b\",\n" +
                "    \"c\"\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void beanWithListMismatchDetected() {
        BeanWithList input = new BeanWithList("container", Arrays.asList("a", "x", "c"));
        String approvedContent = "{\n" +
                "  \"label\": \"container\",\n" +
                "  \"items\": [\n" +
                "    \"a\",\n" +
                "    \"b\",\n" +
                "    \"c\"\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                "Expected file 4ac405/11b2ef-approved.json\n" +
                        "items[1]\n" +
                        "Expected: b\n" +
                        "     got: x\n");
    }

    @Test
    public void beanWithObjectListMatchesApprovedFile() {
        List<SimplePojo> items = Arrays.asList(new SimplePojo("a", 1), new SimplePojo("b", 2));
        BeanWithObjectList input = new BeanWithObjectList("parent", items);
        String approvedContent = "{\n" +
                "  \"label\": \"parent\",\n" +
                "  \"children\": [\n" +
                "    {\n" +
                "      \"name\": \"a\",\n" +
                "      \"value\": 1\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"b\",\n" +
                "      \"value\": 2\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void beanWithArrayMatchesApprovedFile() {
        BeanWithArray input = new BeanWithArray("data", new int[]{10, 20, 30});
        String approvedContent = "{\n" +
                "  \"label\": \"data\",\n" +
                "  \"numbers\": [\n" +
                "    10,\n" +
                "    20,\n" +
                "    30\n" +
                "  ]\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    // ---- Maps ----

    @Test
    public void beanWithMapMatchesApprovedFile() {
        Map<String, Integer> data = new LinkedHashMap<>();
        data.put("alice", 100);
        data.put("bob", 85);
        BeanWithMap input = new BeanWithMap("scores", data);
        String approvedContent = "{\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"alice\": 100\n" +
                "    },\n" +
                "    {\n" +
                "      \"bob\": 85\n" +
                "    }\n" +
                "  ],\n" +
                "  \"label\": \"scores\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void beanWithMapMismatchDetected() {
        Map<String, Integer> data = new LinkedHashMap<>();
        data.put("alice", 50);
        data.put("bob", 85);
        BeanWithMap input = new BeanWithMap("scores", data);
        String approvedContent = "{\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"alice\": 100\n" +
                "    },\n" +
                "    {\n" +
                "      \"bob\": 85\n" +
                "    }\n" +
                "  ],\n" +
                "  \"label\": \"scores\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                "Expected file 4ac405/11b2ef-approved.json\n" +
                        "data[0].alice\n" +
                        "Expected: 100\n" +
                        "     got: 50\n");
    }

    // ---- Inheritance ----

    @Test
    public void inheritedFieldsMatchApprovedFile() {
        ChildPojo input = new ChildPojo("parentVal", "childVal", 7);
        String approvedContent = "{\n" +
                "  \"childField\": \"childVal\",\n" +
                "  \"childValue\": 7,\n" +
                "  \"parentField\": \"parentVal\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void inheritedFieldMismatchDetected() {
        ChildPojo input = new ChildPojo("wrong", "childVal", 7);
        String approvedContent = "{\n" +
                "  \"childField\": \"childVal\",\n" +
                "  \"childValue\": 7,\n" +
                "  \"parentField\": \"parentVal\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                "Expected file 4ac405/11b2ef-approved.json\n" +
                        "parentField\n" +
                        "Expected: parentVal\n" +
                        "     got: wrong\n");
    }

    @Test
    public void grandchildInheritanceMatchesApprovedFile() {
        GrandchildPojo input = new GrandchildPojo("gp", "mid", "gc");
        String approvedContent = "{\n" +
                "  \"gcField\": \"gc\",\n" +
                "  \"gpField\": \"gp\",\n" +
                "  \"middleField\": \"mid\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    // ---- Enums ----

    @Test
    public void beanWithEnumMatchesApprovedFile() {
        BeanWithEnum input = new BeanWithEnum("item", Status.ACTIVE);
        String approvedContent = "{\n" +
                "  \"name\": \"item\",\n" +
                "  \"status\": \"ACTIVE\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void beanWithEnumMismatchDetected() {
        BeanWithEnum input = new BeanWithEnum("item", Status.INACTIVE);
        String approvedContent = "{\n" +
                "  \"name\": \"item\",\n" +
                "  \"status\": \"ACTIVE\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                "Expected file 4ac405/11b2ef-approved.json\n" +
                        "status\n" +
                        "Expected: ACTIVE\n" +
                        "     got: INACTIVE\n");
    }

    // ---- Custom adapters (LocalDate, Instant, Path) ----

    @Test
    public void beanWithLocalDateMatchesApprovedFile() {
        DatePojo input = new DatePojo("event", LocalDate.of(2024, 3, 15));
        String approvedContent = "{\n" +
                "  \"label\": \"event\",\n" +
                "  \"date\": \"2024-03-15\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void beanWithInstantMatchesApprovedFile() {
        InstantPojo input = new InstantPojo("timestamp", Instant.parse("2024-01-15T10:30:00Z"));
        String approvedContent = "{\n" +
                "  \"label\": \"timestamp\",\n" +
                "  \"when\": \"2024-01-15T10:30:00Z\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void beanWithPathMatchesApprovedFile() {
        PathPojo input = new PathPojo("config", Paths.get("/etc/config.yml"));
        String approvedContent = "{\n" +
                "  \"label\": \"config\",\n" +
                "  \"path\": \"/etc/config.yml\"\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    // ---- Throwable ----

    @Test
    public void throwableMatchesApprovedFile() {
        RuntimeException input = new RuntimeException("test error");
        String approvedContent = "{\n" +
                "  \"cause\": null,\n" +
                "  \"class\": \"java.lang.RuntimeException\",\n" +
                "  \"localizedMessage\": \"test error\",\n" +
                "  \"message\": \"test error\",\n" +
                "  \"suppressed\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void throwableMismatchDetected() {
        RuntimeException input = new RuntimeException("actual error");
        String approvedContent = "{\n" +
                "  \"cause\": null,\n" +
                "  \"class\": \"java.lang.RuntimeException\",\n" +
                "  \"localizedMessage\": \"expected error\",\n" +
                "  \"message\": \"expected error\",\n" +
                "  \"suppressed\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                "Expected file 4ac405/11b2ef-approved.json\n" +
                        "localizedMessage\n" +
                        "Expected: expected error\n" +
                        "     got: actual error\n" +
                        " ; message\n" +
                        "Expected: expected error\n" +
                        "     got: actual error\n");
    }

    // ---- Locked-module inheritance (getter-based) ----
    // FileSystemException demonstrates:
    // - Child-specific getters: getFile(), getOtherFile(), getReason()
    // - Inherited getters from Throwable: getMessage(), getCause(), getSuppressed()
    // - Overridden getter: getMessage() returns formatted "/src -> /dst: reason"

    @Test
    public void lockedTypeInheritanceChildGettersPickedUp() {
        java.nio.file.FileSystemException input =
                new java.nio.file.FileSystemException("/src.txt", "/dst.txt", "denied");
        String approvedContent = "{\n" +
                "  \"cause\": null,\n" +
                "  \"class\": \"java.nio.file.FileSystemException\",\n" +
                "  \"file\": \"/src.txt\",\n" +
                "  \"localizedMessage\": \"/src.txt -\\u003e /dst.txt: denied\",\n" +
                "  \"message\": \"/src.txt -\\u003e /dst.txt: denied\",\n" +
                "  \"otherFile\": \"/dst.txt\",\n" +
                "  \"reason\": \"denied\",\n" +
                "  \"suppressed\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void lockedTypeOverriddenGetterReflectsSubclassBehavior() {
        // FileSystemException.getMessage() OVERRIDES Throwable.getMessage()
        // Returns formatted: "/src.txt -> /dst.txt: denied"
        // If parent's implementation were called, it'd return raw detailMessage
        java.nio.file.FileSystemException input =
                new java.nio.file.FileSystemException("/src.txt", "/dst.txt", "allowed");
        String approvedContent = "{\n" +
                "  \"cause\": null,\n" +
                "  \"class\": \"java.nio.file.FileSystemException\",\n" +
                "  \"file\": \"/src.txt\",\n" +
                "  \"localizedMessage\": \"/src.txt -\\u003e /dst.txt: denied\",\n" +
                "  \"message\": \"/src.txt -\\u003e /dst.txt: denied\",\n" +
                "  \"otherFile\": \"/dst.txt\",\n" +
                "  \"reason\": \"denied\",\n" +
                "  \"suppressed\": []\n" +
                "}";
        // reason differs → getMessage() override returns different formatted string
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                "Expected file 4ac405/11b2ef-approved.json\n" +
                        "localizedMessage\n" +
                        "Expected: /src.txt -> /dst.txt: denied\n" +
                        "     got: /src.txt -> /dst.txt: allowed\n" +
                        " ; message\n" +
                        "Expected: /src.txt -> /dst.txt: denied\n" +
                        "     got: /src.txt -> /dst.txt: allowed\n" +
                        " ; reason\n" +
                        "Expected: denied\n" +
                        "     got: allowed\n");
    }

    @Test
    public void lockedTypeMismatchOnChildSpecificGetter() {
        // getFile() is declared in FileSystemException (not inherited from Throwable)
        java.nio.file.FileSystemException input =
                new java.nio.file.FileSystemException("/other.txt", "/dst.txt", "denied");
        String approvedContent = "{\n" +
                "  \"cause\": null,\n" +
                "  \"class\": \"java.nio.file.FileSystemException\",\n" +
                "  \"file\": \"/src.txt\",\n" +
                "  \"localizedMessage\": \"/src.txt -\\u003e /dst.txt: denied\",\n" +
                "  \"message\": \"/src.txt -\\u003e /dst.txt: denied\",\n" +
                "  \"otherFile\": \"/dst.txt\",\n" +
                "  \"reason\": \"denied\",\n" +
                "  \"suppressed\": []\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                "Expected file 4ac405/11b2ef-approved.json\n" +
                        "file\n" +
                        "Expected: /src.txt\n" +
                        "     got: /other.txt\n" +
                        " ; localizedMessage\n" +
                        "Expected: /src.txt -> /dst.txt: denied\n" +
                        "     got: /other.txt -> /dst.txt: denied\n" +
                        " ; message\n" +
                        "Expected: /src.txt -> /dst.txt: denied\n" +
                        "     got: /other.txt -> /dst.txt: denied\n");
    }

    // ---- Ignoring fields ----

    @Test
    public void ignorePathWorksInFallbackMode() {
        SimplePojo input = new SimplePojo("hello", 99);
        String approvedContent = "{\n  \"name\": \"hello\"\n}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                jsonMatcher -> jsonMatcher.ignoring("value"), null);
    }

    @Test
    public void ignoreNestedPathWorksInFallbackMode() {
        NestedPojo input = new NestedPojo("outer", new SimplePojo("inner", 99));
        String approvedContent = "{\n" +
                "  \"label\": \"outer\",\n" +
                "  \"child\": {\n" +
                "    \"name\": \"inner\"\n" +
                "  }\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                jsonMatcher -> jsonMatcher.ignoring("child.value"), null);
    }

    @Test
    public void ignoreMultipleFieldsWorks() {
        SimplePojo input = new SimplePojo("anything", 999);
        String approvedContent = "{}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                jsonMatcher -> jsonMatcher.ignoring("name").ignoring("value"), null);
    }

    // ---- Custom matching ----

    @Test
    public void customMatcherWorksInFallbackMode() {
        SimplePojo input = new SimplePojo("hello world", 42);
        String approvedContent = "{\n  \"value\": 42\n}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                jsonMatcher -> jsonMatcher.with("name", org.hamcrest.Matchers.startsWith("hello")), null);
    }

    // ---- Not-approved file creation ----

    @Test
    public void notApprovedFileCreatedCorrectly() {
        SimplePojo input = new SimplePojo("test", 1);
        inMemoryUnixFs(imfsi -> {
            JsonMatcher<Object> underTest = MATCHER_FACTORY.jsonMatcher(dummyInformation(imfsi), getDefaultFileMatcherConfig());

            AssertionError actualError = Assertions.assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(input, underTest));

            assertEquals(getNotApprovedCreationMessage("4ac405", "11b2ef-not-approved.json", "11b2ef-approved.json"),
                    actualError.getMessage());
        });
    }

    @Test
    public void notApprovedFileForComplexTypeCreated() {
        NestedPojo input = new NestedPojo("outer", new SimplePojo("inner", 5));
        inMemoryUnixFs(imfsi -> {
            JsonMatcher<Object> underTest = MATCHER_FACTORY.jsonMatcher(dummyInformation(imfsi), getDefaultFileMatcherConfig());

            AssertionError actualError = Assertions.assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(input, underTest));

            assertEquals(getNotApprovedCreationMessage("4ac405", "11b2ef-not-approved.json", "11b2ef-approved.json"),
                    actualError.getMessage());
        });
    }

    // ---- Non-standard getter naming (record-style) ----
    // User POJOs on the classpath use field-based access in all modes,
    // so this tests that field access works regardless of getter naming.

    @Test
    public void recordStyleBeanMatchesApprovedFile() {
        RecordStylePojo input = new RecordStylePojo("hello", 42, true);
        // Field names are used (not getter names) since this class is on the classpath
        String approvedContent = "{\n" +
                "  \"active\": true,\n" +
                "  \"name\": \"hello\",\n" +
                "  \"value\": 42\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

    @Test
    public void recordStyleBeanMismatchDetected() {
        RecordStylePojo input = new RecordStylePojo("world", 99, false);
        String approvedContent = "{\n" +
                "  \"active\": true,\n" +
                "  \"name\": \"hello\",\n" +
                "  \"value\": 42\n" +
                "}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                "Expected file 4ac405/11b2ef-approved.json\n" +
                        "active\n" +
                        "Expected: true\n" +
                        "     got: false\n" +
                        " ; name\n" +
                        "Expected: hello\n" +
                        "     got: world\n" +
                        " ; value\n" +
                        "Expected: 42\n" +
                        "     got: 99\n");
    }

    // ---- Test data classes (on classpath = unnamed module → field-based access works) ----

    @SuppressWarnings("unused")
    public static class SimplePojo {
        private final String name;
        private final int value;

        public SimplePojo(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    @SuppressWarnings("unused")
    public static class NestedPojo {
        private final String label;
        private final SimplePojo child;

        public NestedPojo(String label, SimplePojo child) {
            this.label = label;
            this.child = child;
        }

        public String getLabel() { return label; }
        public SimplePojo getChild() { return child; }
    }

    @SuppressWarnings("unused")
    public static class BeanWithList {
        private final String label;
        private final List<String> items;

        public BeanWithList(String label, List<String> items) {
            this.label = label;
            this.items = items;
        }

        public String getLabel() { return label; }
        public List<String> getItems() { return items; }
    }

    @SuppressWarnings("unused")
    public static class BeanWithObjectList {
        private final String label;
        private final List<SimplePojo> children;

        public BeanWithObjectList(String label, List<SimplePojo> children) {
            this.label = label;
            this.children = children;
        }

        public String getLabel() { return label; }
        public List<SimplePojo> getChildren() { return children; }
    }

    @SuppressWarnings("unused")
    public static class BeanWithArray {
        private final String label;
        private final int[] numbers;

        public BeanWithArray(String label, int[] numbers) {
            this.label = label;
            this.numbers = numbers;
        }

        public String getLabel() { return label; }
        public int[] getNumbers() { return numbers; }
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
    public static class ParentPojo {
        private final String parentField;

        public ParentPojo(String parentField) {
            this.parentField = parentField;
        }

        public String getParentField() { return parentField; }
    }

    @SuppressWarnings("unused")
    public static class ChildPojo extends ParentPojo {
        private final String childField;
        private final int childValue;

        public ChildPojo(String parentField, String childField, int childValue) {
            super(parentField);
            this.childField = childField;
            this.childValue = childValue;
        }

        public String getChildField() { return childField; }
        public int getChildValue() { return childValue; }
    }

    @SuppressWarnings("unused")
    public static class GrandparentPojo {
        private final String gpField;

        public GrandparentPojo(String gpField) {
            this.gpField = gpField;
        }

        public String getGpField() { return gpField; }
    }

    @SuppressWarnings("unused")
    public static class MiddlePojo extends GrandparentPojo {
        private final String middleField;

        public MiddlePojo(String gpField, String middleField) {
            super(gpField);
            this.middleField = middleField;
        }

        public String getMiddleField() { return middleField; }
    }

    @SuppressWarnings("unused")
    public static class GrandchildPojo extends MiddlePojo {
        private final String gcField;

        public GrandchildPojo(String gpField, String middleField, String gcField) {
            super(gpField, middleField);
            this.gcField = gcField;
        }

        public String getGcField() { return gcField; }
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
    public static class DatePojo {
        private final String label;
        private final LocalDate date;

        public DatePojo(String label, LocalDate date) {
            this.label = label;
            this.date = date;
        }

        public String getLabel() { return label; }
        public LocalDate getDate() { return date; }
    }

    @SuppressWarnings("unused")
    public static class InstantPojo {
        private final String label;
        private final Instant when;

        public InstantPojo(String label, Instant when) {
            this.label = label;
            this.when = when;
        }

        public String getLabel() { return label; }
        public Instant getWhen() { return when; }
    }

    @SuppressWarnings("unused")
    public static class PathPojo {
        private final String label;
        private final java.nio.file.Path path;

        public PathPojo(String label, java.nio.file.Path path) {
            this.label = label;
            this.path = path;
        }

        public String getLabel() { return label; }
        public java.nio.file.Path getPath() { return path; }
    }

    /**
     * Bean using record-style accessor naming — no getX()/isX() prefix.
     * Since this class is on the classpath, it uses field-based access regardless of mode.
     */
    @SuppressWarnings("unused")
    public static class RecordStylePojo {
        private final String name;
        private final int value;
        private final boolean active;

        public RecordStylePojo(String name, int value, boolean active) {
            this.name = name;
            this.value = value;
            this.active = active;
        }

        // Non-standard accessors
        public String name() { return name; }
        public int value() { return value; }
        public boolean active() { return active; }
    }
}
