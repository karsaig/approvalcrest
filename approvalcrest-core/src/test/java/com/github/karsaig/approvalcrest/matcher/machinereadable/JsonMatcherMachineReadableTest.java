package com.github.karsaig.approvalcrest.matcher.machinereadable;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class JsonMatcherMachineReadableTest extends AbstractFileMatcherTest {

    private static final String EXISTING_APPROVED_CONTENT = "{\n  \"beanBoolean\": false\n}";

    // Case B — mismatch via fluent API (.withMachineReadableOutput())
    @Test
    public void shouldOutputMachineReadableMessageOnMismatchWhenFluentApiEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi);
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());
            underTest.withMachineReadableOutput();

            Path jsonDir = imfsi.getTestPath().resolve("4ac405");
            writeApprovedFile(jsonDir, "11b2ef-approved.json", EXISTING_APPROVED_CONTENT);

            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertThat(actual, underTest));

            String msg = error.getMessage();
            String approvedPath = jsonDir.resolve("11b2ef-approved.json").toAbsolutePath().toString();
            assertAll(
                    () -> assertTrue(msg.contains("Approved file (expected): " + approvedPath), "Should contain approved file path"),
                    () -> assertTrue(msg.contains("Tip: to update approved content with current actual, re-run with -DfileMatcherUpdateInPlace=true"), "Should contain update tip"),
                    () -> assertTrue(msg.contains("=== ACTUAL (full) ==="), "Should contain ACTUAL block start"),
                    () -> assertTrue(msg.contains("=== END ACTUAL ==="), "Should contain ACTUAL block end"),
                    () -> assertFalse(msg.contains("=== EXPECTED (full) ==="), "Should NOT contain EXPECTED block for file matchers")
            );
        });
    }

    // Case B — mismatch via system property
    @Test
    public void shouldOutputMachineReadableMessageOnMismatchWhenSystemPropertyEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        System.setProperty("fileMatcherMachineReadable", "true");
        try {
            inMemoryUnixFs(imfsi -> {
                DummyInformation dummyTestInfo = dummyInformation(imfsi);
                JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

                Path jsonDir = imfsi.getTestPath().resolve("4ac405");
                writeApprovedFile(jsonDir, "11b2ef-approved.json", EXISTING_APPROVED_CONTENT);

                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertThat(actual, underTest));

                String msg = error.getMessage();
                String approvedPath = jsonDir.resolve("11b2ef-approved.json").toAbsolutePath().toString();
                assertTrue(msg.contains("Approved file (expected): " + approvedPath));
                assertTrue(msg.contains("=== ACTUAL (full) ==="));
                assertTrue(msg.contains("=== END ACTUAL ==="));
            });
        } finally {
            System.clearProperty("fileMatcherMachineReadable");
        }
    }

    // Case B — mismatch with machine-readable OFF (default): structured output absent
    @Test
    public void shouldNotOutputMachineReadableMarkersWhenMachineReadableDisabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi);
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            Path jsonDir = imfsi.getTestPath().resolve("4ac405");
            writeApprovedFile(jsonDir, "11b2ef-approved.json", EXISTING_APPROVED_CONTENT);

            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertThat(actual, underTest));

            String msg = error.getMessage();
            assertAll(
                    () -> assertFalse(msg.contains("=== ACTUAL (full) ==="), "Should NOT contain machine-readable ACTUAL block"),
                    () -> assertFalse(msg.contains("Approved file (expected):"), "Should NOT contain approved file path label")
            );
        });
    }

    // Case A — no approved file, machine-readable ON: absolute paths in message
    @Test
    public void shouldOutputAbsolutePathsWhenNoApprovedFileAndMachineReadableEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi);
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());
            underTest.withMachineReadableOutput();

            Path jsonDir = imfsi.getTestPath().resolve("4ac405");
            Path notApprovedFile = jsonDir.resolve("11b2ef-not-approved.json");
            Path approvedFile = jsonDir.resolve("11b2ef-approved.json");

            AssertionError error = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            String msg = error.getMessage();
            String expectedMsg = getMachineReadableNotApprovedCreationMessage(notApprovedFile, approvedFile);
            assertEquals(expectedMsg, msg);
        });
    }

    // Case A — no approved file, machine-readable OFF (default): old relative-path format unchanged
    @Test
    public void shouldOutputRelativePathWhenNoApprovedFileAndMachineReadableDisabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi);
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError error = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            assertEquals(getNotApprovedCreationMessage("4ac405", "11b2ef-not-approved.json", "11b2ef-approved.json"),
                    error.getMessage());
        });
    }

    private void writeApprovedFile(Path dir, String fileName, String content) {
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(fileName), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
