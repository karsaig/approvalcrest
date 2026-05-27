package com.github.karsaig.approvalcrest.matcher.machinereadable;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.ContentMatcher;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ContentMatcherMachineReadableTest extends AbstractFileMatcherTest {

    private static final String EXISTING_APPROVED_CONTENT = "approved text content";

    // Case B — mismatch via fluent API (.withMachineReadableOutput())
    @Test
    public void shouldOutputMachineReadableMessageOnMismatchWhenFluentApiEnabled() {
        String actual = "actual text content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi);
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());
            underTest.withMachineReadableOutput();

            Path contentDir = imfsi.getTestPath().resolve("4ac405");
            writeApprovedFile(contentDir, "11b2ef-approved.content", EXISTING_APPROVED_CONTENT);

            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertThat(actual, underTest));

            String msg = error.getMessage();
            String approvedPath = contentDir.resolve("11b2ef-approved.content").toAbsolutePath().toString();
            assertAll(
                    () -> assertTrue(msg.contains("Approved file (expected): " + approvedPath), "Should contain approved file path"),
                    () -> assertTrue(msg.contains("Tip: to update approved content with current actual, re-run with -DfileMatcherUpdateInPlace=true"), "Should contain update tip"),
                    () -> assertTrue(msg.contains("=== ACTUAL (full) ==="), "Should contain ACTUAL block start"),
                    () -> assertTrue(msg.contains("=== END ACTUAL ==="), "Should contain ACTUAL block end"),
                    () -> assertTrue(msg.contains(actual), "Should contain full actual content"),
                    () -> assertFalse(msg.contains("=== EXPECTED (full) ==="), "Should NOT contain EXPECTED block for file matchers")
            );
        });
    }

    // Case B — mismatch with machine-readable OFF (default): old diff format unchanged
    @Test
    public void shouldOutputDiffOnMismatchWhenMachineReadableDisabled() {
        String actual = "actual text content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi);
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            Path contentDir = imfsi.getTestPath().resolve("4ac405");
            writeApprovedFile(contentDir, "11b2ef-approved.content", EXISTING_APPROVED_CONTENT);

            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertThat(actual, underTest));

            String msg = error.getMessage();
            assertAll(
                    () -> assertFalse(msg.contains("=== ACTUAL (full) ==="), "Should NOT contain machine-readable ACTUAL block"),
                    () -> assertFalse(msg.contains("Approved file (expected):"), "Should NOT contain approved file path label"),
                    () -> assertTrue(msg.contains("Content does not match!"), "Should contain diff message")
            );
        });
    }

    // Case A — no approved file, machine-readable ON: absolute paths in message
    @Test
    public void shouldOutputAbsolutePathsWhenNoApprovedFileAndMachineReadableEnabled() {
        String actual = "actual text content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi);
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());
            underTest.withMachineReadableOutput();

            Path contentDir = imfsi.getTestPath().resolve("4ac405");
            Path notApprovedFile = contentDir.resolve("11b2ef-not-approved.content");
            Path approvedFile = contentDir.resolve("11b2ef-approved.content");

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
        String actual = "actual text content";
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi);
            ContentMatcher<String> underTest = new ContentMatcher<>(dummyTestInfo, getDefaultFileMatcherConfig());

            AssertionError error = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            assertEquals(getNotApprovedCreationMessage("4ac405", "11b2ef-not-approved.content", "11b2ef-approved.content"),
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
