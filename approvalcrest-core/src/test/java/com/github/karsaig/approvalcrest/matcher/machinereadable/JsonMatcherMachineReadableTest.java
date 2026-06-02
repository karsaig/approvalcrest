package com.github.karsaig.approvalcrest.matcher.machinereadable;

import com.github.karsaig.approvalcrest.matcher.AbstractFileMatcherTest;
import com.github.karsaig.approvalcrest.matcher.JsonMatcher;
import com.github.karsaig.approvalcrest.testdata.BeanWithPrimitives;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
            JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
            String approvedPath = jsonDir.resolve("11b2ef-approved.json").toAbsolutePath().toString();
            assertAll(
                    () -> assertEquals("MISMATCH", json.get("failureType").getAsString(), "Should contain failureType"),
                    () -> assertEquals("dummyTestClassName#dummyTestMethodName", json.get("test").getAsString(), "Should contain test info"),
                    () -> assertEquals(approvedPath, json.get("approvedFile").getAsString(), "Should contain approved file path"),
                    () -> assertTrue(json.has("action"), "Should contain action"),
                    () -> assertTrue(json.has("actual"), "Should contain actual field"),
                    () -> assertTrue(json.has("ignoredFields"), "Should contain ignoredFields array"),
                    () -> assertTrue(json.has("aliasedFields"), "Should contain aliasedFields array"),
                    () -> assertTrue(json.has("sortedFields"), "Should contain sortedFields array")
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
                JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
                String approvedPath = jsonDir.resolve("11b2ef-approved.json").toAbsolutePath().toString();
                assertEquals(approvedPath, json.get("approvedFile").getAsString());
                assertTrue(json.has("actual"));
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
                    () -> assertFalse(msg.contains("\"failureType\""), "Should NOT contain JSON failureType field"),
                    () -> assertFalse(msg.contains("\"approvedFile\""), "Should NOT contain JSON approvedFile field")
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
            Path approvedFile = classHashDir.resolve("11b2ef-approved.json");
            writeApprovedFile(classHashDir, "11b2ef-approved.json", EXISTING_APPROVED_CONTENT);

            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertThat(actual, underTest));

            String msg = error.getMessage();
            JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
            String expectedAbsPath = approvedFile.toAbsolutePath().toString();
            assertEquals(expectedAbsPath, json.get("approvedFile").getAsString(),
                    "Absolute path must point to the correct file even when working dir is unrelated.");
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
            Path deepTestPath;
            try {
                deepTestPath = Files.createDirectories(fs.getPath("/deep/project/src/test/java/classes"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Path workingDir = fs.getPath("/deep/project");

            DummyInformation dummyTestInfo = new DummyInformation(deepTestPath, imfsi.getResourcePath(), workingDir);
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());
            underTest.withMachineReadableOutput();

            Path classHashDir = deepTestPath.resolve("4ac405");
            Path approvedFile = classHashDir.resolve("11b2ef-approved.json");
            writeApprovedFile(classHashDir, "11b2ef-approved.json", EXISTING_APPROVED_CONTENT);

            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertThat(actual, underTest));

            String msg = error.getMessage();
            JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
            String expectedAbsPath = approvedFile.toAbsolutePath().toString();
            assertEquals(expectedAbsPath, json.get("approvedFile").getAsString(),
                    "Absolute path must be correct even when working dir is an ancestor of the file.");
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
                JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
                String approvedPath = jsonDir.resolve("11b2ef-approved.json").toAbsolutePath().toString();
                assertEquals(approvedPath, json.get("approvedFile").getAsString());
                assertTrue(json.has("actual"));
            });
        } finally {
            System.clearProperty("fMMReadable");
        }
    }

    // Alias test — fmAI alias should enable machine-readable output
    @Test
    public void shouldOutputMachineReadableMessageWhenFmAIAliasPropertyEnabled() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        System.setProperty("fmAI", "true");
        try {
            inMemoryUnixFs(imfsi -> {
                DummyInformation dummyTestInfo = dummyInformation(imfsi);
                JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());

                Path jsonDir = imfsi.getTestPath().resolve("4ac405");
                writeApprovedFile(jsonDir, "11b2ef-approved.json", EXISTING_APPROVED_CONTENT);

                AssertionFailedError error = assertThrows(AssertionFailedError.class,
                        () -> assertThat(actual, underTest));

                String msg = error.getMessage();
                JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
                String approvedPath = jsonDir.resolve("11b2ef-approved.json").toAbsolutePath().toString();
                assertEquals(approvedPath, json.get("approvedFile").getAsString());
                assertTrue(json.has("actual"));
            });
        } finally {
            System.clearProperty("fmAI");
        }
    }

    @Test
    public void shouldTrackIgnoredPathsInJsonMatcherOutput() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi);
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());
            underTest.ignoring("beanLong").withMachineReadableOutput();

            Path jsonDir = imfsi.getTestPath().resolve("4ac405");
            writeApprovedFile(jsonDir, "11b2ef-approved.json", EXISTING_APPROVED_CONTENT);

            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertThat(actual, underTest));

            String msg = error.getMessage();
            JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
            com.google.gson.JsonArray ignoredFields = json.getAsJsonArray("ignoredFields");
            assertTrue(ignoredFields.size() > 0, "ignoredFields should not be empty");

            boolean foundBeanLong = false;
            for (int i = 0; i < ignoredFields.size(); i++) {
                JsonObject entry = ignoredFields.get(i).getAsJsonObject();
                if ("beanLong".equals(entry.get("path").getAsString())
                        && "IGNORE_PATH".equals(entry.get("reason").getAsString())) {
                    foundBeanLong = true;
                    break;
                }
            }
            assertTrue(foundBeanLong, "Should track beanLong as IGNORE_PATH");
        });
    }

    @Test
    public void shouldShowNoteForPatternBasedIgnoringInJsonMatcherOutput() {
        BeanWithPrimitives actual = getBeanWithPrimitives();
        inMemoryUnixFs(imfsi -> {
            DummyInformation dummyTestInfo = dummyInformation(imfsi);
            JsonMatcher<BeanWithPrimitives> underTest = MATCHER_FACTORY.jsonMatcher(dummyTestInfo, getDefaultFileMatcherConfig());
            underTest.ignoring(org.hamcrest.Matchers.startsWith("beanL")).withMachineReadableOutput();

            Path jsonDir = imfsi.getTestPath().resolve("4ac405");
            writeApprovedFile(jsonDir, "11b2ef-approved.json", EXISTING_APPROVED_CONTENT);

            AssertionFailedError error = assertThrows(AssertionFailedError.class,
                    () -> assertThat(actual, underTest));

            String msg = error.getMessage();
            JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
            assertTrue(json.has("note"), "Should have a note about pattern-based ignoring");
            String note = json.get("note").getAsString();
            assertTrue(note.contains("Pattern-based ignoring"), "Note should mention pattern-based ignoring, was: " + note);
        });
    }

    @Test
    public void shouldOutputCompactExpectedAndActualInMachineReadableJson() {
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
            JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
            String expectedValue = json.get("expected").getAsString();
            String actualValue = json.get("actual").getAsString();
            assertFalse(expectedValue.contains("\n"), "expected JSON value in machine-readable output must be compact (no newlines)");
            assertFalse(actualValue.contains("\n"), "actual JSON value in machine-readable output must be compact (no newlines)");
        });
    }
}
