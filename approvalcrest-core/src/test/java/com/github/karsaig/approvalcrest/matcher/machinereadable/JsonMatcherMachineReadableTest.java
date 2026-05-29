package com.github.karsaig.approvalcrest.matcher.machinereadable;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
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
                    () -> assertTrue(msg.contains("FAILURE_TYPE: MISMATCH"), "Should contain failure type header"),
                    () -> assertTrue(msg.contains("TEST: dummyTestClassName#dummyTestMethodName"), "Should contain test info"),
                    () -> assertTrue(msg.contains("APPROVED_FILE: " + approvedPath), "Should contain approved file path"),
                    () -> assertTrue(msg.contains("ACTION: Set system property fMUInPlace=true and re-run to update the approved file"), "Should contain update action"),
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
                assertTrue(msg.contains("APPROVED_FILE: " + approvedPath));
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
                    () -> assertFalse(msg.contains("APPROVED_FILE:"), "Should NOT contain approved file path label")
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

    // Deep path — Case B (content mismatch), working dir completely unrelated to file path
    @Test
    public void shouldShowCorrectAbsolutePathWithDeepDirectoryStructureAndUnrelatedWorkingDirectory() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            FileSystem fs = imfsi.getInMemoryFileSystem();
            // 5 levels deep: /projects/myapp/module/src/test/java
            Path deepTestPath;
            try {
                deepTestPath = Files.createDirectories(fs.getPath("/projects/myapp/module/src/test/java"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Working dir is in a completely separate part of the filesystem
            Path workingDir = fs.getPath("/home/ci/builds/workspace");

            DummyInformation dummyTestInfo = new DummyInformation(deepTestPath, imfsi.getResourcePath(), workingDir);
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());
            underTest.withMachineReadableOutput();

            Path classHashDir = deepTestPath.resolve("4ac405");
            Path approvedFile = classHashDir.resolve("11b2ef-approved.json");
            writeApprovedFile(classHashDir, "11b2ef-approved.json", EXISTING_APPROVED_CONTENT);

            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertThat(actual, underTest));

            String expectedAbsPath = approvedFile.toAbsolutePath().toString();
            assertTrue(error.getMessage().contains("APPROVED_FILE: " + expectedAbsPath),
                    "Absolute path must point to the correct file even when working dir is unrelated. Got: " + error.getMessage());
        });
    }

    // Deep path — Case A (no approved file), working dir completely unrelated to file path
    @Test
    public void shouldShowCorrectAbsoluteNotApprovedPathWithDeepDirectoryStructureAndUnrelatedWorkingDirectory() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            FileSystem fs = imfsi.getInMemoryFileSystem();
            Path deepTestPath;
            try {
                deepTestPath = Files.createDirectories(fs.getPath("/projects/myapp/module/src/test/java"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Path workingDir = fs.getPath("/home/ci/builds/workspace");

            DummyInformation dummyTestInfo = new DummyInformation(deepTestPath, imfsi.getResourcePath(), workingDir);
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());
            underTest.withMachineReadableOutput();

            Path classHashDir = deepTestPath.resolve("4ac405");
            Path notApprovedFile = classHashDir.resolve("11b2ef-not-approved.json");
            Path approvedFile = classHashDir.resolve("11b2ef-approved.json");

            AssertionError error = assertThrows(AssertionError.class,
                    () -> MatcherAssert.assertThat(actual, underTest));

            String expectedMsg = getMachineReadableNotApprovedCreationMessage(notApprovedFile, approvedFile);
            assertEquals(expectedMsg, error.getMessage(),
                    "Both paths in Case A message must be absolute and correct even when working dir is unrelated");
        });
    }

    // Deep path — Case B (content mismatch), working dir is an ancestor of the approved file
    @Test
    public void shouldShowCorrectAbsolutePathWithDeepDirectoryStructureAndWorkingDirAsAncestor() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            FileSystem fs = imfsi.getInMemoryFileSystem();
            // 5 levels deep test path: /deep/project/src/test/java/classes
            Path deepTestPath;
            try {
                deepTestPath = Files.createDirectories(fs.getPath("/deep/project/src/test/java/classes"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Working dir is an ancestor of the approved file's location
            Path workingDir = fs.getPath("/deep/project");

            DummyInformation dummyTestInfo = new DummyInformation(deepTestPath, imfsi.getResourcePath(), workingDir);
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());
            underTest.withMachineReadableOutput();

            Path classHashDir = deepTestPath.resolve("4ac405");
            Path approvedFile = classHashDir.resolve("11b2ef-approved.json");
            writeApprovedFile(classHashDir, "11b2ef-approved.json", EXISTING_APPROVED_CONTENT);

            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertThat(actual, underTest));

            String expectedAbsPath = approvedFile.toAbsolutePath().toString();
            assertTrue(error.getMessage().contains("APPROVED_FILE: " + expectedAbsPath),
                    "Absolute path must be correct even when working dir is an ancestor of the file. Got: " + error.getMessage());
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

    // Alias test — fMMReadable alias should enable machine-readable output
    @Test
    public void shouldOutputMachineReadableMessageWhenAliasPropertyEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        System.setProperty("fMMReadable", "true");
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
                assertTrue(msg.contains("APPROVED_FILE: " + approvedPath));
                assertTrue(msg.contains("=== ACTUAL (full) ==="));
                assertTrue(msg.contains("=== END ACTUAL ==="));
            });
        } finally {
            System.clearProperty("fMMReadable");
        }
    }
}
