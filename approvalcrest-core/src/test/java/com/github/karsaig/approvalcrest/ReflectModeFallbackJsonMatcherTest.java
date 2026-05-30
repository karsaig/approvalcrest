package com.github.karsaig.approvalcrest;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
 * Key behaviors verified:
 * <ul>
 *   <li>User POJOs serialize field-based (same as normal mode — they're on unnamed module)</li>
 *   <li>Types with custom adapters (LocalDate, Instant, Path) produce same output</li>
 *   <li>Throwable uses getter-based delegation (different property names)</li>
 *   <li>Mismatch detection works correctly</li>
 *   <li>Not-approved file creation works</li>
 *   <li>Ignore paths work</li>
 * </ul>
 */
public class ReflectModeFallbackJsonMatcherTest extends AbstractFileMatcherTest {

    @Test
    public void modeIsFallbackInJsonMatcherTest() {
        assertTrue(ReflectUtil.isFallbackMode());
        assertFalse(ReflectUtil.isForceMode());
        assertFalse(ReflectUtil.isUnsafeAvailable());
    }

    @Test
    public void pojoMatchesApprovedFile() {
        SimplePojo input = new SimplePojo("hello", 42);
        String approvedContent = "{\n  \"name\": \"hello\",\n  \"value\": 42\n}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
    }

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

    @Test
    public void throwableMatchesApprovedFile() {
        // In fallback mode, Throwable is serialized via getters:
        // getMessage(), getLocalizedMessage(), getCause(), getSuppressed()
        // ThrowableTypeAdapterFactory removes stackTrace and adds class
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
    public void ignorePathWorksInFallbackMode() {
        SimplePojo input = new SimplePojo("hello", 99);
        // Approved file excludes "value" since we're ignoring it
        String approvedContent = "{\n  \"name\": \"hello\"\n}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent,
                jsonMatcher -> jsonMatcher.ignoring("value"), null);
    }

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
    public void nullFieldsSerializeCorrectly() {
        SimplePojo input = new SimplePojo(null, 0);
        String approvedContent = "{\n  \"name\": null,\n  \"value\": 0\n}";
        assertJsonMatcherWithDummyTestInfo(input, approvedContent, true);
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
}
